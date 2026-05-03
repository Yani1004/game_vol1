package com.example.game_vol1

import android.Manifest
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CaptureRequest
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.view.Surface
import android.view.TextureView
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.game_vol1.data.GameRepository
import com.google.android.gms.location.LocationServices
import kotlin.math.abs

class ArDemoActivity : AppCompatActivity(), SensorEventListener {
    private lateinit var cameraPreview: TextureView
    private lateinit var arPinModel: ArPinModelView
    private lateinit var arScanLine: View
    private lateinit var btnScanPin: Button
    private lateinit var tvArStatus: TextView
    private lateinit var tvArLiveHelp: TextView
    private lateinit var sensorManager: SensorManager
    private var cameraDevice: CameraDevice? = null
    private var captureSession: CameraCaptureSession? = null
    private var cameraThread: HandlerThread? = null
    private var cameraHandler: Handler? = null
    private var placeId: String = ""
    private var placeLatitude: Double? = null
    private var placeLongitude: Double? = null
    private var userLatitude: Double? = null
    private var userLongitude: Double? = null
    private val accelerometerReading = FloatArray(3)
    private val magnetometerReading = FloatArray(3)
    private var hasAccelerometer = false
    private var hasMagnetometer = false
    private var headingDegrees = 0f
    private var pinInView = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ar_demo)

        cameraPreview = findViewById(R.id.cameraPreview)
        arPinModel = findViewById(R.id.arPinModel)
        arScanLine = findViewById(R.id.arScanLine)
        btnScanPin = findViewById(R.id.btnScanPin)
        tvArStatus = findViewById(R.id.tvArStatus)
        tvArLiveHelp = findViewById(R.id.tvArLiveHelp)
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        val title = intent.getStringExtra(EXTRA_PLACE_TITLE) ?: "Selected place"
        val city = intent.getStringExtra(EXTRA_PLACE_CITY) ?: "Bulgaria"
        placeId = intent.getStringExtra(EXTRA_PLACE_ID) ?: ""
        val place = GameRepository.placeById(placeId)
        placeLatitude = if (intent.hasExtra(EXTRA_PLACE_LATITUDE)) {
            intent.getDoubleExtra(EXTRA_PLACE_LATITUDE, 0.0)
        } else {
            place?.latitude
        }
        placeLongitude = if (intent.hasExtra(EXTRA_PLACE_LONGITUDE)) {
            intent.getDoubleExtra(EXTRA_PLACE_LONGITUDE, 0.0)
        } else {
            place?.longitude
        }
        if (intent.hasExtra(EXTRA_USER_LATITUDE) && intent.hasExtra(EXTRA_USER_LONGITUDE)) {
            userLatitude = intent.getDoubleExtra(EXTRA_USER_LATITUDE, 0.0)
            userLongitude = intent.getDoubleExtra(EXTRA_USER_LONGITUDE, 0.0)
        }

        findViewById<TextView>(R.id.tvArTitle).text = title
        findViewById<TextView>(R.id.tvArSubtitle).text = "Point the camera toward the AR pin near $city"
        btnScanPin.setOnClickListener { scanPin() }
        findViewById<Button>(R.id.btnCloseAr).setOnClickListener { finish() }

        updateScanState()
        startOverlayAnimation()
        cameraPreview.surfaceTextureListener = surfaceListener
    }

    override fun onResume() {
        super.onResume()
        arPinModel.resumeRendering()
        startCameraThread()
        registerCompass()
        refreshCurrentLocation()
        if (cameraPreview.isAvailable) openCamera() else cameraPreview.surfaceTextureListener = surfaceListener
    }

    override fun onPause() {
        arPinModel.pauseRendering()
        sensorManager.unregisterListener(this)
        closeCamera()
        stopCameraThread()
        super.onPause()
    }

    private fun startOverlayAnimation() {
        ObjectAnimator.ofFloat(arScanLine, View.TRANSLATION_Y, -120f, 120f).apply {
            duration = 1300L
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
            start()
        }
    }

    private fun updateScanState() {
        val place = GameRepository.placeById(placeId)
        val lat = userLatitude
        val lng = userLongitude
        val placeLat = placeLatitude
        val placeLng = placeLongitude
        if (place == null || lat == null || lng == null || placeLat == null || placeLng == null) {
            btnScanPin.isEnabled = false
            btnScanPin.alpha = 0.55f
            arPinModel.visibility = View.INVISIBLE
            arScanLine.visibility = View.INVISIBLE
            tvArLiveHelp.text = "WAITING FOR GPS"
            tvArLiveHelp.setTextColor(ContextCompat.getColor(this, R.color.game_accent))
            tvArStatus.text = "Waiting for GPS before the world pin can be placed."
            return
        }

        val distance = GameRepository.distanceMeters(lat, lng, place)
        val radius = GameRepository.discoveryRadiusMeters().toInt()
        val targetBearing = bearingToPlace(lat, lng, placeLat, placeLng)
        val headingDelta = signedAngleDifference(targetBearing, headingDegrees)
        pinInView = GameRepository.canDiscover(distance) && abs(headingDelta) <= HEADING_TOLERANCE_DEGREES
        arPinModel.visibility = if (pinInView) View.VISIBLE else View.INVISIBLE
        arScanLine.visibility = if (pinInView) View.VISIBLE else View.INVISIBLE
        btnScanPin.isEnabled = pinInView
        btnScanPin.alpha = if (pinInView) 1f else 0.55f

        tvArStatus.text = when {
            !GameRepository.canDiscover(distance) -> {
                btnScanPin.text = "Move within ${radius}m"
                tvArLiveHelp.text = "MOVE CLOSER"
                tvArLiveHelp.setTextColor(ContextCompat.getColor(this, R.color.game_accent))
                "You are ${distance.toInt()}m away. Get within ${radius}m, then search with the camera."
            }
            pinInView -> {
                btnScanPin.text = "Scan location pin"
                tvArLiveHelp.text = "PIN IN VIEW - SCAN"
                tvArLiveHelp.setTextColor(ContextCompat.getColor(this, R.color.game_primary))
                "Pin found in camera. Hold steady and scan it."
            }
            headingDelta > 0 -> {
                btnScanPin.text = "Find the pin"
                val degrees = abs(headingDelta).toInt()
                tvArLiveHelp.text = "TURN RIGHT > ${degrees} DEG"
                tvArLiveHelp.setTextColor(ContextCompat.getColor(this, R.color.game_secondary))
                "Pin is nearby. Bearing ${targetBearing.toInt()} deg | heading ${headingDegrees.toInt()} deg."
            }
            else -> {
                btnScanPin.text = "Find the pin"
                val degrees = abs(headingDelta).toInt()
                tvArLiveHelp.text = "< TURN LEFT ${degrees} DEG"
                tvArLiveHelp.setTextColor(ContextCompat.getColor(this, R.color.game_secondary))
                "Pin is nearby. Bearing ${targetBearing.toInt()} deg | heading ${headingDegrees.toInt()} deg."
            }
        }
    }

    private fun registerCompass() {
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.also {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
        sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)?.also {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    private fun refreshCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            updateScanState()
            return
        }
        LocationServices.getFusedLocationProviderClient(this).lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                userLatitude = location.latitude
                userLongitude = location.longitude
            }
            updateScanState()
        }
    }

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                System.arraycopy(event.values, 0, accelerometerReading, 0, accelerometerReading.size)
                hasAccelerometer = true
            }
            Sensor.TYPE_MAGNETIC_FIELD -> {
                System.arraycopy(event.values, 0, magnetometerReading, 0, magnetometerReading.size)
                hasMagnetometer = true
            }
        }
        if (hasAccelerometer && hasMagnetometer) {
            val rotationMatrix = FloatArray(9)
            val orientationAngles = FloatArray(3)
            if (SensorManager.getRotationMatrix(rotationMatrix, null, accelerometerReading, magnetometerReading)) {
                SensorManager.getOrientation(rotationMatrix, orientationAngles)
                headingDegrees = ((Math.toDegrees(orientationAngles[0].toDouble()).toFloat() + 360f) % 360f)
                updateScanState()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    private fun bearingToPlace(fromLat: Double, fromLng: Double, toLat: Double, toLng: Double): Float {
        val results = FloatArray(3)
        Location.distanceBetween(fromLat, fromLng, toLat, toLng, results)
        return (results[1] + 360f) % 360f
    }

    private fun signedAngleDifference(target: Float, current: Float): Float =
        ((target - current + 540f) % 360f) - 180f

    private fun scanPin() {
        val place = GameRepository.placeById(placeId)
        val lat = userLatitude
        val lng = userLongitude
        if (!pinInView) {
            Toast.makeText(this, "Point the camera at the location pin first.", Toast.LENGTH_SHORT).show()
            updateScanState()
            return
        }
        if (place == null || lat == null || lng == null) {
            Toast.makeText(this, "GPS is not ready for scanning.", Toast.LENGTH_SHORT).show()
            return
        }

        val result = GameRepository.discoverPlace(this, place, lat, lng)
        if (result.success) {
            Toast.makeText(this, "Pin scanned: +${result.pointsAwarded} points", Toast.LENGTH_LONG).show()
            btnScanPin.text = "Pin scanned"
            btnScanPin.isEnabled = false
            btnScanPin.alpha = 0.65f
            tvArLiveHelp.text = "DISCOVERED"
            tvArLiveHelp.setTextColor(ContextCompat.getColor(this, R.color.game_primary))
            tvArStatus.text = "Discovery saved. Return to the map to continue."
        } else {
            updateScanState()
            Toast.makeText(this, "Move closer to scan this pin.", Toast.LENGTH_SHORT).show()
        }
    }

    private val surfaceListener = object : TextureView.SurfaceTextureListener {
        override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
            openCamera()
        }

        override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) = Unit
        override fun onSurfaceTextureUpdated(surface: SurfaceTexture) = Unit
        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean = true
    }

    private fun startCameraThread() {
        cameraThread = HandlerThread("ArDemoCamera").also {
            it.start()
            cameraHandler = Handler(it.looper)
        }
    }

    private fun stopCameraThread() {
        cameraThread?.quitSafely()
        cameraThread?.join()
        cameraThread = null
        cameraHandler = null
    }

    private fun closeCamera() {
        captureSession?.close()
        captureSession = null
        cameraDevice?.close()
        cameraDevice = null
    }

    private fun openCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA)
            return
        }
        openCameraWithPermission()
    }

    @SuppressLint("MissingPermission")
    private fun openCameraWithPermission() {
        val manager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val cameraId = manager.cameraIdList.firstOrNull { id ->
            manager.getCameraCharacteristics(id).get(CameraCharacteristics.LENS_FACING) ==
                    CameraCharacteristics.LENS_FACING_BACK
        } ?: manager.cameraIdList.firstOrNull()

        if (cameraId == null) {
            Toast.makeText(this, "No camera found.", Toast.LENGTH_SHORT).show()
            return
        }

        manager.openCamera(
            cameraId,
            object : CameraDevice.StateCallback() {
                override fun onOpened(camera: CameraDevice) {
                    cameraDevice = camera
                    startPreview(camera)
                }

                override fun onDisconnected(camera: CameraDevice) {
                    camera.close()
                    cameraDevice = null
                }

                override fun onError(camera: CameraDevice, error: Int) {
                    camera.close()
                    cameraDevice = null
                    Toast.makeText(this@ArDemoActivity, "Camera preview failed.", Toast.LENGTH_SHORT).show()
                }
            },
            cameraHandler,
        )
    }

    private fun startPreview(camera: CameraDevice) {
        val texture = cameraPreview.surfaceTexture ?: return
        texture.setDefaultBufferSize(cameraPreview.width.coerceAtLeast(1), cameraPreview.height.coerceAtLeast(1))
        val surface = Surface(texture)
        val request = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW).apply {
            addTarget(surface)
            set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO)
        }

        camera.createCaptureSession(
            listOf(surface),
            object : CameraCaptureSession.StateCallback() {
                override fun onConfigured(session: CameraCaptureSession) {
                    captureSession = session
                    session.setRepeatingRequest(request.build(), null, cameraHandler)
                }

                override fun onConfigureFailed(session: CameraCaptureSession) {
                    Toast.makeText(this@ArDemoActivity, "Could not start camera preview.", Toast.LENGTH_SHORT).show()
                }
            },
            cameraHandler,
        )
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CAMERA && grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) {
            openCamera()
        } else if (requestCode == REQUEST_CAMERA) {
            Toast.makeText(this, "Camera permission is needed for AR view.", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        const val EXTRA_PLACE_TITLE = "extra_place_title"
        const val EXTRA_PLACE_CITY = "extra_place_city"
        const val EXTRA_PLACE_ID = "extra_place_id"
        const val EXTRA_PLACE_LATITUDE = "extra_place_latitude"
        const val EXTRA_PLACE_LONGITUDE = "extra_place_longitude"
        const val EXTRA_USER_LATITUDE = "extra_user_latitude"
        const val EXTRA_USER_LONGITUDE = "extra_user_longitude"
        private const val HEADING_TOLERANCE_DEGREES = 35f
        private const val REQUEST_CAMERA = 42
    }
}
