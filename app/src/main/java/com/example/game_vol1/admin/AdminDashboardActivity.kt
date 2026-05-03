package com.example.game_vol1.admin

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import com.example.game_vol1.GeoLoginActivity
import com.example.game_vol1.HuntAction
import com.example.game_vol1.HuntColors
import com.example.game_vol1.admin.viewmodel.AdminDashboardViewModel

class AdminDashboardActivity : AppCompatActivity() {
    private val vm: AdminDashboardViewModel by viewModels()
    private var playerCount by mutableIntStateOf(0)
    private var locationCount by mutableIntStateOf(0)
    private var gameCount by mutableIntStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!AdminAccessManager.enforceAdminOrRedirect(this)) return
        vm.playerCount.observe(this) { playerCount = it ?: 0 }
        vm.geoLocationCount.observe(this) { locationCount = it ?: 0 }
        vm.totalGamesCount.observe(this) { gameCount = it ?: 0 }
        setContent {
            AdminDashboardScreen(
                playerCount,
                locationCount,
                gameCount,
                onAddPlace = { startActivity(Intent(this, AddGeoLocationActivity::class.java)) },
                onManagePlaces = { startActivity(Intent(this, ManageGeoLocationsActivity::class.java)) },
                onPlayers = { startActivity(Intent(this, ViewPlayersActivity::class.java)) },
                onLogout = {
                    AdminAccessManager.logout(this)
                    startActivity(Intent(this, GeoLoginActivity::class.java))
                    finishAffinity()
                },
            )
        }
    }
}

@Composable
private fun AdminDashboardScreen(
    players: Int,
    places: Int,
    games: Int,
    onAddPlace: () -> Unit,
    onManagePlaces: () -> Unit,
    onPlayers: () -> Unit,
    onLogout: () -> Unit,
) {
    AdminScrollScreen("Admin Dashboard", "Tune the game world without leaving the app.") {
        AdminMetricRow("Players" to players.toString(), "Places" to places.toString(), "Games" to games.toString())
        HuntAction("Add Place", "Create a new playable location.", HuntColors.Green, onAddPlace)
        HuntAction("Manage Places", "Edit coordinates, difficulty, photos, and descriptions.", HuntColors.Blue, onManagePlaces)
        HuntAction("Players", "Review player progress and game history.", HuntColors.Gold, onPlayers)
        HuntAction("Log Out", "Return to the player login screen.", HuntColors.Rose, onLogout)
    }
}
