package com.example.game_vol1

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View
import kotlin.math.cos
import kotlin.math.sin

class ArPinModelView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var rotation = 0f

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

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        val cx = w / 2f
        val cy = h * 0.42f
        val pulse = 0.92f + 0.08f * sin(Math.toRadians(rotation.toDouble())).toFloat()
        val tilt = cos(Math.toRadians(rotation.toDouble())).toFloat()

        paint.shader = RadialGradient(cx, h * 0.78f, w * 0.34f, 0x8838BDF8.toInt(), Color.TRANSPARENT, Shader.TileMode.CLAMP)
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

        paint.shader = LinearGradient(left, top, right, bottom, 0xFF22C55E.toInt(), 0xFF38BDF8.toInt(), Shader.TileMode.CLAMP)
        canvas.drawOval(left, top, right, bottom, paint)
        paint.shader = null

        paint.color = 0xAA07111F.toInt()
        canvas.drawCircle(cx + bodyWidth * 0.08f, cy - bodyHeight * 0.05f, bodyWidth * 0.42f, paint)

        paint.color = 0xFFF8FAFC.toInt()
        canvas.drawCircle(cx + bodyWidth * 0.08f, cy - bodyHeight * 0.05f, bodyWidth * 0.25f, paint)

        paint.shader = LinearGradient(cx, cy + bodyHeight * 0.35f, cx, h * 0.86f, 0xFF22C55E.toInt(), 0xFFF59E0B.toInt(), Shader.TileMode.CLAMP)
        val point = Path().apply {
            moveTo(cx - bodyWidth * 0.48f, cy + bodyHeight * 0.35f)
            lineTo(cx + bodyWidth * 0.48f, cy + bodyHeight * 0.35f)
            lineTo(cx, h * 0.86f)
            close()
        }
        canvas.drawPath(point, paint)
        paint.shader = null

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 6f
        paint.color = 0xCCF8FAFC.toInt()
        canvas.drawOval(left + 10f, top + 8f, right - 10f, bottom - 8f, paint)

        paint.strokeWidth = 4f
        paint.color = 0xAA38BDF8.toInt()
        canvas.drawOval(cx - w * 0.42f, h * 0.07f, cx + w * 0.42f, h * 0.95f, paint)
        canvas.drawOval(cx - w * 0.30f, h * 0.16f, cx + w * 0.30f, h * 0.88f, paint)
        paint.style = Paint.Style.FILL

        paint.textAlign = Paint.Align.CENTER
        paint.textSize = 28f
        paint.typeface = android.graphics.Typeface.DEFAULT_BOLD
        paint.color = 0xFFF8FAFC.toInt()
        canvas.drawText("3D PIN", cx, h * 0.14f, paint)

        canvas.restore()
    }
}
