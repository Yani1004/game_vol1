package com.example.game_vol1.admin

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.game_vol1.HuntButton
import com.example.game_vol1.HuntColors
import com.example.game_vol1.HuntField
import com.example.game_vol1.HuntPanel
import com.example.game_vol1.HuntScaffold
import com.example.game_vol1.HuntTheme
import com.example.game_vol1.HuntTitle
import com.example.game_vol1.admin.viewmodel.GeoLocationViewModel
import com.example.game_vol1.database.entity.GeoLocationEntity

class ManageGeoLocationsActivity : AppCompatActivity() {
    private val vm: GeoLocationViewModel by viewModels()
    private val geoLocations = mutableStateListOf<GeoLocationEntity>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!AdminAccessManager.enforceAdminOrRedirect(this)) return
        vm.geoLocations.observe(this) {
            geoLocations.clear()
            geoLocations.addAll(it)
        }
        vm.operationResult.observe(this) { Toast.makeText(this, it, Toast.LENGTH_SHORT).show() }
        vm.operationError.observe(this) { Toast.makeText(this, it, Toast.LENGTH_LONG).show() }
        setContent {
            ManageGeoLocationsScreen(geoLocations, vm::search, vm::filterByDifficulty, { startActivity(Intent(this, AddGeoLocationActivity::class.java)) }, ::openEdit, vm::delete, ::finish)
        }
    }

    private fun openEdit(geo: GeoLocationEntity) {
        startActivity(Intent(this, AddGeoLocationActivity::class.java).apply {
            putExtra(AddGeoLocationActivity.EXTRA_EDIT_ID, geo.id)
            putExtra(AddGeoLocationActivity.EXTRA_NAME, geo.name)
            putExtra(AddGeoLocationActivity.EXTRA_COUNTRY, geo.country)
            putExtra(AddGeoLocationActivity.EXTRA_CITY, geo.city)
            putExtra(AddGeoLocationActivity.EXTRA_LAT, geo.latitude.toString())
            putExtra(AddGeoLocationActivity.EXTRA_LNG, geo.longitude.toString())
            putExtra(AddGeoLocationActivity.EXTRA_DIFFICULTY, geo.difficulty)
            putExtra(AddGeoLocationActivity.EXTRA_IMAGE_URL, geo.imageUrl)
            putExtra(AddGeoLocationActivity.EXTRA_DESCRIPTION, geo.description)
        })
    }
}

@Composable
private fun ManageGeoLocationsScreen(
    geoLocations: List<GeoLocationEntity>,
    onSearch: (String) -> Unit,
    onDifficulty: (String?) -> Unit,
    onAdd: () -> Unit,
    onEdit: (GeoLocationEntity) -> Unit,
    onDelete: (GeoLocationEntity) -> Unit,
    onBack: () -> Unit,
) {
    var query by remember { mutableStateOf("") }
    var difficulty by remember { mutableStateOf<String?>(null) }
    var pendingDelete by remember { mutableStateOf<GeoLocationEntity?>(null) }

    HuntTheme {
        HuntScaffold { modifier ->
            Column(modifier = modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                HuntButton("Back", onBack, color = HuntColors.SlateLight)
                HuntTitle("Manage Places", "Search, filter, edit, or remove playable locations.")
                HuntPanel(accent = HuntColors.Blue) {
                    HuntField(query, {
                        query = it
                        difficulty = null
                        onSearch(it)
                    }, "Search places")
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        listOf(null, "Easy", "Medium", "Hard").forEach { option ->
                            HuntButton(option ?: "All", {
                                query = ""
                                difficulty = option
                                onDifficulty(option)
                            }, Modifier.weight(1f), if (difficulty == option) HuntColors.Gold else HuntColors.SlateLight)
                        }
                    }
                    HuntButton("Add Place", onAdd, color = HuntColors.Green)
                }
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f)) {
                    items(geoLocations, key = { it.id }) { geo ->
                        HuntPanel(accent = if (geo.difficulty == "Hard") HuntColors.Rose else if (geo.difficulty == "Medium") HuntColors.Gold else HuntColors.Green) {
                            Text(geo.name, color = HuntColors.Text, fontWeight = FontWeight.Black)
                            Text("${geo.city}, ${geo.country} | ${geo.difficulty}", color = HuntColors.Muted)
                            Text("${geo.latitude}, ${geo.longitude}", color = HuntColors.BlueSoft)
                            if (geo.description.isNotBlank()) Text(geo.description, color = HuntColors.Muted, maxLines = 2, overflow = TextOverflow.Ellipsis)
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                HuntButton("Edit", { onEdit(geo) }, Modifier.weight(1f), HuntColors.Blue)
                                HuntButton("Delete", { pendingDelete = geo }, Modifier.weight(1f), HuntColors.Rose)
                            }
                        }
                    }
                }
            }
        }
    }

    pendingDelete?.let { geo ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Delete Location") },
            text = { Text("Remove \"${geo.name}\" from the game?") },
            confirmButton = { HuntButton("Delete", { pendingDelete = null; onDelete(geo) }, color = HuntColors.Rose) },
            dismissButton = { HuntButton("Cancel", { pendingDelete = null }, color = HuntColors.SlateLight) },
        )
    }
}
