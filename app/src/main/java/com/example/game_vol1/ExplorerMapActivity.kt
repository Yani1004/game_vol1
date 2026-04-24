package com.example.game_vol1

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.game_vol1.data.GameRepository
import com.example.game_vol1.models.HeritagePlace
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomnavigation.BottomNavigationView

class ExplorerMapActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var tvExplorerStatus: TextView
    private lateinit var tvSelectedPlace: TextView
    private lateinit var tvDistance: TextView
    private lateinit var tvPlaceInfo: TextView
    private lateinit var btnDiscover: Button
    private lateinit var btnCamera: Button
    private lateinit var btnDaily: Button

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var googleMap: GoogleMap? = null
    private var currentLocation: Location? = null
    private var selectedPlace: HeritagePlace? = null
    private val markerPlaceMap = mutableMapOf<Marker, HeritagePlace>()

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        Toast.makeText(this, UiLanguageStore.pick(this, "Камерата е отворена.", "Camera opened."), Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.geo_activity_explore)

        tvExplorerStatus = findViewById(R.id.tvExplorerStatus)
        tvSelectedPlace = findViewById(R.id.tvSelectedPlace)
        tvDistance = findViewById(R.id.tvDistance)
        tvPlaceInfo = findViewById(R.id.tvPlaceInfo)
        btnDiscover = findViewById(R.id.btnDiscoverPlace)
        btnCamera = findViewById(R.id.btnOpenCamera)
        btnDaily = findViewById(R.id.btnOpenDaily)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        btnDiscover.setOnClickListener { discoverSelectedPlace() }
        btnCamera.setOnClickListener { openCamera() }
        btnDaily.setOnClickListener { startActivity(Intent(this, GoalsActivity::class.java)) }

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        applyLanguage()
        updateHeader()
    }

    override fun onResume() {
        super.onResume()
        applyLanguage()
        updateHeader()
        renderSelectedPlace()
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        map.uiSettings.isMapToolbarEnabled = false
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(42.7339, 25.4858), 6.2f))
        map.setOnMarkerClickListener { marker ->
            val place = markerPlaceMap[marker] ?: return@setOnMarkerClickListener false
            selectedPlace = place
            renderSelectedPlace()
            true
        }
        addPlaceMarkers()
        requestLocationAndRefresh()
    }

    private fun addPlaceMarkers() {
        val map = googleMap ?: return
        markerPlaceMap.clear()
        GameRepository.getPlaces().forEach { place ->
            val marker = map.addMarker(
                MarkerOptions()
                    .position(LatLng(place.latitude, place.longitude))
                    .title(place.title),
            )
            if (marker != null) {
                markerPlaceMap[marker] = place
            }
        }
    }

    private fun requestLocationAndRefresh() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            googleMap?.isMyLocationEnabled = true
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    currentLocation = location
                    googleMap?.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(LatLng(location.latitude, location.longitude), 11.5f),
                    )
                    updateHeader()
                    renderSelectedPlace()
                }
            }
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                11,
            )
        }
    }

    private fun updateHeader() {
        val profile = GameRepository.loadProfile(this)
        tvExplorerStatus.text = if (currentLocation == null) {
            if (UiLanguageStore.isBulgarian(this)) "${profile.username} | точки ${profile.totalScore} | изчакване на GPS"
            else "${profile.username} | Score ${profile.totalScore} | Waiting for GPS"
        } else {
            if (UiLanguageStore.isBulgarian(this)) "${profile.username} | точки ${profile.totalScore} | GPS е готов"
            else "${profile.username} | Score ${profile.totalScore} | GPS ready"
        }
    }

    private fun renderSelectedPlace() {
        val place = selectedPlace
        if (place == null) {
            tvSelectedPlace.text = UiLanguageStore.pick(this, "Избери обект от картата", "Select a place marker")
            tvDistance.text = UiLanguageStore.pick(this, "Докосни маркер, за да видиш разстоянието.", "Choose a landmark on the map to see your distance.")
            tvPlaceInfo.text = UiLanguageStore.pick(
                this,
                "Отиди до мястото, приближи се на ${GameRepository.discoveryRadiusMeters().toInt()} метра и натисни Открий, за да го запазиш в историята си.",
                "Walk to a place, get within ${GameRepository.discoveryRadiusMeters().toInt()} meters, then tap Discover to save it into your history.",
            )
            return
        }

        tvSelectedPlace.text = "${place.title} | ${place.city}"
        tvPlaceInfo.text = "${place.shortDescription}\n\n${place.historicalInfo}"

        val current = currentLocation
        if (current == null) {
            tvDistance.text = UiLanguageStore.pick(this, "Разстоянието ще се покаже след GPS локализация.", "Distance unavailable until GPS locks in.")
            return
        }

        val distance = GameRepository.distanceMeters(current.latitude, current.longitude, place)
        tvDistance.text =
            if (UiLanguageStore.isBulgarian(this)) {
                "Разстояние: ${distance.toInt()} м | ${if (GameRepository.canDiscover(distance)) "Можеш да откриеш това място." else "Приближи се, за да го отключиш."}"
            } else {
                "Distance: ${distance.toInt()} m | ${if (GameRepository.canDiscover(distance)) "You can discover this place now." else "Move closer to unlock it."}"
            }
    }

    private fun discoverSelectedPlace() {
        val place = selectedPlace
        val location = currentLocation
        if (place == null) {
            Toast.makeText(this, UiLanguageStore.pick(this, "Първо избери обект от картата.", "Choose a place marker first."), Toast.LENGTH_SHORT).show()
            return
        }
        if (location == null) {
            Toast.makeText(this, UiLanguageStore.pick(this, "Изчаква се GPS местоположението ти.", "Waiting for your GPS location."), Toast.LENGTH_SHORT).show()
            return
        }

        val result = GameRepository.discoverPlace(this, place, location.latitude, location.longitude)
        if (!result.success) {
            Toast.makeText(
                this,
                if (UiLanguageStore.isBulgarian(this)) {
                    "Намираш се на ${result.distanceMeters.toInt()} метра. Приближи се в радиус от ${GameRepository.discoveryRadiusMeters().toInt()} метра."
                } else {
                    "You are ${result.distanceMeters.toInt()} meters away. Move within ${GameRepository.discoveryRadiusMeters().toInt()} meters."
                },
                Toast.LENGTH_LONG,
            ).show()
            return
        }

        updateHeader()
        renderSelectedPlace()

        AlertDialog.Builder(this)
            .setTitle(UiLanguageStore.pick(this, "Мястото е открито", "Place discovered"))
            .setMessage(
                if (UiLanguageStore.isBulgarian(this)) {
                    "${place.title}\n\n${place.historicalInfo}\n\nСпечелени точки: ${result.pointsAwarded}" +
                        if (result.dailyBonusAwarded) "\nПолучен е бонус от дневното предизвикателство." else ""
                } else {
                    "${place.title}\n\n${place.historicalInfo}\n\nPoints earned: ${result.pointsAwarded}" +
                        if (result.dailyBonusAwarded) "\nDaily challenge bonus unlocked." else ""
                },
            )
            .setPositiveButton(UiLanguageStore.pick(this, "Супер", "Great")) { _, _ -> }
            .show()
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(packageManager) != null) {
            cameraLauncher.launch(intent)
        } else {
            Toast.makeText(this, UiLanguageStore.pick(this, "Не е намерено приложение за камера на това устройство.", "No camera app found on this device."), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 11 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            requestLocationAndRefresh()
        }
    }

    private fun applyLanguage() {
        findViewById<TextView>(R.id.tvExploreSection).text = UiLanguageStore.pick(this, "Карта", "Explore")
        btnDiscover.text = UiLanguageStore.pick(this, "Открий това място", "Discover This Place")
        btnCamera.text = UiLanguageStore.pick(this, "Камера", "Camera")
        btnDaily.text = UiLanguageStore.pick(this, "Днешна задача", "Today's Task")
        AppNavigation.bind(this, findViewById<BottomNavigationView>(R.id.bottomNav), R.id.nav_explore)
    }
}
