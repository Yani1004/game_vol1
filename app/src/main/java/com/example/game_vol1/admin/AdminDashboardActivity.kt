package com.example.game_vol1.admin

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import com.example.game_vol1.GeoLoginActivity
import com.example.game_vol1.R
import com.example.game_vol1.admin.viewmodel.AdminDashboardViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.navigation.NavigationView

class AdminDashboardActivity : AppCompatActivity() {

    private val vm: AdminDashboardViewModel by viewModels()
    private lateinit var drawer: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!AdminAccessManager.isAdmin(this)) {
            redirectAccessDenied(); return
        }

        setContentView(R.layout.activity_admin_dashboard)

        val toolbar = findViewById<Toolbar>(R.id.adminToolbar)
        setSupportActionBar(toolbar)

        drawer = findViewById(R.id.adminDrawerLayout)
        val toggle = ActionBarDrawerToggle(
            this, drawer, toolbar,
            R.string.nav_drawer_open, R.string.nav_drawer_close
        )
        drawer.addDrawerListener(toggle)
        toggle.syncState()

        setupNavigation()
        observeStats()
        setupQuickActions()
    }

    private fun setupNavigation() {
        val navView = findViewById<NavigationView>(R.id.adminNavView)
        navView.setNavigationItemSelectedListener { item ->
            drawer.closeDrawers()
            when (item.itemId) {
                R.id.nav_dashboard -> { /* already here */ }
                R.id.nav_add_geo -> startActivity(Intent(this, AddGeoLocationActivity::class.java))
                R.id.nav_manage_geo -> startActivity(Intent(this, ManageGeoLocationsActivity::class.java))
                R.id.nav_players -> startActivity(Intent(this, ViewPlayersActivity::class.java))
                R.id.nav_logout -> {
                    AdminAccessManager.logout(this)
                    startActivity(Intent(this, GeoLoginActivity::class.java))
                    finishAffinity()
                }
            }
            true
        }
    }

    private fun observeStats() {
        vm.playerCount.observe(this) { findViewById<TextView>(R.id.tvStatPlayers).text = it.toString() }
        vm.geoLocationCount.observe(this) { findViewById<TextView>(R.id.tvStatLocations).text = it.toString() }
        vm.totalGamesCount.observe(this) { findViewById<TextView>(R.id.tvStatGames).text = it.toString() }
    }

    private fun setupQuickActions() {
        findViewById<MaterialButton>(R.id.btnQuickAddGeo).setOnClickListener {
            startActivity(Intent(this, AddGeoLocationActivity::class.java))
        }
        findViewById<MaterialButton>(R.id.btnQuickManageGeo).setOnClickListener {
            startActivity(Intent(this, ManageGeoLocationsActivity::class.java))
        }
        findViewById<MaterialButton>(R.id.btnQuickViewPlayers).setOnClickListener {
            startActivity(Intent(this, ViewPlayersActivity::class.java))
        }
    }

    private fun redirectAccessDenied() {
        startActivity(Intent(this, GeoLoginActivity::class.java).apply {
            putExtra("access_denied", true)
        })
        finish()
    }
}
