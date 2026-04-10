package com.example.game_vol1

import android.app.Activity
import android.content.Intent
import com.google.android.material.bottomnavigation.BottomNavigationView

object AppNavigation {
    fun bind(activity: Activity, nav: BottomNavigationView, selectedItemId: Int) {
        nav.menu.findItem(R.id.nav_explore)?.title = UiLanguageStore.pick(activity, "Карта", "Explore")
        nav.menu.findItem(R.id.nav_history)?.title = UiLanguageStore.pick(activity, "История", "History")
        nav.menu.findItem(R.id.nav_daily)?.title = UiLanguageStore.pick(activity, "Дневни", "Daily")
        nav.menu.findItem(R.id.nav_profile)?.title = UiLanguageStore.pick(activity, "Профил", "Profile")
        nav.selectedItemId = selectedItemId
        nav.setOnItemSelectedListener { item ->
            if (item.itemId == selectedItemId) {
                true
            } else {
                val target = when (item.itemId) {
                    R.id.nav_explore -> ExplorerMapActivity::class.java
                    R.id.nav_history -> DiscoveriesActivity::class.java
                    R.id.nav_daily -> GoalsActivity::class.java
                    R.id.nav_profile -> GeoMenuActivity::class.java
                    else -> return@setOnItemSelectedListener false
                }
                activity.startActivity(Intent(activity, target))
                activity.overridePendingTransition(0, 0)
                activity.finish()
                true
            }
        }
    }
}
