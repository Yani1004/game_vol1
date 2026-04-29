package com.example.game_vol1.admin

import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.game_vol1.R
import com.example.game_vol1.admin.adapter.GameResultAdapter
import com.example.game_vol1.admin.viewmodel.PlayerDetailsViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PlayerDetailsActivity : AppCompatActivity() {

    private val vm: PlayerDetailsViewModel by viewModels()
    private lateinit var resultsAdapter: GameResultAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!AdminAccessManager.enforceAdminOrRedirect(this)) return
        setContentView(R.layout.activity_player_details)

        val playerId = intent.getIntExtra(EXTRA_PLAYER_ID, -1)
        val playerName = intent.getStringExtra(EXTRA_PLAYER_NAME) ?: "Player"

        val toolbar = findViewById<Toolbar>(R.id.playerDetailsToolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = playerName

        resultsAdapter = GameResultAdapter()
        val rv = findViewById<RecyclerView>(R.id.rvGameHistory)
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = resultsAdapter
        rv.isNestedScrollingEnabled = false

        if (playerId == -1) { finish(); return }

        vm.getPlayer(playerId).observe(this) { player ->
            player ?: return@observe
            val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            val lastPlayed = if (player.lastPlayedDate > 0)
                sdf.format(Date(player.lastPlayedDate)) else "Never"
            val accuracy = if (player.totalGuesses > 0)
                "%.1f%%".format(player.correctGuesses * 100f / player.totalGuesses) else "N/A"

            findViewById<TextView>(R.id.tvDetailUsername).text = player.username
            findViewById<TextView>(R.id.tvDetailEmail).text = player.email
            findViewById<TextView>(R.id.tvDetailRank).text = rank(player.totalScore)
            findViewById<TextView>(R.id.tvDetailTotalScore).text = player.totalScore.toString()
            findViewById<TextView>(R.id.tvDetailBestScore).text = player.bestScore.toString()
            findViewById<TextView>(R.id.tvDetailGamesPlayed).text = player.gamesPlayed.toString()
            findViewById<TextView>(R.id.tvDetailCorrect).text = player.correctGuesses.toString()
            findViewById<TextView>(R.id.tvDetailAccuracy).text = accuracy
            findViewById<TextView>(R.id.tvDetailRegDate).text = sdf.format(Date(player.registrationDate))
            findViewById<TextView>(R.id.tvDetailLastPlayed).text = lastPlayed
        }

        vm.getResults(playerId).observe(this) { results ->
            resultsAdapter.submitList(results)

            val correct = results.filter { it.isCorrect }
            val incorrect = results.filterNot { it.isCorrect }

            findViewById<TextView>(R.id.tvCorrectLocationsCount).text = correct.size.toString()
            findViewById<TextView>(R.id.tvIncorrectLocationsCount).text = incorrect.size.toString()
            findViewById<TextView>(R.id.tvCorrectLocationsList).text =
                if (correct.isEmpty()) "No correct guesses yet." else correct.take(5).joinToString("\n") { it.geoLocationName }
            findViewById<TextView>(R.id.tvIncorrectLocationsList).text =
                if (incorrect.isEmpty()) "No incorrect guesses yet." else incorrect.take(5).joinToString("\n") { it.geoLocationName }
        }

        vm.averageScore.observe(this) { avg ->
            val text = if (avg != null) "%.0f pts".format(avg) else "N/A"
            findViewById<TextView>(R.id.tvDetailAvgScore).text = text
        }
        vm.operationError.observe(this) { Toast.makeText(this, it, Toast.LENGTH_LONG).show() }

        vm.loadAverage(playerId)

        findViewById<com.google.android.material.button.MaterialButton>(R.id.btnBackAccessible)
            .setOnClickListener { finish() }
    }

    private fun rank(score: Int) = when {
        score >= 8000 -> "Master"
        score >= 5000 -> "Expert"
        score >= 2500 -> "Navigator"
        score >= 1000 -> "Explorer"
        else -> "Beginner"
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) { finish(); return true }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        const val EXTRA_PLAYER_ID = "player_id"
        const val EXTRA_PLAYER_NAME = "player_name"
    }
}
