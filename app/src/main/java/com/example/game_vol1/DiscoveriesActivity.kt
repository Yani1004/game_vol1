package com.example.game_vol1

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.game_vol1.data.GameRepository
import com.example.game_vol1.data.MultiplayerRepository
import com.example.game_vol1.models.PlaceVisit
import com.google.firebase.firestore.ListenerRegistration

class DiscoveriesActivity : AppCompatActivity() {
    private var leaderboardListener: ListenerRegistration? = null
    private var visits by mutableStateOf<List<PlaceVisit>>(emptyList())
    private var cloudLeaderboard by mutableStateOf<List<MultiplayerRepository.LeaderboardEntry>>(emptyList())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        refresh()
        setContent { DiscoveriesScreen(this, visits, cloudLeaderboard) }
        leaderboardListener = MultiplayerRepository.observeGlobalLeaderboard(this) { entries ->
            runOnUiThread { cloudLeaderboard = entries }
        }
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    override fun onDestroy() {
        leaderboardListener?.remove()
        leaderboardListener = null
        super.onDestroy()
    }

    private fun refresh() {
        visits = GameRepository.loadVisits(this)
    }
}

@Composable
private fun DiscoveriesScreen(
    activity: DiscoveriesActivity,
    visits: List<PlaceVisit>,
    leaderboard: List<MultiplayerRepository.LeaderboardEntry>,
) {
    HuntTheme {
        HuntScaffold(activity = activity, selected = "history") { modifier ->
            LazyColumn(
                modifier = modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    HuntTitle("Places You've Visited", "Journey stats, discoveries, and leaderboard.")
                }
                item {
                    HuntPanel(accent = HuntColors.Blue) {
                        Text("Journey Stats", color = HuntColors.Text, fontWeight = FontWeight.Black)
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            HuntMetric("Places", visits.size.toString(), Modifier.weight(1f), HuntColors.Green)
                            HuntMetric("Points", visits.sumOf { it.pointsEarned }.toString(), Modifier.weight(1f), HuntColors.Gold)
                        }
                    }
                }
                item {
                    HuntPanel(accent = HuntColors.Gold) {
                        Text(if (MultiplayerRepository.isAvailable(activity)) "Live Global Leaderboard" else "Local Leaderboard", color = HuntColors.Text, fontWeight = FontWeight.Black)
                        if (leaderboard.isEmpty()) {
                            Text("No cloud scores yet. Discover a place on two devices to populate this list.", color = HuntColors.Muted)
                        } else {
                            leaderboard.forEach { entry ->
                                Text("${entry.rank}. ${entry.username} - ${entry.totalScore} pts (${entry.visitedCount} places)", color = HuntColors.Muted)
                            }
                        }
                    }
                }
                if (visits.isEmpty()) {
                    item {
                        HuntPanel(accent = HuntColors.Rose) {
                            Text("You have not discovered any places yet.", color = HuntColors.Text, fontWeight = FontWeight.Black)
                            Text("Open the map, get near a landmark, and tap Discover.", color = HuntColors.Muted)
                        }
                    }
                } else {
                    items(visits, key = { "${it.placeId}-${it.visitedAtEpochMs}" }) { visit ->
                        val place = GameRepository.placeById(visit.placeId)
                        HuntPanel(accent = HuntColors.Green) {
                            Text(place?.title ?: "Unknown place", color = HuntColors.Text, fontWeight = FontWeight.Black)
                            Text(place?.let { "${it.city}, ${it.country}" }.orEmpty(), color = HuntColors.BlueSoft)
                            Text("Visited: ${GameRepository.formatVisitTime(visit.visitedAtEpochMs)} | ${visit.pointsEarned} pts", color = HuntColors.Gold)
                            if (place != null) Text(place.historicalInfo, color = HuntColors.Muted)
                        }
                    }
                }
            }
        }
    }
}
