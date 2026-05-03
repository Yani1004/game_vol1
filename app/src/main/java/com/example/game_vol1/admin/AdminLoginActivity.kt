package com.example.game_vol1.admin

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.KeyboardType
import com.example.game_vol1.HuntButton
import com.example.game_vol1.HuntColors
import com.example.game_vol1.HuntField
import com.example.game_vol1.HuntPanel

class AdminLoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (AdminAccessManager.isAdmin(this)) {
            goToDashboard()
            return
        }
        setContent {
            AdminLoginScreen(
                onLogin = { email, password ->
                    when {
                        email.isBlank() || password.isBlank() -> Toast.makeText(this, "Enter admin email and password", Toast.LENGTH_SHORT).show()
                        AdminAccessManager.login(this, email, password) -> goToDashboard()
                        else -> Toast.makeText(this, "Invalid admin credentials", Toast.LENGTH_SHORT).show()
                    }
                },
                onBack = ::finish,
            )
        }
    }

    private fun goToDashboard() {
        startActivity(Intent(this, AdminDashboardActivity::class.java))
        finish()
    }
}

@Composable
private fun AdminLoginScreen(onLogin: (String, String) -> Unit, onBack: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    AdminScrollScreen("Admin Access", "Manage places, players, and game progress.", onBack) {
        HuntPanel(accent = HuntColors.Blue) {
            HuntField(email, { email = it }, "Admin email", keyboardType = KeyboardType.Email)
            HuntField(password, { password = it }, "Password", password = true)
            HuntButton("Log In", { onLogin(email.trim(), password) }, color = HuntColors.Blue)
        }
    }
}
