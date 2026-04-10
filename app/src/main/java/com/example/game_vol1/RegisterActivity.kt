package com.example.game_vol1

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.game_vol1.data.GameRepository

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.geo_activity_register_v2)

        val nameInput = findViewById<EditText>(R.id.etRegisterName)
        val emailInput = findViewById<EditText>(R.id.etRegisterEmail)
        val passwordInput = findViewById<EditText>(R.id.etRegisterPassword)
        val confirmInput = findViewById<EditText>(R.id.etRegisterConfirmPassword)
        val createButton = findViewById<Button>(R.id.btnCreateAccount)
        val backButton = findViewById<Button>(R.id.btnBackToLogin)
        val languageButton = findViewById<Button>(R.id.btnLanguageToggle)
        val titleView = findViewById<TextView>(R.id.tvRegisterTitle)

        fun applyLanguage() {
            val isBulgarian = UiLanguageStore.isBulgarian(this)
            titleView.text = if (isBulgarian) "Създай профил" else "Create Account"
            nameInput.hint = if (isBulgarian) "Име" else "Name"
            emailInput.hint = if (isBulgarian) "Имейл" else "Email"
            passwordInput.hint = if (isBulgarian) "Парола" else "Password"
            confirmInput.hint = if (isBulgarian) "Потвърди парола" else "Confirm password"
            createButton.text = if (isBulgarian) "СЪЗДАЙ ПРОФИЛ" else "CREATE ACCOUNT"
            backButton.text = if (isBulgarian) "ОБРАТНО КЪМ ВХОД" else "BACK TO LOGIN"
            languageButton.text = if (isBulgarian) "ENG" else "БГ"
        }

        applyLanguage()

        createButton.setOnClickListener {
            val name = nameInput.text.toString().trim()
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString()
            val confirm = confirmInput.text.toString()
            val isBulgarian = UiLanguageStore.isBulgarian(this)

            if (name.isBlank() || email.isBlank() || password.isBlank() || confirm.isBlank()) {
                Toast.makeText(
                    this,
                    if (isBulgarian) "Попълни всички полета за регистрация." else "Fill in all registration fields.",
                    Toast.LENGTH_SHORT,
                ).show()
                return@setOnClickListener
            }

            if (password != confirm) {
                Toast.makeText(
                    this,
                    if (isBulgarian) "Паролите не съвпадат." else "Passwords do not match.",
                    Toast.LENGTH_SHORT,
                ).show()
                return@setOnClickListener
            }

            val success = GameRepository.register(this, name, email, password)
            if (!success) {
                Toast.makeText(
                    this,
                    if (isBulgarian) "На това устройство вече съществува профил." else "An account already exists on this device.",
                    Toast.LENGTH_LONG,
                ).show()
                return@setOnClickListener
            }

            startActivity(Intent(this, GeoMenuActivity::class.java))
            finishAffinity()
        }

        backButton.setOnClickListener { finish() }

        languageButton.setOnClickListener {
            UiLanguageStore.toggle(this)
            applyLanguage()
        }
    }
}
