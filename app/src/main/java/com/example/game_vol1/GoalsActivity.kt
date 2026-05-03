package com.example.game_vol1

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import com.example.game_vol1.data.GameRepository
import com.example.game_vol1.models.DailyChallenge
import com.example.game_vol1.models.PlayerProfile

class GoalsActivity : AppCompatActivity() {
    private var profile by mutableStateOf<PlayerProfile?>(null)
    private var challenge by mutableStateOf<DailyChallenge?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        refresh()
        setContent {
            GoalsScreen(this, profile, challenge, GameRepository.getPlaces().size)
        }
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    private fun refresh() {
        profile = GameRepository.loadProfile(this)
        challenge = GameRepository.getDailyChallenge()
    }
}

@Composable
private fun GoalsScreen(
    activity: GoalsActivity,
    profile: PlayerProfile?,
    challenge: DailyChallenge?,
    totalPlaces: Int,
) {
    val completed = profile != null && challenge != null && profile.completedDailyDate == challenge.date
    HuntTheme {
        HuntScaffold(activity = activity, selected = "daily") { modifier ->
            Column(
                modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                HuntTitle("Challenge Board", "Daily objectives and collection progress.")
                HuntPanel(accent = HuntColors.Gold) {
                    Text("Featured Place", color = HuntColors.Text, fontWeight = FontWeight.Black)
                    Text(challenge?.place?.title ?: "Loading...", color = HuntColors.Gold, fontWeight = FontWeight.Bold)
                    Text(challenge?.place?.city.orEmpty(), color = HuntColors.Muted)
                    Text(if (completed) "Today's challenge is completed." else "Discover this place today to claim +${challenge?.bonusPoints ?: 0} bonus.", color = HuntColors.Muted)
                }
                HuntPanel(accent = HuntColors.Green) {
                    Text("Collection Progress", color = HuntColors.Text, fontWeight = FontWeight.Black)
                    Text("${profile?.discoveredPlaceIds?.size ?: 0} / $totalPlaces discovered", color = HuntColors.BlueSoft, fontWeight = FontWeight.Bold)
                    Text("Every discovery adds to your heritage journal.", color = HuntColors.Muted)
                }
                HuntPanel(accent = HuntColors.Blue) {
                    Text("Today's Objective", color = HuntColors.Text, fontWeight = FontWeight.Black)
                    Text(if (completed) "Completed today (+${challenge?.bonusPoints ?: 0})" else challenge?.prompt ?: "Loading objective...", color = HuntColors.Muted)
                }
            }
        }
    }
}
