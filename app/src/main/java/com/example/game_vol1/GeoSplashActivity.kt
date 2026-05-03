package com.example.game_vol1

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game_vol1.data.GameRepository

class GeoSplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { SplashScreen() }

        Handler(Looper.getMainLooper()).postDelayed({
            val target = if (GameRepository.isLoggedIn(this)) GeoMenuActivity::class.java else GeoLoginActivity::class.java
            startActivity(Intent(this, target))
            finish()
        }, 1400)
    }
}

@Composable
private fun SplashScreen() {
    HuntTheme {
        HuntScaffold { modifier ->
            Column(
                modifier = modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text("Heritage Hunt", color = HuntColors.Text, fontWeight = FontWeight.Black, fontSize = 34.sp)
                Text("Explore Bulgaria through play", color = HuntColors.BlueSoft, fontSize = 16.sp)
                VerticalSpacer(24)
                HuntPanel(accent = HuntColors.Green) {
                    Text("Loading your next run...", color = HuntColors.Muted)
                }
            }
        }
    }
}
