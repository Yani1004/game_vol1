package com.example.game_vol1

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.game_vol1.data.GameRepository
import com.google.android.material.bottomnavigation.BottomNavigationView

class GoalsActivity : AppCompatActivity() {
    override fun onResume() {
        super.onResume()
        renderScreen()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.geo_activity_goals)

        renderScreen()
    }

    private fun renderScreen() {
        val profile = GameRepository.loadProfile(this)
        val challenge = GameRepository.getDailyChallenge()
        val totalPlaces = GameRepository.getPlaces().size
        val dailyCompleted = profile.completedDailyDate == challenge.date
        val isBg = UiLanguageStore.isBulgarian(this)

        findViewById<TextView>(R.id.tvDailySection).text = UiLanguageStore.pick(this, "Дневни", "Daily")
        findViewById<TextView>(R.id.tvGoalsTitle).text = UiLanguageStore.pick(this, "Табло с предизвикателства", "Challenge Board")
        findViewById<TextView>(R.id.tvFeaturedLabel).text = UiLanguageStore.pick(this, "Избрано място", "Featured Place")
        findViewById<TextView>(R.id.tvCollectionLabel).text = UiLanguageStore.pick(this, "Напредък на колекцията", "Collection Progress")
        findViewById<TextView>(R.id.tvObjectiveLabel).text = UiLanguageStore.pick(this, "Днешна цел", "Today's Objective")
        findViewById<TextView>(R.id.tvQuestOneProgress).text = challenge.place.title
        findViewById<TextView>(R.id.tvQuestTwoProgress).text =
            if (isBg) "${profile.discoveredPlaceIds.size} / $totalPlaces открити"
            else "${profile.discoveredPlaceIds.size} / $totalPlaces discovered"
        findViewById<TextView>(R.id.tvQuestThreeProgress).text =
            if (dailyCompleted) {
                if (isBg) "Изпълнено днес (+${challenge.bonusPoints})" else "Completed today (+${challenge.bonusPoints})"
            } else {
                if (isBg) "Отиди до ${challenge.place.title} в ${challenge.place.city} и го открий днес за бонус."
                else challenge.prompt
            }

        AppNavigation.bind(this, findViewById<BottomNavigationView>(R.id.bottomNav), R.id.nav_daily)
    }
}
