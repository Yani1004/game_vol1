package com.example.game_vol1.admin

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.game_vol1.HuntButton
import com.example.game_vol1.HuntColors
import com.example.game_vol1.HuntField
import com.example.game_vol1.HuntPanel
import com.example.game_vol1.admin.viewmodel.GeoLocationViewModel
import com.example.game_vol1.database.entity.GeoLocationEntity

class AddGeoLocationActivity : AppCompatActivity() {
    private val vm: GeoLocationViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!AdminAccessManager.enforceAdminOrRedirect(this)) return
        val editId = intent.getIntExtra(EXTRA_EDIT_ID, -1)
        vm.operationResult.observe(this) { msg ->
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
            if (msg.contains("added") || msg.contains("updated")) finish()
        }
        vm.operationError.observe(this) { msg -> Toast.makeText(this, msg, Toast.LENGTH_LONG).show() }
        setContent {
            AddGeoLocationScreen(
                editId = editId,
                initialName = intent.getStringExtra(EXTRA_NAME).orEmpty(),
                initialCountry = intent.getStringExtra(EXTRA_COUNTRY).orEmpty(),
                initialCity = intent.getStringExtra(EXTRA_CITY).orEmpty(),
                initialLat = intent.getStringExtra(EXTRA_LAT).orEmpty(),
                initialLng = intent.getStringExtra(EXTRA_LNG).orEmpty(),
                initialDifficulty = intent.getStringExtra(EXTRA_DIFFICULTY) ?: "Easy",
                initialImageUrl = intent.getStringExtra(EXTRA_IMAGE_URL).orEmpty(),
                initialDescription = intent.getStringExtra(EXTRA_DESCRIPTION).orEmpty(),
                onSave = { geo -> if (editId != -1) vm.update(geo) else vm.add(geo) },
                onBack = ::finish,
            )
        }
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

@Composable
private fun AddGeoLocationScreen(
    editId: Int,
    initialName: String,
    initialCountry: String,
    initialCity: String,
    initialLat: String,
    initialLng: String,
    initialDifficulty: String,
    initialImageUrl: String,
    initialDescription: String,
    onSave: (GeoLocationEntity) -> Unit,
    onBack: () -> Unit,
) {
    var name by remember { mutableStateOf(initialName) }
    var country by remember { mutableStateOf(initialCountry) }
    var city by remember { mutableStateOf(initialCity) }
    var lat by remember { mutableStateOf(initialLat) }
    var lng by remember { mutableStateOf(initialLng) }
    var difficulty by remember { mutableStateOf(initialDifficulty.ifBlank { "Easy" }) }
    var imageUrl by remember { mutableStateOf(initialImageUrl) }
    var description by remember { mutableStateOf(initialDescription) }
    var error by remember { mutableStateOf<String?>(null) }

    AdminScrollScreen(if (editId != -1) "Edit Place" else "Add Place", "Build the official list with real coordinates and useful place photos.", onBack) {
        HuntPanel(accent = HuntColors.Green) {
            HuntField(name, { name = it }, "Place name")
            HuntField(country, { country = it }, "Country")
            HuntField(city, { city = it }, "City / region")
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                HuntField(lat, { lat = it }, "Latitude", Modifier.weight(1f), keyboardType = KeyboardType.Decimal)
                HuntField(lng, { lng = it }, "Longitude", Modifier.weight(1f), keyboardType = KeyboardType.Decimal)
            }
            Text("Difficulty", color = HuntColors.Muted)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                listOf("Easy", "Medium", "Hard").forEach { option ->
                    HuntButton(option, { difficulty = option }, Modifier.weight(1f), if (difficulty == option) HuntColors.Gold else HuntColors.SlateLight)
                }
            }
            HuntField(imageUrl, { imageUrl = it }, "Image URL")
            OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth(), minLines = 4)
            if (error != null) Text(error.orEmpty(), color = HuntColors.Rose)
            HuntButton("Save Place", {
                val latitude = lat.toDoubleOrNull()
                val longitude = lng.toDoubleOrNull()
                error = when {
                    name.isBlank() -> "Location name is required."
                    country.isBlank() -> "Country is required."
                    city.isBlank() -> "City / region is required."
                    latitude == null || latitude !in -90.0..90.0 -> "Latitude must be between -90 and 90."
                    longitude == null || longitude !in -180.0..180.0 -> "Longitude must be between -180 and 180."
                    difficulty.isBlank() -> "Choose a difficulty."
                    else -> null
                }
                if (error == null && latitude != null && longitude != null) {
                    onSave(
                        GeoLocationEntity(
                            id = if (editId != -1) editId else 0,
                            name = name.trim(),
                            country = country.trim(),
                            city = city.trim(),
                            latitude = latitude,
                            longitude = longitude,
                            difficulty = difficulty,
                            imageUrl = imageUrl.trim(),
                            description = description.trim(),
                        ),
                    )
                }
            })
        }
    }
}
