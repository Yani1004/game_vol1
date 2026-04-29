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
            val isBulgarian = UiLanguageStore.isBulgarian(this)
            titleView.text = if (isBulgarian) "Наследство+" else "Heritage Hunt"
            subtitleView.text = if (isBulgarian) {
                "Влез, за да откриваш места наблизо, да пазиш историята си и да изпълняваш дневни предизвикателства."
            } else {
                "Log in to explore nearby places, keep your history, and complete daily challenges."
            }
            emailInput.hint = if (isBulgarian) "Имейл" else "Email"
            passwordInput.hint = if (isBulgarian) "Парола" else "Password"
            loginButton.text = if (isBulgarian) "Вход" else "Log In"
            registerButton.text = if (isBulgarian) "Регистрация" else "Register"
            languageButton.text = if (isBulgarian) "ENG" else "БГ"
        }

        val profile = GameRepository.loadProfile(this)
        if (profile.email.isNotBlank()) {
            emailInput.setText(profile.email)
        }

        applyLanguage()

        if (intent.getBooleanExtra("access_denied", false)) {
            val message = if (UiLanguageStore.isBulgarian(this)) {
                "Достъпът до админ панела е отказан."
            } else {
                "Access denied for the admin panel."
            }
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        }

        loginButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString()
            if (email.isBlank() || password.isBlank()) {
                val message = if (UiLanguageStore.isBulgarian(this)) {
                    "Въведи имейл и парола."
                } else {
                    "Enter both email and password."
                }
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (email.equals(AdminAccessManager.ADMIN_EMAIL, ignoreCase = true)) {
                if (AdminAccessManager.login(this, email, password)) {
                    startActivity(Intent(this, AdminDashboardActivity::class.java))
                    finish()
                } else {
                    val message = if (UiLanguageStore.isBulgarian(this)) {
                        "Невалидни админ данни."
                    } else {
                        "Invalid admin credentials."
                    }
                    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                }
                return@setOnClickListener
            }

            if (GameRepository.login(this, email, password)) {
                startActivity(Intent(this, GeoMenuActivity::class.java))
                finish()
            } else {
                val message = if (UiLanguageStore.isBulgarian(this)) {
                    "Неуспешен вход. Регистрирай се първо или провери паролата си."
                } else {
                    "Login failed. Register first or check your password."
                }
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
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
