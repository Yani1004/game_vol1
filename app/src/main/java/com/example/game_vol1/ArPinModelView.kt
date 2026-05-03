package com.example.game_vol1

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.Shader
import android.net.Uri
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.SceneView
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import kotlin.math.cos
import kotlin.math.sin

class ArPinModelView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : FrameLayout(context, attrs) {
    private val sceneView = SceneView(context)
    private val fallbackView = FallbackPinView(context)
    private val pinNode = Node()
    private var modelLoaded = false

    init {
        sceneView.setTransparent(true)
        sceneView.visibility = INVISIBLE
        addView(sceneView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
        addView(fallbackView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))

        sceneView.scene.camera.apply {
            localPosition = Vector3(0f, 0f, 2.2f)
            setLookDirection(Vector3(0f, 0f, -1f), Vector3(0f, 1f, 0f))
        }
        pinNode.apply {
            setParent(sceneView.scene)
            localPosition = Vector3(0f, -0.12f, -1.15f)
            localScale = Vector3(0.45f, 0.45f, 0.45f)
            localRotation = Quaternion.axisAngle(Vector3(0f, 1f, 0f), 180f)
        }

        loadPinModel()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        resumeRendering()
    }

    override fun onDetachedFromWindow() {
        pauseRendering()
        destroyRendering()
        super.onDetachedFromWindow()
    }

    fun resumeRendering() {
        if (!modelLoaded && sceneView.visibility != VISIBLE) return
        runCatching { sceneView.resume() }
            .onFailure { Log.w(TAG, "Could not resume AR pin renderer.", it) }
    }

    fun pauseRendering() {
        runCatching { sceneView.pause() }
            .onFailure { Log.w(TAG, "Could not pause AR pin renderer.", it) }
    }

    private fun destroyRendering() {
        runCatching { sceneView.destroy() }
            .onFailure { Log.w(TAG, "Could not destroy AR pin renderer.", it) }
    }

    private fun loadPinModel() {
        ModelRenderable.builder()
            .setSource(context, Uri.parse(PIN_MODEL_ASSET))
            .setIsFilamentGltf(true)
            .build()
            .thenAccept { renderable ->
                modelLoaded = true
                pinNode.renderable = renderable
                fallbackView.visibility = GONE
                sceneView.visibility = VISIBLE
                resumeRendering()
            }
            .exceptionally { throwable ->
                Log.e(TAG, "Could not load location pin model.", throwable)
                modelLoaded = false
                sceneView.visibility = INVISIBLE
                fallbackView.visibility = VISIBLE
                null
            }
    }

    private class FallbackPinView(context: Context) : View(context) {
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val pointPath = Path()
        private var rotation = 0f
        private var cachedWidth = 0
        private var cachedHeight = 0
        private var shadowShader: RadialGradient? = null

        private val animator = ValueAnimator.ofFloat(0f, 360f).apply {
            duration = 2600L
            repeatCount = ValueAnimator.INFINITE
            addUpdateListener {
                rotation = it.animatedValue as Float
                invalidate()
            }
        }

        override fun onAttachedToWindow() {
            super.onAttachedToWindow()
            animator.start()
        }

        override fun onDetachedFromWindow() {
            animator.cancel()
            super.onDetachedFromWindow()
        }

        override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
            super.onSizeChanged(w, h, oldw, oldh)
            cachedWidth = w
            cachedHeight = h
            val width = w.toFloat()
            val height = h.toFloat()
            shadowShader = RadialGradient(
                width / 2f,
                height * 0.78f,
                width * 0.34f,
                0x8838BDF8.toInt(),
                Color.TRANSPARENT,
                Shader.TileMode.CLAMP,
            )
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            val w = width.toFloat()
            val h = height.toFloat()
            if (cachedWidth != width || cachedHeight != height) {
                onSizeChanged(width, height, cachedWidth, cachedHeight)
            }
            val cx = w / 2f
            val cy = h * 0.42f
            val pulse = 0.92f + 0.08f * sin(Math.toRadians(rotation.toDouble())).toFloat()
            val tilt = cos(Math.toRadians(rotation.toDouble())).toFloat()

            paint.shader = shadowShader
            canvas.drawOval(cx - w * 0.34f, h * 0.70f, cx + w * 0.34f, h * 0.86f, paint)
            paint.shader = null

            canvas.save()
            canvas.scale(pulse, pulse, cx, cy)

            val bodyWidth = w * (0.28f + 0.05f * tilt)
            val bodyHeight = h * 0.38f
            val left = cx - bodyWidth
            val right = cx + bodyWidth
            val top = cy - bodyHeight * 0.72f
            val bottom = cy + bodyHeight * 0.72f

            paint.color = 0xFFF97316.toInt()
            canvas.drawOval(left, top, right, bottom, paint)

            paint.color = 0xAA07111F.toInt()
            canvas.drawCircle(cx + bodyWidth * 0.08f, cy - bodyHeight * 0.05f, bodyWidth * 0.42f, paint)

            paint.color = 0xFFF8FAFC.toInt()
            canvas.drawCircle(cx + bodyWidth * 0.08f, cy - bodyHeight * 0.05f, bodyWidth * 0.25f, paint)

            paint.color = 0xFFEF4444.toInt()
            pointPath.reset()
            pointPath.moveTo(cx - bodyWidth * 0.48f, cy + bodyHeight * 0.35f)
            pointPath.lineTo(cx + bodyWidth * 0.48f, cy + bodyHeight * 0.35f)
            pointPath.lineTo(cx, h * 0.86f)
            pointPath.close()
            canvas.drawPath(pointPath, paint)

            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 6f
            paint.color = 0xCCF8FAFC.toInt()
            canvas.drawOval(left + 10f, top + 8f, right - 10f, bottom - 8f, paint)
            paint.style = Paint.Style.FILL

            canvas.restore()
        }
    }

    companion object {
        private const val TAG = "ArPinModelView"
        private const val PIN_MODEL_ASSET = "models/location_pin/location_tag.gltf"
    }
}
