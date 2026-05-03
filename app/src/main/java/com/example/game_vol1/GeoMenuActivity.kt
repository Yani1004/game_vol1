package com.example.game_vol1

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.game_vol1.admin.AdminAccessManager
import com.example.game_vol1.admin.AdminDashboardActivity
import com.example.game_vol1.data.GameRepository
import com.example.game_vol1.models.DailyChallenge
import com.example.game_vol1.models.PlayerProfile
import com.example.game_vol1.models.TeamInfo

class GeoMenuActivity : AppCompatActivity() {
    private var profile by mutableStateOf<PlayerProfile?>(null)
    private var daily by mutableStateOf<DailyChallenge?>(null)
    private var team by mutableStateOf<TeamInfo?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupNotifications()
        refresh()
        setContent {
            HomeScreen(
                activity = this,
                profile = profile,
                daily = daily,
                team = team,
                isAdmin = AdminAccessManager.isAdmin(this),
                onExplore = { startActivity(Intent(this, ExplorerMapActivity::class.java)) },
                onGoals = { startActivity(Intent(this, GoalsActivity::class.java)) },
                onHistory = { startActivity(Intent(this, DiscoveriesActivity::class.java)) },
                onTeam = { startActivity(Intent(this, TeamActivity::class.java)) },
                onAdmin = { startActivity(Intent(this, AdminDashboardActivity::class.java)) },
                onLogout = {
                    GameRepository.logout(this)
                    startActivity(Intent(this, GeoLoginActivity::class.java))
                    finishAffinity()
                },
            )
        }
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    private fun refresh() {
        profile = GameRepository.loadProfile(this)
        daily = GameRepository.getDailyChallenge()
        team = GameRepository.loadTeam(this)
    }

    private fun setupNotifications() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            NotificationScheduler.ensureDailyReminderScheduled(this)
            return
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            NotificationScheduler.ensureDailyReminderScheduled(this)
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), NOTIFICATION_PERMISSION_REQUEST)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == NOTIFICATION_PERMISSION_REQUEST && grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) {
            NotificationScheduler.ensureDailyReminderScheduled(this)
        }
    }

    companion object {
        private const val NOTIFICATION_PERMISSION_REQUEST = 301
    }
}

@Composable
private fun HomeScreen(
    activity: GeoMenuActivity,
    profile: PlayerProfile?,
    daily: DailyChallenge?,
    team: TeamInfo?,
    isAdmin: Boolean,
    onExplore: () -> Unit,
    onGoals: () -> Unit,
    onHistory: () -> Unit,
    onTeam: () -> Unit,
    onAdmin: () -> Unit,
    onLogout: () -> Unit,
) {
    HuntTheme {
        HuntScaffold(activity = activity, selected = "profile") { modifier ->
            Column(
                modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                HuntTitle(profile?.username ?: "Explorer", "Your base for landmarks, teams, and daily goals.")
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    HuntMetric("Visited", profile?.visitedCount?.toString() ?: "0", Modifier.weight(1f), HuntColors.Green)
                    HuntMetric("Score", profile?.totalScore?.toString() ?: "0", Modifier.weight(1f), HuntColors.Gold)
                }
                HuntPanel(accent = HuntColors.Blue) {
                    Text("Today's Route", color = HuntColors.Text, fontWeight = FontWeight.Black)
                    Text(daily?.place?.title ?: "Loading daily place", color = HuntColors.BlueSoft, fontWeight = FontWeight.Bold)
                    Text("Discover it today for +${daily?.bonusPoints ?: 0} bonus points.", color = HuntColors.Muted)
                    HuntButton("Jump Into Explore", onExplore, color = HuntColors.Blue)
                }
                HuntAction("History", "${profile?.visitedCount ?: 0} places discovered", HuntColors.Rose, onHistory)
                HuntAction("Daily", "Track the challenge board.", HuntColors.Gold, onGoals)
                HuntAction("Team Play", team?.takeIf { it.hasTeam }?.let { "${it.teamName} | ${it.memberNames.size} members" } ?: "Create or join a team.", HuntColors.Green, onTeam)
                if (isAdmin) {
                    HuntAction("Admin Panel", "Manage players and game locations.", HuntColors.Blue, onAdmin)
                }
                HuntButton("Log Out", onLogout, color = HuntColors.SlateLight)
            }
        }
    }
}
