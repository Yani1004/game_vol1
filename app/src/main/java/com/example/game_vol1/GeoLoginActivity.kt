package com.example.game_vol1

import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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
            loginButton.text = if (isBulgarian) "ВХОД" else "LOGIN"
            registerButton.text = if (isBulgarian) "РЕГИСТРАЦИЯ" else "REGISTER"
            languageButton.text = if (isBulgarian) "ENG" else "БГ"
        }

        val profile = GameRepository.loadProfile(this)
        if (profile.email.isNotBlank()) {
            emailInput.setText(profile.email)
        }

        applyLanguage()

        loginButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString()
            if (email.isBlank() || password.isBlank()) {
                Toast.makeText(
                    this,
                    if (UiLanguageStore.isBulgarian(this)) "Въведи имейл и парола." else "Enter both email and password.",
                    Toast.LENGTH_SHORT,
                ).show()
                return@setOnClickListener
            }

            if (GameRepository.login(this, email, password)) {
                startActivity(Intent(this, GeoMenuActivity::class.java))
                finish()
            } else {
                Toast.makeText(
                    this,
                    if (UiLanguageStore.isBulgarian(this)) {
                        "Неуспешен вход. Регистрирай се първо или провери паролата си."
                    } else {
                        "Login failed. Register first or check your password."
                    },
                    Toast.LENGTH_LONG,
                ).show()
            }
        }

        registerButton.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        languageButton.setOnClickListener {
            UiLanguageStore.toggle(this)
            applyLanguage()
        }

        val tvAdmin = findViewById<TextView>(R.id.tvAdminAccess)
        tvAdmin.paintFlags = tvAdmin.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        tvAdmin.setOnClickListener {
            startActivity(Intent(this, AdminLoginActivity::class.java))
        }
    }
}
