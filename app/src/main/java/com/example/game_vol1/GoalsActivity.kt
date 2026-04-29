package com.example.game_vol1

import android.os.Bundle
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
        findViewById<android.view.View>(android.R.id.content).fadeSlideIn()

        renderScreen()
    }

    private fun renderScreen() {
        val profile = GameRepository.loadProfile(this)
        val challenge = GameRepository.getDailyChallenge()
        val totalPlaces = GameRepository.getPlaces().size
        val dailyCompleted = profile.completedDailyDate == challenge.date
        val isBg = UiLanguageStore.isBulgarian(this)

        findViewById<TextView>(R.id.tvDailySection).text = UiLanguageStore.pick(this, "Дневни", "Daily")
        findViewById<TextView>(R.id.tvGoalsTitle).text = UiLanguageStore.pick(this, "Дневно предизвикателство", "Challenge Board")
        findViewById<TextView>(R.id.tvFeaturedLabel).text = UiLanguageStore.pick(this, "Избрано място", "Featured Place")
        findViewById<TextView>(R.id.tvCollectionLabel).text = UiLanguageStore.pick(this, "Напредък на колекцията", "Collection Progress")
        findViewById<TextView>(R.id.tvObjectiveLabel).text = UiLanguageStore.pick(this, "Днешна цел", "Today's Objective")
        findViewById<TextView>(R.id.tvQuestOneProgress).text = challenge.place.title
        findViewById<TextView>(R.id.tvDailyBonus).text = if (isBg) "+${challenge.bonusPoints} бонус" else "+${challenge.bonusPoints} bonus"
        findViewById<TextView>(R.id.tvDailyStatus).text =
            if (dailyCompleted) {
                UiLanguageStore.pick(this, "Днешното предизвикателство е завършено.", "Today's challenge is completed.")
            } else {
                UiLanguageStore.pick(this, "Открий това място днес, за да вземеш бонуса.", "Discover this place today to claim the bonus.")
            }
        findViewById<TextView>(R.id.tvQuestTwoProgress).text =
            if (isBg) "${profile.discoveredPlaceIds.size} / $totalPlaces открити"
            else "${profile.discoveredPlaceIds.size} / $totalPlaces discovered"
        findViewById<TextView>(R.id.tvCollectionHint).text =
            if (isBg) {
                "Колкото повече места откриваш, толкова по-богата става личната ти карта на наследството."
            } else {
                "The more places you discover, the richer your personal heritage journal becomes."
            }
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
