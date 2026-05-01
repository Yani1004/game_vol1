package com.example.game_vol1

import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.game_vol1.admin.AdminAccessManager
import com.example.game_vol1.admin.AdminDashboardActivity
import com.example.game_vol1.admin.AdminLoginActivity
import com.example.game_vol1.data.GameRepository
import com.example.game_vol1.data.MultiplayerRepository

class GeoLoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.geo_activity_login_v2)

        val emailInput = findViewById<EditText>(R.id.etLoginEmail)
        val passwordInput = findViewById<EditText>(R.id.etLoginPassword)
        val loginButton = findViewById<Button>(R.id.btnLogin)
        val registerButton = findViewById<Button>(R.id.btnOpenRegister)
        val languageButton = findViewById<Button>(R.id.btnLanguageToggle)
        val titleView = findViewById<TextView>(R.id.tvLoginTitle)
        val subtitleView = findViewById<TextView>(R.id.tvLoginSubtitle)

        listOf(loginButton, registerButton, languageButton).forEach { it.applyPressFeedback() }
        findViewById<android.view.View>(android.R.id.content).fadeSlideIn()

        fun applyLanguage() {
            titleView.text = "GPS Monument Game"
            subtitleView.text = if (MultiplayerRepository.isAvailable(this)) {
                "Sign in to compete on the live leaderboard from any device."
            } else {
                "Firebase is not configured yet, so this build uses local demo login."
            }
            emailInput.hint = "Email"
            passwordInput.hint = "Password"
            loginButton.text = "Log In"
            registerButton.text = "Register"
            languageButton.text = if (UiLanguageStore.isBulgarian(this)) "ENG" else "BG"
        }

        val profile = GameRepository.loadProfile(this)
        if (profile.email.isNotBlank()) {
            emailInput.setText(profile.email)
        }

        applyLanguage()

        if (intent.getBooleanExtra("access_denied", false)) {
            Toast.makeText(this, "Access denied for the admin panel.", Toast.LENGTH_LONG).show()
        }

        loginButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString()
            if (email.isBlank() || password.isBlank()) {
                Toast.makeText(this, "Enter both email and password.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (email.equals(AdminAccessManager.ADMIN_EMAIL, ignoreCase = true)) {
                if (AdminAccessManager.login(this, email, password)) {
                    startActivity(Intent(this, AdminDashboardActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Invalid admin credentials.", Toast.LENGTH_LONG).show()
                }
                return@setOnClickListener
            }

            if (MultiplayerRepository.isAvailable(this)) {
                loginButton.isEnabled = false
                MultiplayerRepository.login(this, email, password) { success, error ->
                    runOnUiThread {
                        loginButton.isEnabled = true
                        if (!success) {
                            Toast.makeText(
                                this,
                                error ?: "Cloud login failed. Register first or check your password.",
                                Toast.LENGTH_LONG,
                            ).show()
                            return@runOnUiThread
                        }

                        MultiplayerRepository.loadRemoteProfile(this) { remoteProfile ->
                            runOnUiThread {
                                GameRepository.saveSessionFromCloud(
                                    this,
                                    remoteProfile?.username ?: email.substringBefore("@").ifBlank { "Explorer" },
                                    remoteProfile?.email ?: email,
                                    remoteProfile?.totalScore ?: 0,
                                )
                                startActivity(Intent(this, GeoMenuActivity::class.java))
                                finish()
                            }
                        }
                    }
                }
                return@setOnClickListener
            }

            if (GameRepository.login(this, email, password)) {
                MultiplayerRepository.syncLocalProfile(this)
                startActivity(Intent(this, GeoMenuActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "Login failed. Register first or check your password.", Toast.LENGTH_LONG).show()
            }
        }

        registerButton.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        languageButton.setOnClickListener {
            UiLanguageStore.toggle(this)
            applyLanguage()
        }

        val adminAccessLabel = findViewById<TextView>(R.id.tvAdminAccess)
        adminAccessLabel.paintFlags = adminAccessLabel.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        adminAccessLabel.setOnClickListener {
            startActivity(Intent(this, AdminLoginActivity::class.java))
        }
    }
}
