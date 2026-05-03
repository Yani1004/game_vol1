package com.example.game_vol1

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.game_vol1.data.GameRepository
import com.example.game_vol1.data.MultiplayerRepository

class RegisterActivity : AppCompatActivity() {
    private var busy by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RegisterScreen(
                busy = busy,
                cloud = MultiplayerRepository.isAvailable(this),
                onCreate = ::createAccount,
                onBack = ::finish,
                onLanguage = { UiLanguageStore.toggle(this) },
            )
        }
    }

    private fun createAccount(name: String, email: String, password: String, confirm: String) {
        if (name.isBlank() || email.isBlank() || password.isBlank() || confirm.isBlank()) {
            Toast.makeText(this, "Fill in all registration fields.", Toast.LENGTH_SHORT).show()
            return
        }
        if (password != confirm) {
            Toast.makeText(this, "Passwords do not match.", Toast.LENGTH_SHORT).show()
            return
        }

        if (MultiplayerRepository.isAvailable(this)) {
            busy = true
            MultiplayerRepository.register(this, name, email, password) { success, error ->
                runOnUiThread {
                    busy = false
                    if (!success) {
                        Toast.makeText(this, error ?: "Cloud registration failed.", Toast.LENGTH_LONG).show()
                        return@runOnUiThread
                    }
                    GameRepository.saveSessionFromCloud(this, name, email, 0)
                    startActivity(Intent(this, GeoMenuActivity::class.java))
                    finishAffinity()
                }
            }
            return
        }

        if (!GameRepository.register(this, name, email, password)) {
            Toast.makeText(this, "An account already exists on this device.", Toast.LENGTH_LONG).show()
            return
        }
        MultiplayerRepository.syncLocalProfile(this)
        startActivity(Intent(this, GeoMenuActivity::class.java))
        finishAffinity()
    }
}

@Composable
private fun RegisterScreen(
    busy: Boolean,
    cloud: Boolean,
    onCreate: (String, String, String, String) -> Unit,
    onBack: () -> Unit,
    onLanguage: () -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }

    HuntTheme {
        HuntScaffold { modifier ->
            Column(
                modifier = modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                HuntTitle(if (cloud) "Create Multiplayer Account" else "Create Local Account", "Start your heritage run.")
                HuntPanel(accent = HuntColors.Green) {
                    HuntField(name, { name = it }, "Explorer name")
                    HuntField(email, { email = it }, "Email", keyboardType = KeyboardType.Email)
                    HuntField(password, { password = it }, "Password", password = true)
                    HuntField(confirm, { confirm = it }, "Confirm password", password = true)
                    HuntButton(if (busy) "Creating..." else "Create Account", { if (!busy) onCreate(name.trim(), email.trim(), password, confirm) })
                    HuntButton("Back To Login", onBack, color = HuntColors.SlateLight)
                    HuntButton("Language", onLanguage, color = HuntColors.Blue)
                }
            }
        }
    }
}
