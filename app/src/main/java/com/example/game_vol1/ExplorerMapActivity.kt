package com.example.game_vol1

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
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
    private lateinit var ivPlacePhoto: ImageView
    private lateinit var btnDiscover: Button
    private lateinit var btnCamera: Button
    private lateinit var btnDaily: Button
    private lateinit var btnCameraEmpty: Button
    private lateinit var btnDailyEmpty: Button
    private lateinit var bottomPlaceCard: LinearLayout
    private lateinit var noPlaceButtons: LinearLayout

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var googleMap: GoogleMap? = null
    private var currentLocation: Location? = null
    private var selectedPlace: HeritagePlace? = null
    private val markerPlaceMap = mutableMapOf<Marker, HeritagePlace>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.geo_activity_explore)

        tvExplorerStatus = findViewById(R.id.tvExplorerStatus)
        tvSelectedPlace = findViewById(R.id.tvSelectedPlace)
        tvDistance = findViewById(R.id.tvDistance)
        tvPlaceInfo = findViewById(R.id.tvPlaceInfo)
        ivPlacePhoto = findViewById(R.id.ivPlacePhoto)
        btnDiscover = findViewById(R.id.btnDiscoverPlace)
        btnCamera = findViewById(R.id.btnOpenCamera)
        btnDaily = findViewById(R.id.btnOpenDaily)
        btnCameraEmpty = findViewById(R.id.btnOpenCameraEmpty)
        btnDailyEmpty = findViewById(R.id.btnOpenDailyEmpty)
        bottomPlaceCard = findViewById(R.id.bottomPlaceCard)
        noPlaceButtons = findViewById(R.id.noPlaceButtons)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        btnDiscover.setOnClickListener { discoverSelectedPlace() }
        btnCamera.setOnClickListener { openArCamera() }
        btnDaily.setOnClickListener { startActivity(Intent(this, GoalsActivity::class.java)) }
        btnCameraEmpty.setOnClickListener { openArCamera() }
        btnDailyEmpty.setOnClickListener { startActivity(Intent(this, GoalsActivity::class.java)) }
        listOf(btnDiscover, btnCamera, btnDaily, btnCameraEmpty, btnDailyEmpty).forEach { it.applyPressFeedback() }
        noPlaceButtons.fadeSlideIn(80L)

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
        requestLocationAndRefresh()
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        map.uiSettings.isMapToolbarEnabled = false
        map.uiSettings.isZoomControlsEnabled = true
        map.uiSettings.isZoomGesturesEnabled = true
        map.uiSettings.isScrollGesturesEnabled = true
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
            if (marker != null) markerPlaceMap[marker] = place
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
                REQUEST_LOCATION,
            )
        }
    }

    private fun updateHeader() {
        val profile = GameRepository.loadProfile(this)
        val level = (profile.visitedCount / 3) + 1
        val gpsStatus = UiLanguageStore.pick(
            this,
            if (currentLocation == null) "чака GPS" else "GPS готов",
            if (currentLocation == null) "Waiting for GPS" else "GPS ready",
        )
        val pointsLabel = UiLanguageStore.pick(this, "точки", "pts")
        val levelLabel = UiLanguageStore.pick(this, "Ниво", "Level")
        tvExplorerStatus.text = "${profile.username} | $levelLabel $level | ${profile.totalScore} $pointsLabel | $gpsStatus"
    }
    private fun renderSelectedPlace() {
        val place = selectedPlace
        if (place == null) {
            bottomPlaceCard.visibility = View.GONE
            noPlaceButtons.visibility = View.VISIBLE
            return
        }

        bottomPlaceCard.visibility = View.VISIBLE
        noPlaceButtons.visibility = View.GONE
        tvSelectedPlace.text = "${place.title} | ${place.city}"
        tvPlaceInfo.text = "${place.shortDescription}\n\n${place.historicalInfo}"
        PlaceImageLoader.loadInto(ivPlacePhoto, place)

        val current = currentLocation
        if (current == null) {
            tvDistance.text = UiLanguageStore.pick(
                this,
                "Разстоянието ще се покаже след GPS локализация.",
                "Distance unavailable until GPS locks in.",
            )
            return
        }

        val distance = GameRepository.distanceMeters(current.latitude, current.longitude, place)
        tvDistance.text = if (UiLanguageStore.isBulgarian(this)) {
            "Разстояние: ${distance.toInt()} м | ${
                if (GameRepository.canDiscover(distance)) "Можеш да откриеш!" else "Приближи се"
            }"
        } else {
            "Distance: ${distance.toInt()} m | ${
                if (GameRepository.canDiscover(distance)) "You can discover!" else "Move closer"
            }"
        }
    }

    private fun discoverSelectedPlace() {
        val place = selectedPlace
        val location = currentLocation
        if (place == null) {
            Toast.makeText(
                this,
                UiLanguageStore.pick(this, "Първо избери обект от картата.", "Choose a place marker first."),
                Toast.LENGTH_SHORT,
            ).show()
            return
        }
        if (location == null) {
            Toast.makeText(
                this,
                UiLanguageStore.pick(this, "Изчаква се твоето GPS местоположение.", "Waiting for your GPS location."),
                Toast.LENGTH_SHORT,
            ).show()
            return
        }

        val result = GameRepository.discoverPlace(this, place, location.latitude, location.longitude)
        if (!result.success) {
            Toast.makeText(
                this,
                if (UiLanguageStore.isBulgarian(this)) {
                    "Намираш се на ${result.distanceMeters.toInt()} м. Приближи се в радиус от ${
                        GameRepository.discoveryRadiusMeters().toInt()
                    } м."
                } else {
                    "You are ${result.distanceMeters.toInt()} m away. Move within ${
                        GameRepository.discoveryRadiusMeters().toInt()
                    } meters."
                },
                Toast.LENGTH_LONG,
            ).show()
            return
        }

        updateHeader()
        renderSelectedPlace()

        AlertDialog.Builder(this)
            .setTitle(UiLanguageStore.pick(this, "Мястото е открито!", "Place discovered!"))
            .setMessage(
                if (UiLanguageStore.isBulgarian(this)) {
                    "${place.title}\n\n${place.historicalInfo}\n\nСпечелени точки: ${result.pointsAwarded}" +
                            if (result.dailyBonusAwarded) "\nПолучен е бонус от дневното предизвикателство!" else ""
                } else {
                    "${place.title}\n\n${place.historicalInfo}\n\nPoints earned: ${result.pointsAwarded}" +
                            if (result.dailyBonusAwarded) "\nDaily challenge bonus unlocked!" else ""
                },
            )
            .setPositiveButton(UiLanguageStore.pick(this, "Супер!", "Awesome!")) { _, _ -> }
            .show()
    }

    private fun openArCamera() {
        val place = selectedPlace
        if (place == null) {
            Toast.makeText(
                this,
                UiLanguageStore.pick(this, "Първо избери обект от картата.", "Choose a place marker first."),
                Toast.LENGTH_SHORT,
            ).show()
            return
        }
        val location = currentLocation
        startActivity(
            Intent(this, ArDemoActivity::class.java).apply {
                putExtra(ArDemoActivity.EXTRA_PLACE_TITLE, place.title)
                putExtra(ArDemoActivity.EXTRA_PLACE_CITY, place.city)
                putExtra(ArDemoActivity.EXTRA_PLACE_ID, place.id)
                putExtra(ArDemoActivity.EXTRA_PLACE_LATITUDE, place.latitude)
                putExtra(ArDemoActivity.EXTRA_PLACE_LONGITUDE, place.longitude)
                if (location != null) {
                    putExtra(ArDemoActivity.EXTRA_USER_LATITUDE, location.latitude)
                    putExtra(ArDemoActivity.EXTRA_USER_LONGITUDE, location.longitude)
                }
            },
        )
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_LOCATION && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            requestLocationAndRefresh()
        }
    }

    private fun applyLanguage() {
        findViewById<TextView>(R.id.tvExploreSection).text = UiLanguageStore.pick(this, "Карта", "Explore")
        btnDiscover.text = UiLanguageStore.pick(this, "Открий това място", "Discover This Place")
        btnCamera.text = UiLanguageStore.pick(this, "AR камера", "AR Camera")
        btnDaily.text = UiLanguageStore.pick(this, "Днешна задача", "Today's Task")
        btnCameraEmpty.text = UiLanguageStore.pick(this, "AR камера", "AR Camera")
        btnDailyEmpty.text = UiLanguageStore.pick(this, "Днешна задача", "Today's Task")
        AppNavigation.bind(this, findViewById<BottomNavigationView>(R.id.bottomNav), R.id.nav_explore)
    }
    companion object {
        private const val REQUEST_LOCATION = 11
    }
}
