package com.example.game_vol1

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.game_vol1.data.GameRepository
import com.example.game_vol1.models.HeritagePlace
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions

class ExplorerMapActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var googleMap: GoogleMap? = null
    private var currentLocation: Location? = null
    private var selectedPlace: HeritagePlace? by mutableStateOf(null)
    private var distanceText by mutableStateOf("Distance unavailable until GPS locks in.")
    private var headerText by mutableStateOf("")
    private var discoveryDialog by mutableStateOf<String?>(null)
    private val markerPlaceMap = mutableMapOf<Marker, HeritagePlace>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        updateHeader()
        setContent {
            ExploreScreen(
                activity = this,
                headerText = headerText,
                selectedPlace = selectedPlace,
                distanceText = distanceText,
                discoveryDialog = discoveryDialog,
                onDismissDialog = { discoveryDialog = null },
                onDiscover = ::discoverSelectedPlace,
                onCamera = ::openArCamera,
                onDaily = { startActivity(Intent(this, GoalsActivity::class.java)) },
                onImageReady = { imageView, place -> PlaceImageLoader.loadInto(imageView, place) },
            )
        }
    }

    override fun onResume() {
        super.onResume()
        updateHeader()
        requestLocationAndRefresh()
        renderSelectedPlace()
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        map.uiSettings.isMapToolbarEnabled = false
        map.uiSettings.isZoomControlsEnabled = true
        map.uiSettings.isZoomGesturesEnabled = true
        map.uiSettings.isScrollGesturesEnabled = true
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(42.7339, 25.4858), 6.2f))
        map.setOnMarkerClickListener { marker ->
            selectedPlace = markerPlaceMap[marker]
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
            val marker = map.addMarker(MarkerOptions().position(LatLng(place.latitude, place.longitude)).title(place.title))
            if (marker != null) markerPlaceMap[marker] = place
        }
    }

    private fun requestLocationAndRefresh() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            googleMap?.isMyLocationEnabled = true
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    currentLocation = location
                    googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(location.latitude, location.longitude), 11.5f))
                }
                updateHeader()
                renderSelectedPlace()
            }
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), REQUEST_LOCATION)
        }
    }

    private fun updateHeader() {
        val profile = GameRepository.loadProfile(this)
        val level = (profile.visitedCount / 3) + 1
        val gpsStatus = if (currentLocation == null) "Waiting for GPS" else "GPS ready"
        headerText = "${profile.username} | Level $level | ${profile.totalScore} pts | $gpsStatus"
    }

    private fun renderSelectedPlace() {
        val place = selectedPlace
        val location = currentLocation
        distanceText = when {
            place == null -> "Tap a map marker, then get within range to earn points."
            location == null -> "Distance unavailable until GPS locks in."
            else -> {
                val distance = GameRepository.distanceMeters(location.latitude, location.longitude, place)
                "Distance: ${distance.toInt()} m | ${if (GameRepository.canDiscover(distance)) "You can discover!" else "Move closer"}"
            }
        }
    }

    private fun discoverSelectedPlace() {
        val place = selectedPlace
        val location = currentLocation
        if (place == null) {
            Toast.makeText(this, "Choose a place marker first.", Toast.LENGTH_SHORT).show()
            return
        }
        if (location == null) {
            Toast.makeText(this, "Waiting for your GPS location.", Toast.LENGTH_SHORT).show()
            return
        }

        val result = GameRepository.discoverPlace(this, place, location.latitude, location.longitude)
        if (!result.success) {
            Toast.makeText(this, "You are ${result.distanceMeters.toInt()} m away. Move within ${GameRepository.discoveryRadiusMeters().toInt()} meters.", Toast.LENGTH_LONG).show()
            return
        }

        updateHeader()
        renderSelectedPlace()
        discoveryDialog = "${place.title}\n\n${place.historicalInfo}\n\nPoints earned: ${result.pointsAwarded}" +
            if (result.dailyBonusAwarded) "\nDaily challenge bonus unlocked!" else ""
    }

    private fun openArCamera() {
        val place = selectedPlace
        if (place == null) {
            Toast.makeText(this, "Choose a place marker first.", Toast.LENGTH_SHORT).show()
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
        if (requestCode == REQUEST_LOCATION && grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) {
            requestLocationAndRefresh()
        }
    }

    companion object {
        private const val REQUEST_LOCATION = 11
    }
}

@Composable
private fun ExploreScreen(
    activity: ExplorerMapActivity,
    headerText: String,
    selectedPlace: HeritagePlace?,
    distanceText: String,
    discoveryDialog: String?,
    onDismissDialog: () -> Unit,
    onDiscover: () -> Unit,
    onCamera: () -> Unit,
    onDaily: () -> Unit,
    onImageReady: (ImageView, HeritagePlace) -> Unit,
) {
    val mapView = rememberComposeMapView(activity)

    HuntTheme {
        HuntScaffold(activity = activity, selected = "map") { modifier ->
            Box(modifier = modifier) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { mapView },
                )

                HuntPanel(modifier = Modifier.align(Alignment.TopCenter).padding(12.dp), accent = HuntColors.Blue) {
                    Text("Explore", color = HuntColors.BlueSoft, fontWeight = FontWeight.Black, fontSize = 13.sp)
                    Text(headerText, color = HuntColors.Text, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }

                Column(
                    modifier = Modifier.align(Alignment.BottomCenter).padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    if (selectedPlace == null) {
                        HuntPanel(accent = HuntColors.Green) {
                            Text("Nearby Monuments", color = HuntColors.Text, fontWeight = FontWeight.Black, fontSize = 18.sp)
                            Text(distanceText, color = HuntColors.Muted)
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                HuntButton("AR Camera", onCamera, Modifier.weight(1f), HuntColors.Blue)
                                HuntButton("Daily Task", onDaily, Modifier.weight(1f), HuntColors.Gold)
                            }
                        }
                    } else {
                        HuntPanel(accent = HuntColors.Green) {
                            AndroidView(
                                modifier = Modifier.fillMaxWidth().height(120.dp),
                                factory = { context -> ImageView(context).apply { scaleType = ImageView.ScaleType.CENTER_CROP } },
                                update = { onImageReady(it, selectedPlace) },
                            )
                            Text("${selectedPlace.title} | ${selectedPlace.city}", color = HuntColors.Text, fontWeight = FontWeight.Black, fontSize = 18.sp)
                            Text(distanceText, color = HuntColors.Gold, fontWeight = FontWeight.Bold)
                            Text("${selectedPlace.shortDescription}\n\n${selectedPlace.historicalInfo}", color = HuntColors.Muted, maxLines = 4, overflow = TextOverflow.Ellipsis)
                            HuntButton("Discover This Place", onDiscover)
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                HuntButton("AR Camera", onCamera, Modifier.weight(1f), HuntColors.Blue)
                                HuntButton("Daily Task", onDaily, Modifier.weight(1f), HuntColors.Gold)
                            }
                        }
                    }
                }
            }
        }
    }

    if (discoveryDialog != null) {
        AlertDialog(
            onDismissRequest = onDismissDialog,
            title = { Text("Place discovered!") },
            text = { Text(discoveryDialog) },
            confirmButton = { HuntButton("Awesome!", onDismissDialog) },
        )
    }
}

@Composable
private fun rememberComposeMapView(callback: OnMapReadyCallback): MapView {
    val context = LocalContext.current
    val mapView = remember {
        MapView(context).apply {
            onCreate(Bundle())
            getMapAsync(callback)
        }
    }

    DisposableEffect(mapView) {
        mapView.onStart()
        mapView.onResume()
        onDispose {
            mapView.onPause()
            mapView.onStop()
            mapView.onDestroy()
        }
    }
    return mapView
}
