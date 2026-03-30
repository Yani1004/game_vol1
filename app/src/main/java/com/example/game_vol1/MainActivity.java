package com.example.game_vol1;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FloatingActionButton btnScanAR;
    private FusedLocationProviderClient fusedLocationClient; // Инструментът за четене на GPS

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    // Координатите на нашия тестов обект (Античен театър Пловдив)
    private final LatLng POI_LOCATION = new LatLng(42.1466, 24.7510);

    // ПРОМЯНА 1: Радиусът за чекиране вече е 15 метра!
    private final int INTERACTION_RADIUS_METERS = 70;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnScanAR = findViewById(R.id.btnScanAR);

        // Инициализираме GPS сензора
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // КОГАТО ИГРАЧЪТ НАТИСНЕ ЖЪЛТИЯ БУТОН ЗА AR:
        btnScanAR.setOnClickListener(v -> {
            checkLocationAndStartAR();
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        enableUserLocation();

        // Добавяме маркера на картата
        mMap.addMarker(new MarkerOptions()
                .position(POI_LOCATION)
                .title("Античен театър Пловдив")
                .snippet("Обектът се владее от Траки"));

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(POI_LOCATION, 14f));
    }

    // --------------------------------------------------------
    // БИЗНЕС ЛОГИКАТА: Проверка на разстоянието
    // --------------------------------------------------------
    private void checkLocationAndStartAR() {
        // 1. Проверяваме дали имаме права за локация
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            // 2. Взимаме последната известна локация на играча
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, userLocation -> {
                if (userLocation != null) {

                    // 3. Изчисляваме разстоянието (Математиката)
                    float[] results = new float[1];
                    Location.distanceBetween(
                            userLocation.getLatitude(), userLocation.getLongitude(), // Координати на играча
                            POI_LOCATION.latitude, POI_LOCATION.longitude,           // Координати на обекта
                            results // Тук Android записва резултата в метри
                    );

                    float distanceInMeters = results[0];

                    // 4. Проверяваме дали играчът е в радиуса от 15 метра
                    if (distanceInMeters <= INTERACTION_RADIUS_METERS) {
                        Toast.makeText(MainActivity.this, "Обектът е открит! Стартиране на AR...", Toast.LENGTH_SHORT).show();

                        // ПРОМЯНА 2: Пускаме го в AR камерата и изпращаме данните!
                        Intent intent = new Intent(MainActivity.this, ARQuestActivity.class);
                        intent.putExtra("POI_ID", "plovdiv_theatre_01");
                        intent.putExtra("ARTIFACT_NAME", "Златна Тракийска Маска");
                        intent.putExtra("ARTIFACT_ICON", "🎭");
                        intent.putExtra("POINTS", 100);

                        startActivity(intent);
                    } else {
                        // ПРОМЯНА 3: Новото, по-ясно съобщение за разстоянието
                        int currentDistance = Math.round(distanceInMeters);
                        Toast.makeText(MainActivity.this,
                                "Ти си на " + currentDistance + " метра от обекта. Трябва да си на максимум " + INTERACTION_RADIUS_METERS + " метра. Приближи се!",
                                Toast.LENGTH_LONG).show();
                    }

                } else {
                    Toast.makeText(MainActivity.this, "Търсене на GPS сигнал... Опитай пак.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(MainActivity.this, "Нямаш права за локация!", Toast.LENGTH_SHORT).show();
        }
    }

    private void enableUserLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }
}