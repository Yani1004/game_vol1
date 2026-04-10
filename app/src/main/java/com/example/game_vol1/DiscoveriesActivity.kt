package com.example.game_vol1

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.game_vol1.data.GameRepository
import com.google.android.material.bottomnavigation.BottomNavigationView

class DiscoveriesActivity : AppCompatActivity() {
    override fun onResume() {
        super.onResume()
        renderScreen()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.geo_activity_collection)

        renderScreen()
    }

    private fun renderScreen() {
        val emptyText = findViewById<TextView>(R.id.tvEmptyCollection)
        val listText = findViewById<TextView>(R.id.tvCollectionList)
        val visits = GameRepository.loadVisits(this)
        val isBg = UiLanguageStore.isBulgarian(this)

        findViewById<TextView>(R.id.tvHistorySection).text = UiLanguageStore.pick(this, "История", "History")
        findViewById<TextView>(R.id.tvHistoryTitle).text = UiLanguageStore.pick(this, "Посетени места", "Places You've Visited")

        if (visits.isEmpty()) {
            emptyText.visibility = View.VISIBLE
            listText.visibility = View.GONE
            emptyText.text = UiLanguageStore.pick(
                this,
                "Още не си открил никакви места.\n\nОтвори картата, приближи се до обект и натисни Открий.",
                "You have not discovered any places yet.\n\nOpen the map, get near a landmark, and tap Discover.",
            )
        } else {
            emptyText.visibility = View.GONE
            listText.visibility = View.VISIBLE
            listText.text = visits.joinToString("\n\n") { visit ->
                val place = GameRepository.placeById(visit.placeId)
                if (place == null) {
                    UiLanguageStore.pick(this, "Неизвестно място", "Unknown place")
                } else {
                    if (isBg) {
                        "${place.title}\n${place.city}, ${place.country}\nПосетено: ${GameRepository.formatVisitTime(visit.visitedAtEpochMs)}\nТочки: ${visit.pointsEarned}\n${place.historicalInfo}"
                    } else {
                        "${place.title}\n${place.city}, ${place.country}\nVisited: ${GameRepository.formatVisitTime(visit.visitedAtEpochMs)}\nPoints: ${visit.pointsEarned}\n${place.historicalInfo}"
                    }
                }
            }
        }

        AppNavigation.bind(this, findViewById<BottomNavigationView>(R.id.bottomNav), R.id.nav_history)
    }
}
