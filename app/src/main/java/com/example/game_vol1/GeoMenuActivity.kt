package com.example.game_vol1

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.game_vol1.data.GameRepository
import com.google.android.material.bottomnavigation.BottomNavigationView

class GeoMenuActivity : AppCompatActivity() {
    override fun onResume() {
        super.onResume()
        renderScreen()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.geo_activity_menu_v2)

        val profile = GameRepository.loadProfile(this)
        val daily = GameRepository.getDailyChallenge()
        val team = GameRepository.loadTeam(this)

        findViewById<TextView>(R.id.tvUsername).text = profile.username
        findViewById<TextView>(R.id.tvMenuStats).text =
            "Visited ${profile.visitedCount} places  •  Score ${profile.totalScore}"
        findViewById<TextView>(R.id.tvDailyHighlight).text =
            "Today's challenge: ${daily.place.title} (+${daily.bonusPoints})"
        findViewById<TextView>(R.id.tvMenuStats).text =
            "Visited ${profile.visitedCount} places | Score ${profile.totalScore}"
        findViewById<TextView>(R.id.tvTeamSummary).text =
            if (team.hasTeam) {
                "${team.teamName} | ${team.memberNames.size} members | Team score ${team.teamScore}"
            } else {
                "Create a team, invite friends, and earn score together."
            }

        findViewById<Button>(R.id.btnPlayRun).setOnClickListener {
            startActivity(Intent(this, ExplorerMapActivity::class.java))
        }

        findViewById<Button>(R.id.btnGoals).setOnClickListener {
            startActivity(Intent(this, GoalsActivity::class.java))
        }

        findViewById<Button>(R.id.btnCollectionNew).setOnClickListener {
            startActivity(Intent(this, DiscoveriesActivity::class.java))
        }

        findViewById<Button>(R.id.btnTeam).setOnClickListener {
            startActivity(Intent(this, TeamActivity::class.java))
        }

        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            GameRepository.logout(this)
            startActivity(Intent(this, GeoLoginActivity::class.java))
            finishAffinity()
        }

        renderScreen()
    }

    private fun renderScreen() {
        val profile = GameRepository.loadProfile(this)
        val daily = GameRepository.getDailyChallenge()
        val team = GameRepository.loadTeam(this)
        val isBg = UiLanguageStore.isBulgarian(this)

        findViewById<TextView>(R.id.tvProfileSection).text = UiLanguageStore.pick(this, "Профил", "Profile")
        findViewById<TextView>(R.id.tvRouteLabel).text = UiLanguageStore.pick(this, "Днешен маршрут", "Today's Route")
        findViewById<TextView>(R.id.tvHistoryCardTitle).text = UiLanguageStore.pick(this, "История", "History")
        findViewById<TextView>(R.id.tvProfileHistoryHint).text =
            UiLanguageStore.pick(this, "Прегледай откритите обекти и бележките си.", "Revisit your discovered landmarks and notes.")
        findViewById<TextView>(R.id.tvDailyCardTitle).text = UiLanguageStore.pick(this, "Дневни", "Daily")
        findViewById<TextView>(R.id.tvProfileDailyHint).text =
            UiLanguageStore.pick(this, "Следи бонус задачата и статуса на изпълнение.", "Track the bonus challenge and completion status.")
        findViewById<TextView>(R.id.tvTeamCardTitle).text = UiLanguageStore.pick(this, "Отбор", "Team Play")
        findViewById<Button>(R.id.btnPlayRun).text = UiLanguageStore.pick(this, "Към картата", "Jump Into Explore")
        findViewById<Button>(R.id.btnCollectionNew).text = UiLanguageStore.pick(this, "Отвори", "Open")
        findViewById<Button>(R.id.btnGoals).text = UiLanguageStore.pick(this, "Виж", "View")
        findViewById<Button>(R.id.btnTeam).text = UiLanguageStore.pick(this, "Отвори отбора", "Open Team Hub")
        findViewById<Button>(R.id.btnLogout).text = UiLanguageStore.pick(this, "Изход", "Log Out")

        findViewById<TextView>(R.id.tvUsername).text = profile.username
        findViewById<TextView>(R.id.tvMenuStats).text =
            if (isBg) "Посетени места ${profile.visitedCount} | Точки ${profile.totalScore}"
            else "Visited ${profile.visitedCount} places | Score ${profile.totalScore}"
        findViewById<TextView>(R.id.tvDailyHighlight).text =
            if (isBg) "Днешно предизвикателство: ${daily.place.title} (+${daily.bonusPoints})"
            else "Today's challenge: ${daily.place.title} (+${daily.bonusPoints})"
        findViewById<TextView>(R.id.tvTeamSummary).text =
            if (team.hasTeam) {
                if (isBg) "${team.teamName} | ${team.memberNames.size} членове | Отборни точки ${team.teamScore}"
                else "${team.teamName} | ${team.memberNames.size} members | Team score ${team.teamScore}"
            } else {
                UiLanguageStore.pick(this, "Създай отбор, покани приятели и трупайте точки заедно.", "Create a team, invite friends, and earn score together.")
            }

        AppNavigation.bind(this, findViewById<BottomNavigationView>(R.id.bottomNav), R.id.nav_profile)
    }
}
