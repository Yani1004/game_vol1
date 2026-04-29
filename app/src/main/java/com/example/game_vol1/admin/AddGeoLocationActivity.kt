package com.example.game_vol1.admin

import android.os.Bundle
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.game_vol1.R
import com.example.game_vol1.admin.viewmodel.GeoLocationViewModel
import com.example.game_vol1.database.entity.GeoLocationEntity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class AddGeoLocationActivity : AppCompatActivity() {

    private val vm: GeoLocationViewModel by viewModels()

    private lateinit var tilName: TextInputLayout
    private lateinit var tilCountry: TextInputLayout
    private lateinit var tilCity: TextInputLayout
    private lateinit var tilLat: TextInputLayout
    private lateinit var tilLng: TextInputLayout
    private lateinit var tilDifficulty: TextInputLayout
    private lateinit var tilImageUrl: TextInputLayout
    private lateinit var tilDescription: TextInputLayout

    private lateinit var etName: TextInputEditText
    private lateinit var etCountry: TextInputEditText
    private lateinit var etCity: TextInputEditText
    private lateinit var etLat: TextInputEditText
    private lateinit var etLng: TextInputEditText
    private lateinit var etImageUrl: TextInputEditText
    private lateinit var etDescription: TextInputEditText
    private lateinit var actvDifficulty: AutoCompleteTextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!AdminAccessManager.enforceAdminOrRedirect(this)) return
        setContentView(R.layout.activity_add_geolocation)

        val toolbar = findViewById<Toolbar>(R.id.addGeoToolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        bindViews()

        actvDifficulty.setAdapter(
            ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, listOf("Easy", "Medium", "Hard"))
        )

        // Pre-fill if editing
        val editId = intent.getIntExtra(EXTRA_EDIT_ID, -1)
        if (editId != -1) {
            supportActionBar?.title = "Edit Geolocation"
            etName.setText(intent.getStringExtra(EXTRA_NAME))
            etCountry.setText(intent.getStringExtra(EXTRA_COUNTRY))
            etCity.setText(intent.getStringExtra(EXTRA_CITY))
            etLat.setText(intent.getStringExtra(EXTRA_LAT))
            etLng.setText(intent.getStringExtra(EXTRA_LNG))
            actvDifficulty.setText(intent.getStringExtra(EXTRA_DIFFICULTY), false)
            etImageUrl.setText(intent.getStringExtra(EXTRA_IMAGE_URL))
            etDescription.setText(intent.getStringExtra(EXTRA_DESCRIPTION))
        } else {
            supportActionBar?.title = "Add Geolocation"
        }

        vm.operationResult.observe(this) { msg ->
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
            if (msg.contains("added") || msg.contains("updated")) finish()
        }
        vm.operationError.observe(this) { msg ->
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
        }

        findViewById<MaterialButton>(R.id.btnSaveGeo).setOnClickListener {
            if (!validate()) return@setOnClickListener
            val geo = GeoLocationEntity(
                id = if (editId != -1) editId else 0,
                name = etName.text.toString().trim(),
                country = etCountry.text.toString().trim(),
                city = etCity.text.toString().trim(),
                latitude = etLat.text.toString().toDouble(),
                longitude = etLng.text.toString().toDouble(),
                difficulty = actvDifficulty.text.toString(),
                imageUrl = etImageUrl.text.toString().trim(),
                description = etDescription.text.toString().trim()
            )
            if (editId != -1) vm.update(geo) else vm.add(geo)
        }

        findViewById<MaterialButton>(R.id.btnBackAccessible).setOnClickListener { finish() }
    }

    private fun bindViews() {
        tilName = findViewById(R.id.tilGeoName)
        tilCountry = findViewById(R.id.tilGeoCountry)
        tilCity = findViewById(R.id.tilGeoCity)
        tilLat = findViewById(R.id.tilGeoLat)
        tilLng = findViewById(R.id.tilGeoLng)
        tilDifficulty = findViewById(R.id.tilGeoDifficulty)
        tilImageUrl = findViewById(R.id.tilGeoImageUrl)
        tilDescription = findViewById(R.id.tilGeoDescription)
        etName = findViewById(R.id.etGeoName)
        etCountry = findViewById(R.id.etGeoCountry)
        etCity = findViewById(R.id.etGeoCity)
        etLat = findViewById(R.id.etGeoLat)
        etLng = findViewById(R.id.etGeoLng)
        etImageUrl = findViewById(R.id.etGeoImageUrl)
        etDescription = findViewById(R.id.etGeoDescription)
        actvDifficulty = findViewById(R.id.actvGeoDifficulty)
    }

    private fun validate(): Boolean {
        var ok = true
        fun req(til: TextInputLayout, et: TextInputEditText, msg: String) {
            if (et.text.isNullOrBlank()) { til.error = msg; ok = false } else til.error = null
        }
        req(tilName, etName, "Location name is required")
        req(tilCountry, etCountry, "Country is required")
        req(tilCity, etCity, "City / Region is required")

        val lat = etLat.text.toString().toDoubleOrNull()
        if (lat == null || lat < -90 || lat > 90) { tilLat.error = "Valid latitude: -90 to 90"; ok = false } else tilLat.error = null

        val lng = etLng.text.toString().toDoubleOrNull()
        if (lng == null || lng < -180 || lng > 180) { tilLng.error = "Valid longitude: -180 to 180"; ok = false } else tilLng.error = null

        if (actvDifficulty.text.isNullOrBlank()) { tilDifficulty.error = "Select a difficulty"; ok = false } else tilDifficulty.error = null

        return ok
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) { finish(); return true }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        const val EXTRA_EDIT_ID = "edit_id"
        const val EXTRA_NAME = "name"
        const val EXTRA_COUNTRY = "country"
        const val EXTRA_CITY = "city"
        const val EXTRA_LAT = "lat"
        const val EXTRA_LNG = "lng"
        const val EXTRA_DIFFICULTY = "difficulty"
        const val EXTRA_IMAGE_URL = "image_url"
        const val EXTRA_DESCRIPTION = "description"
    }
}
