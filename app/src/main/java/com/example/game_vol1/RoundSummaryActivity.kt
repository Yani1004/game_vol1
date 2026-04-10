package com.example.game_vol1

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class RoundSummaryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.geo_activity_round_summary)

        val title = intent.getStringExtra("poiTitle").orEmpty()
        val city = intent.getStringExtra("poiCity").orEmpty()
        val fact = intent.getStringExtra("poiFact").orEmpty()
        val score = intent.getIntExtra("roundScore", 0)
        val distanceKm = intent.getDoubleExtra("distanceKm", 0.0)
        val accuracyLabel = intent.getStringExtra("accuracyLabel").orEmpty()
        val wasDiscovered = intent.getBooleanExtra("wasDiscovered", false)
        val finalRound = intent.getBooleanExtra("finalRound", false)
        val sessionScore = intent.getIntExtra("sessionScore", 0)

        findViewById<TextView>(R.id.tvRoundResultTitle).text = title
        findViewById<TextView>(R.id.tvRoundResultSubtitle).text = "$city • $accuracyLabel"
        findViewById<TextView>(R.id.tvRoundScore).text = "$score pts"
        findViewById<TextView>(R.id.tvRoundDistance).text = "${"%.1f".format(distanceKm)} km away"
        findViewById<TextView>(R.id.tvRoundFact).text = fact
        findViewById<TextView>(R.id.tvRoundUnlock).text =
            if (wasDiscovered) "Location discovered and added to your collection."
            else "Nice try. Score higher on this place to unlock it permanently."
        findViewById<TextView>(R.id.tvSessionRunningScore).text = "Session score: $sessionScore"

        findViewById<Button>(R.id.btnContinueRound).apply {
            text = if (finalRound) "Finish run" else "Next round"
            setOnClickListener { finish() }
        }
    }
}
