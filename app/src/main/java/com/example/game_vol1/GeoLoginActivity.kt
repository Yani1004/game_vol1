package com.example.game_vol1

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.game_vol1.admin.AdminAccessManager
import com.example.game_vol1.admin.AdminDashboardActivity
import com.example.game_vol1.admin.AdminLoginActivity
import com.example.game_vol1.data.GameRepository
import com.example.game_vol1.data.MultiplayerRepository

class GeoLoginActivity : AppCompatActivity() {
    private var busy by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (intent.getBooleanExtra("access_denied", false)) {
            Toast.makeText(this, "Access denied for the admin panel.", Toast.LENGTH_LONG).show()
        }

        setContent {
            LoginScreen(
                initialEmail = GameRepository.loadProfile(this).email,
                busy = busy,
                onLogin = ::login,
                onRegister = { startActivity(Intent(this, RegisterActivity::class.java)) },
                onLanguage = { UiLanguageStore.toggle(this) },
                onAdmin = { startActivity(Intent(this, AdminLoginActivity::class.java)) },
            )
        }
    }

    private fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            Toast.makeText(this, "Enter both email and password.", Toast.LENGTH_SHORT).show()
            return
        }

        if (email.equals(AdminAccessManager.ADMIN_EMAIL, ignoreCase = true)) {
            if (AdminAccessManager.login(this, email, password)) {
                startActivity(Intent(this, AdminDashboardActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "Invalid admin credentials.", Toast.LENGTH_LONG).show()
            }
            return
        }

        if (MultiplayerRepository.isAvailable(this)) {
            busy = true
            MultiplayerRepository.login(this, email, password) { success, error ->
                runOnUiThread {
                    busy = false
                    if (!success) {
                        Toast.makeText(this, error ?: "Cloud login failed. Register first or check your password.", Toast.LENGTH_LONG).show()
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
            return
        }

        if (GameRepository.login(this, email, password)) {
            MultiplayerRepository.syncLocalProfile(this)
            startActivity(Intent(this, GeoMenuActivity::class.java))
            finish()
        } else {
            Toast.makeText(this, "Login failed. Register first or check your password.", Toast.LENGTH_LONG).show()
        }
    }
}

@Composable
private fun LoginScreen(
    initialEmail: String,
    busy: Boolean,
    onLogin: (String, String) -> Unit,
    onRegister: () -> Unit,
    onLanguage: () -> Unit,
    onAdmin: () -> Unit,
) {
    var email by remember { mutableStateOf(initialEmail) }
    var password by remember { mutableStateOf("") }

    HuntTheme {
        HuntScaffold { modifier ->
            Column(
                modifier = modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                HuntTitle("GPS Monument Game", "Sign in to discover landmarks, earn points, and compete.")
                HuntPanel(accent = HuntColors.Blue) {
                    HuntField(email, { email = it }, "Email", keyboardType = KeyboardType.Email)
                    HuntField(password, { password = it }, "Password", password = true)
                    HuntButton(if (busy) "Signing in..." else "Log In", { if (!busy) onLogin(email.trim(), password) }, color = HuntColors.Blue)
                    HuntButton("Register", onRegister, color = HuntColors.Green)
                    HuntButton("Language", onLanguage, color = HuntColors.SlateLight)
                    Text(
                        "Admin access",
                        color = HuntColors.BlueSoft,
                        fontWeight = FontWeight.Bold,
                        textDecoration = TextDecoration.Underline,
                        modifier = Modifier.clickable(onClick = onAdmin),
                    )
                }
            }
        }
    }
}
