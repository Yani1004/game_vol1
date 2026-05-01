package com.example.game_vol1

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.game_vol1.data.GameRepository
import com.example.game_vol1.data.MultiplayerRepository
import com.google.firebase.firestore.ListenerRegistration
import com.google.android.material.bottomnavigation.BottomNavigationView

class DiscoveriesActivity : AppCompatActivity() {
    private var leaderboardListener: ListenerRegistration? = null
    private var cloudLeaderboard: List<MultiplayerRepository.LeaderboardEntry> = emptyList()

    override fun onResume() {
        super.onResume()
        renderScreen()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.geo_activity_collection)
        findViewById<View>(android.R.id.content).fadeSlideIn()

        renderScreen()
        leaderboardListener = MultiplayerRepository.observeGlobalLeaderboard(this) { entries ->
            cloudLeaderboard = entries
            runOnUiThread { renderScreen() }
        }
    }

    override fun onDestroy() {
        leaderboardListener?.remove()
        leaderboardListener = null
        super.onDestroy()
    }

    private fun renderScreen() {
        val emptyText = findViewById<TextView>(R.id.tvEmptyCollection)
        val listText = findViewById<TextView>(R.id.tvCollectionList)
        val summaryLabel = findViewById<TextView>(R.id.tvHistorySummaryLabel)
        val summaryView = findViewById<TextView>(R.id.tvHistorySummary)
        val summaryHint = findViewById<TextView>(R.id.tvHistorySummaryHint)
        val visits = GameRepository.loadVisits(this)
        val isBg = UiLanguageStore.isBulgarian(this)
        val totalPoints = visits.sumOf { it.pointsEarned }

        findViewById<TextView>(R.id.tvHistorySection).text = UiLanguageStore.pick(this, "История", "History")
        findViewById<TextView>(R.id.tvHistoryTitle).text = UiLanguageStore.pick(this, "Посетени места", "Places You've Visited")
        summaryLabel.text = UiLanguageStore.pick(this, "Статистика", "Journey Stats")
        summaryView.text = if (isBg) "${visits.size} открити места" else "${visits.size} places discovered"
        summaryHint.text =
            if (isBg) {
                "Събрани точки от историята: $totalPoints. Всяко място пази време на посещение и кратка информация."
            } else {
                "Points collected from your journey: $totalPoints. Every place keeps its visit time and story."
            }

        if (visits.isEmpty()) {
            emptyText.visibility = View.VISIBLE
            listText.visibility = if (cloudLeaderboard.isEmpty()) View.GONE else View.VISIBLE
            emptyText.text = UiLanguageStore.pick(
                this,
                "Още не си открил никакви места.\n\nОтвори картата, приближи се до обект и натисни Открий.",
                "You have not discovered any places yet.\n\nOpen the map, get near a landmark, and tap Discover.",
            )
        } else {
            emptyText.visibility = View.GONE
            listText.visibility = View.VISIBLE
        }

        listText.text = buildString {
            appendLine(if (MultiplayerRepository.isAvailable(this@DiscoveriesActivity)) "Live Global Leaderboard" else "Local Leaderboard")
            appendLine()
            if (cloudLeaderboard.isEmpty()) {
                appendLine("No cloud scores yet. Discover a place on two devices to populate this list.")
            } else {
                cloudLeaderboard.forEach { entry ->
                    appendLine("${entry.rank}. ${entry.username} - ${entry.totalScore} pts (${entry.visitedCount} places)")
                }
            }

            if (visits.isNotEmpty()) {
                appendLine()
                appendLine("Your Discoveries")
                appendLine()
                append(
                    visits.joinToString("\n\n") { visit ->
                        val place = GameRepository.placeById(visit.placeId)
                        if (place == null) {
                            UiLanguageStore.pick(this@DiscoveriesActivity, "Неизвестно място", "Unknown place")
                        } else if (isBg) {
                            "${place.title}\n${place.city}, ${place.country}\nПосетено: ${GameRepository.formatVisitTime(visit.visitedAtEpochMs)}\nТочки: ${visit.pointsEarned}\n${place.historicalInfo}"
                        } else {
                            "${place.title}\n${place.city}, ${place.country}\nVisited: ${GameRepository.formatVisitTime(visit.visitedAtEpochMs)}\nPoints: ${visit.pointsEarned}\n${place.historicalInfo}"
                        }
                    },
                )
            }
        }

        AppNavigation.bind(this, findViewById<BottomNavigationView>(R.id.bottomNav), R.id.nav_history)
    }
}
