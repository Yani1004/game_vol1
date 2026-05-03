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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.game_vol1.HuntButton
import com.example.game_vol1.HuntColors
import com.example.game_vol1.HuntField
import com.example.game_vol1.HuntPanel
import com.example.game_vol1.HuntScaffold
import com.example.game_vol1.HuntTheme
import com.example.game_vol1.HuntTitle
import com.example.game_vol1.admin.viewmodel.PlayerViewModel
import com.example.game_vol1.database.entity.PlayerEntity

class ViewPlayersActivity : AppCompatActivity() {
    private val vm: PlayerViewModel by viewModels()
    private val players = mutableStateListOf<PlayerEntity>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!AdminAccessManager.enforceAdminOrRedirect(this)) return
        vm.players.observe(this) {
            players.clear()
            players.addAll(it)
        }
        vm.operationError.observe(this) { Toast.makeText(this, it, Toast.LENGTH_LONG).show() }
        setContent {
            ViewPlayersScreen(
                players = players,
                onSearch = vm::search,
                onPlayer = { player ->
                    startActivity(Intent(this, PlayerDetailsActivity::class.java).apply {
                        putExtra(PlayerDetailsActivity.EXTRA_PLAYER_ID, player.id)
                        putExtra(PlayerDetailsActivity.EXTRA_PLAYER_NAME, player.username)
                    })
                },
                onBack = ::finish,
            )
        }
    }
}

@Composable
private fun ViewPlayersScreen(players: List<PlayerEntity>, onSearch: (String) -> Unit, onPlayer: (PlayerEntity) -> Unit, onBack: () -> Unit) {
    var query by remember { mutableStateOf("") }
    HuntTheme {
        HuntScaffold { modifier ->
            Column(modifier = modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                HuntButton("Back", onBack, color = HuntColors.SlateLight)
                HuntTitle("Players", "Review accounts, scores, and exploration history.")
                HuntPanel(accent = HuntColors.Blue) {
                    HuntField(query, { query = it; onSearch(it) }, "Search players")
                }
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f)) {
                    items(players, key = { it.id }) { player ->
                        HuntPanel(accent = HuntColors.Gold) {
                            Text(player.username, color = HuntColors.Text, fontWeight = FontWeight.Black)
                            Text(player.email, color = HuntColors.Muted)
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text("${player.totalScore} pts", color = HuntColors.Gold, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                Text("${player.gamesPlayed} games", color = HuntColors.BlueSoft, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            }
                            HuntButton("Open Player", { onPlayer(player) }, color = HuntColors.Blue)
                        }
                    }
                }
            }
        }
    }
}
