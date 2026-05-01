package com.example.game_vol1

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.game_vol1.data.AdminRepository
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.game_vol1.data.GameRepository
import com.google.android.material.bottomnavigation.BottomNavigationView

class GeoMenuActivity : AppCompatActivity() {
    companion object {
        private const val NOTIFICATION_PERMISSION_REQUEST = 301
    }

    override fun onResume() {
        super.onResume()
        renderScreen()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.geo_activity_menu_v2)

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

        findViewById<Button>(R.id.btnAdminPanel).setOnClickListener {
            startActivity(Intent(this, AdminActivity::class.java))
        }

        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            GameRepository.logout(this)
            startActivity(Intent(this, GeoLoginActivity::class.java))
            finishAffinity()
        }

        listOf(
            R.id.btnPlayRun,
            R.id.btnGoals,
            R.id.btnCollectionNew,
            R.id.btnTeam,
            R.id.btnLogout,
        ).forEach { findViewById<Button>(it).applyPressFeedback() }
        findViewById<android.view.View>(android.R.id.content).fadeSlideIn()

        setupNotifications()
        renderScreen()
    }

    private fun setupNotifications() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            NotificationScheduler.ensureDailyReminderScheduled(this)
            return
        }

        val hasPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            NotificationScheduler.ensureDailyReminderScheduled(this)
            return
        }

        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.POST_NOTIFICATIONS),
            NOTIFICATION_PERMISSION_REQUEST
        )
    }

    private fun renderScreen() {
        val profile = GameRepository.loadProfile(this)
        val daily = GameRepository.getDailyChallenge()
        val team = GameRepository.loadTeam(this)
        val isBg = UiLanguageStore.isBulgarian(this)

        findViewById<TextView>(R.id.tvProfileSection).text = UiLanguageStore.pick(this, "Профил", "Profile")
        findViewById<TextView>(R.id.tvProfileIntro).text =
            UiLanguageStore.pick(this, "Твоят център за откриване на места, отбори и дневни цели.", "Your base for exploring landmarks, teams, and daily goals.")
        findViewById<TextView>(R.id.tvVisitedLabel).text = UiLanguageStore.pick(this, "Посетени места", "Places visited")
        findViewById<TextView>(R.id.tvScoreLabel).text = UiLanguageStore.pick(this, "Общо точки", "Total score")
        findViewById<TextView>(R.id.tvRouteLabel).text = UiLanguageStore.pick(this, "Днешен маршрут", "Today's Route")
        findViewById<TextView>(R.id.tvDailySubcopy).text =
            UiLanguageStore.pick(this, "Излез навън, открий място и поддържай прогреса си жив.", "Head out, discover a place, and keep your streak alive.")
        findViewById<TextView>(R.id.tvQuickActionsLabel).text = UiLanguageStore.pick(this, "Бързи действия", "Quick Actions")
        findViewById<TextView>(R.id.tvHistoryCardTitle).text = UiLanguageStore.pick(this, "История", "History")
        findViewById<TextView>(R.id.tvHistoryCardMeta).text =
            if (isBg) "${profile.visitedCount} места" else "${profile.visitedCount} places"
        findViewById<TextView>(R.id.tvProfileHistoryHint).text =
            UiLanguageStore.pick(this, "Прегледай откритите обекти и бележките си.", "Revisit your discovered landmarks and notes.")
        findViewById<TextView>(R.id.tvDailyCardTitle).text = UiLanguageStore.pick(this, "Дневни", "Daily")
        findViewById<TextView>(R.id.tvDailyCardMeta).text =
            if (isBg) "+${daily.bonusPoints} бонус" else "+${daily.bonusPoints} bonus"
        findViewById<TextView>(R.id.tvProfileDailyHint).text =
            UiLanguageStore.pick(this, "Следи бонус задачата и статуса на изпълнение.", "Track the bonus challenge and completion status.")
        findViewById<TextView>(R.id.tvTeamCardTitle).text = UiLanguageStore.pick(this, "Отбор", "Team Play")
        findViewById<TextView>(R.id.tvTeamMeta).text =
            if (team.hasTeam) {
                if (isBg) "${team.memberNames.size} членове" else "${team.memberNames.size} members"
            } else {
                UiLanguageStore.pick(this, "Нямаш активен отбор", "No active team")
            }
        findViewById<Button>(R.id.btnPlayRun).text = UiLanguageStore.pick(this, "Към картата", "Jump Into Explore")
        findViewById<Button>(R.id.btnCollectionNew).text = UiLanguageStore.pick(this, "Отвори", "Open")
        findViewById<Button>(R.id.btnGoals).text = UiLanguageStore.pick(this, "Виж", "View")
        findViewById<Button>(R.id.btnTeam).text = UiLanguageStore.pick(this, "Отвори отбора", "Open Team Hub")
        findViewById<Button>(R.id.btnLogout).text = UiLanguageStore.pick(this, "Изход", "Log Out")
        findViewById<Button>(R.id.btnAdminPanel).visibility = if (AdminRepository.isCurrentUserAdmin(this)) View.VISIBLE else View.GONE

        findViewById<TextView>(R.id.tvUsername).text = profile.username
        findViewById<TextView>(R.id.tvVisitedValue).text = profile.visitedCount.toString()
        findViewById<TextView>(R.id.tvScoreValue).text = profile.totalScore.toString()
        findViewById<TextView>(R.id.tvDailyHighlight).text =
            if (isBg) "${daily.place.title} (+${daily.bonusPoints})"
            else "${daily.place.title} (+${daily.bonusPoints})"
        findViewById<TextView>(R.id.tvTeamSummary).text =
            if (team.hasTeam) {
                if (isBg) {
                    "${team.teamName} е твоят активен отбор. Продължете да откривате места и да качвате общия резултат."
                } else {
                    "${team.teamName} is your active team. Keep discovering places and push the shared score higher."
                }
            } else {
                UiLanguageStore.pick(this, "Създай отбор, покани приятели и трупайте точки заедно.", "Create a team, invite friends, and earn score together.")
            }

        AppNavigation.bind(this, findViewById<BottomNavigationView>(R.id.bottomNav), R.id.nav_profile)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == NOTIFICATION_PERMISSION_REQUEST &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            NotificationScheduler.ensureDailyReminderScheduled(this)
        }
    }
}
