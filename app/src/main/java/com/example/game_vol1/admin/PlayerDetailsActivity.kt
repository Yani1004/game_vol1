package com.example.game_vol1.admin

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.game_vol1.HuntButton
import com.example.game_vol1.HuntColors
import com.example.game_vol1.HuntPanel
import com.example.game_vol1.HuntScaffold
import com.example.game_vol1.HuntTheme
import com.example.game_vol1.HuntTitle
import com.example.game_vol1.admin.viewmodel.PlayerDetailsViewModel
import com.example.game_vol1.database.entity.GameResultEntity
import com.example.game_vol1.database.entity.PlayerEntity
import java.text.SimpleDateFormat
import java.util.Date

class PlayerDetailsActivity : AppCompatActivity() {
    private val vm: PlayerDetailsViewModel by viewModels()
    private var player by mutableStateOf<PlayerEntity?>(null)
    private var averageScore by mutableStateOf<Double?>(null)
    private val results = mutableStateListOf<GameResultEntity>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!AdminAccessManager.enforceAdminOrRedirect(this)) return
        val playerId = intent.getIntExtra(EXTRA_PLAYER_ID, -1)
        val playerName = intent.getStringExtra(EXTRA_PLAYER_NAME) ?: "Player"
        if (playerId == -1) {
            finish()
            return
        }
        vm.getPlayer(playerId).observe(this) { player = it }
        vm.getResults(playerId).observe(this) {
            results.clear()
            results.addAll(it)
        }
        vm.averageScore.observe(this) { averageScore = it }
        vm.operationError.observe(this) { Toast.makeText(this, it, Toast.LENGTH_LONG).show() }
        vm.loadAverage(playerId)
        setContent { PlayerDetailsScreen(playerName, player, averageScore, results, ::finish) }
    }

    companion object {
        const val EXTRA_PLAYER_ID = "player_id"
        const val EXTRA_PLAYER_NAME = "player_name"
    }
}

@Composable
private fun PlayerDetailsScreen(playerName: String, player: PlayerEntity?, averageScore: Double?, results: List<GameResultEntity>, onBack: () -> Unit) {
    val sdf = SimpleDateFormat("MMM dd, yyyy", LocalLocale.current.platformLocale)
    val correct = results.filter { it.isCorrect }
    val incorrect = results.filterNot { it.isCorrect }
    HuntTheme {
        HuntScaffold { modifier ->
            Column(modifier = modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                HuntButton("Back", onBack, color = HuntColors.SlateLight)
                HuntTitle(player?.username ?: playerName, player?.email ?: "Loading player profile.")
                if (player != null) {
                    val accuracy = if (player.totalGuesses > 0) "%.1f%%".format(player.correctGuesses * 100f / player.totalGuesses) else "N/A"
                    val lastPlayed = if (player.lastPlayedDate > 0) sdf.format(Date(player.lastPlayedDate)) else "Never"
                    AdminMetricRow("Rank" to rank(player.totalScore), "Score" to player.totalScore.toString(), "Best" to player.bestScore.toString())
                    AdminMetricRow("Games" to player.gamesPlayed.toString(), "Accuracy" to accuracy, "Average" to (averageScore?.let { "%.0f pts".format(it) } ?: "N/A"))
                    HuntPanel(accent = HuntColors.Blue) {
                        Text("Registered: ${sdf.format(Date(player.registrationDate))}", color = HuntColors.Muted)
                        Text("Last played: $lastPlayed", color = HuntColors.Muted)
                        Text("Correct places: ${correct.size}", color = HuntColors.Green, fontWeight = FontWeight.Bold)
                        Text("Incorrect places: ${incorrect.size}", color = HuntColors.Rose, fontWeight = FontWeight.Bold)
                    }
                }
                Text("Game History", color = HuntColors.Text, fontWeight = FontWeight.Black)
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f)) {
                    items(results, key = { it.id }) { result ->
                        HuntPanel(accent = if (result.isCorrect) HuntColors.Green else HuntColors.Rose) {
                            Text(result.geoLocationName, color = HuntColors.Text, fontWeight = FontWeight.Black)
                            Text("${result.score} pts | ${if (result.isCorrect) "Correct" else "Missed"}", color = HuntColors.Gold, fontWeight = FontWeight.Bold)
                            Text("Distance: %.1f km | Time: ${result.timeTakenSeconds}s".format(result.distanceKm), color = HuntColors.Muted)
                            Text(sdf.format(Date(result.playedAt)), color = HuntColors.BlueSoft)
                        }
                    }
                }
            }
        }
    }
}

private fun rank(score: Int) = when {
    score >= 8000 -> "Master"
    score >= 5000 -> "Expert"
    score >= 2500 -> "Navigator"
    score >= 1000 -> "Explorer"
    else -> "Beginner"
}
