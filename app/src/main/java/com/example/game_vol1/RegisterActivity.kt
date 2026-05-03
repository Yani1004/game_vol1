package com.example.game_vol1

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.game_vol1.data.GameRepository
import com.example.game_vol1.data.MultiplayerRepository

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

        listOf(createButton, backButton, languageButton).forEach { it.applyPressFeedback() }
        findViewById<android.view.View>(android.R.id.content).fadeSlideIn()

        fun applyLanguage() {
            titleView.text = if (MultiplayerRepository.isAvailable(this)) "Create Multiplayer Account" else "Create Local Account"
            nameInput.hint = "Explorer name"
            emailInput.hint = "Email"
            passwordInput.hint = "Password"
            confirmInput.hint = "Confirm password"
            createButton.text = "Create Account"
            backButton.text = "Back To Login"
            languageButton.text = if (UiLanguageStore.isBulgarian(this)) "ENG" else "BG"
        }

        applyLanguage()

        createButton.setOnClickListener {
            val name = nameInput.text.toString().trim()
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString()
            val confirm = confirmInput.text.toString()

            if (name.isBlank() || email.isBlank() || password.isBlank() || confirm.isBlank()) {
                Toast.makeText(this, "Fill in all registration fields.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirm) {
                Toast.makeText(this, "Passwords do not match.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (MultiplayerRepository.isAvailable(this)) {
                createButton.isEnabled = false
                MultiplayerRepository.register(this, name, email, password) { success, error ->
                    runOnUiThread {
                        createButton.isEnabled = true
                        if (!success) {
                            Toast.makeText(this, error ?: "Cloud registration failed.", Toast.LENGTH_LONG).show()
                            return@runOnUiThread
                        }
                        GameRepository.saveSessionFromCloud(this, name, email, 0)
                        startActivity(Intent(this, GeoMenuActivity::class.java))
                        finishAffinity()
                    }
                }
                return@setOnClickListener
            }

            val success = GameRepository.register(this, name, email, password)
            if (!success) {
                Toast.makeText(this, "An account already exists on this device.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            MultiplayerRepository.syncLocalProfile(this)
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
