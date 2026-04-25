package com.example.game_vol1

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.game_vol1.data.GameRepository

class GeoSplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.geo_activity_splash)

        findViewById<TextView>(R.id.tvSplashTitle).text =
            UiLanguageStore.pick(this, "Наследство+", "Heritage Hunt")
        findViewById<TextView>(R.id.tvSplashSubtitle).text =
            UiLanguageStore.pick(this, "Игра за откриване на културни места", "Explore Bulgaria through play")
        findViewById<TextView>(R.id.tvSplashLoading).text =
            UiLanguageStore.pick(this, "Подготвяме твоето приключение...", "Loading your next run...")

        Handler(Looper.getMainLooper()).postDelayed({
            val target = if (GameRepository.isLoggedIn(this)) {
                GeoMenuActivity::class.java
            } else {
                GeoLoginActivity::class.java
            }
            startActivity(Intent(this, target))
            finish()
        }, 1400)
    }
}
