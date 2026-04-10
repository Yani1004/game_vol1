package com.example.game_vol1

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.game_vol1.data.GameRepository
import com.example.game_vol1.models.RoundGuessResult
import com.example.game_vol1.viewmodels.GeoGameViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions

class GeoGameActivity : AppCompatActivity(), OnMapReadyCallback {
    private val viewModel: GeoGameViewModel by viewModels()

    private lateinit var tvRound: TextView
    private lateinit var tvScore: TextView
    private lateinit var tvClue: TextView
    private lateinit var tvDifficulty: TextView
    private lateinit var tvGuessStatus: TextView
    private lateinit var btnSubmitGuess: Button
    private lateinit var btnNewRun: Button

    private var map: GoogleMap? = null
    private var guessMarker: Marker? = null

    private val summaryLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) {
        val finished = viewModel.advanceAfterRound(this)
        if (finished) {
            showSessionSummary()
        } else {
            renderRound()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.geo_activity_game)

        tvRound = findViewById(R.id.tvRound)
        tvScore = findViewById(R.id.tvScore)
        tvClue = findViewById(R.id.tvClue)
        tvDifficulty = findViewById(R.id.tvDifficulty)
        tvGuessStatus = findViewById(R.id.tvGuessStatus)
        btnSubmitGuess = findViewById(R.id.btnSubmitGuess)
        btnNewRun = findViewById(R.id.btnNewRun)

        viewModel.loadProfile(this)
        viewModel.ensureSession()

        btnSubmitGuess.setOnClickListener {
            val result = viewModel.submitGuess()
            if (result == null) {
                Toast.makeText(this, "Tap the map to place a guess first.", Toast.LENGTH_SHORT).show()
            } else {
                launchRoundSummary(result)
            }
        }

        btnNewRun.setOnClickListener {
            viewModel.startNewSession()
            renderRound()
        }

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        renderRound()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        googleMap.uiSettings.isMapToolbarEnabled = false
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(42.7339, 25.4858), 6.3f))
        googleMap.setOnMapClickListener { latLng ->
            viewModel.selectGuess(latLng)
            if (guessMarker == null) {
                guessMarker = googleMap.addMarker(MarkerOptions().position(latLng).title("Your guess"))
            } else {
                guessMarker?.position = latLng
            }
            tvGuessStatus.text =
                "Guess locked at ${"%.3f".format(latLng.latitude)}, ${"%.3f".format(latLng.longitude)}"
        }
    }

    private fun renderRound() {
        val poi = viewModel.currentPoi.value ?: return
        val roundNumber = (viewModel.roundIndex.value ?: 0) + 1
        val score = viewModel.totalScore.value ?: 0

        tvRound.text = "Round $roundNumber / ${viewModel.totalRounds}"
        tvScore.text = "Session score: $score"
        tvClue.text = poi.clue
        tvDifficulty.text = "Difficulty: ${poi.difficulty}  •  Country: ${poi.country}"
        tvGuessStatus.text = "Tap anywhere on the map to place your pin."
        guessMarker?.remove()
        guessMarker = null
        map?.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(42.7339, 25.4858), 6.3f))
    }

    private fun launchRoundSummary(result: RoundGuessResult) {
        val intent = Intent(this, RoundSummaryActivity::class.java).apply {
            putExtra("poiTitle", result.poi.title)
            putExtra("poiCity", result.poi.city)
            putExtra("poiFact", result.poi.funFact)
            putExtra("roundScore", result.roundScore)
            putExtra("distanceKm", result.distanceKm)
            putExtra("accuracyLabel", GameRepository.accuracyLabel(result.distanceKm))
            putExtra("wasDiscovered", result.wasDiscovered)
            putExtra("finalRound", (viewModel.roundIndex.value ?: 0) + 1 >= viewModel.totalRounds)
            putExtra("sessionScore", viewModel.totalScore.value ?: 0)
        }
        summaryLauncher.launch(intent)
    }

    private fun showSessionSummary() {
        val profile = GameRepository.loadProfile(this)
        AlertDialog.Builder(this)
            .setTitle("Run complete")
            .setMessage(
                "You finished with ${profile.lastRunScore} points.\n\n" +
                    "Best run: ${profile.bestRunScore}\n" +
                    "Lifetime score: ${profile.totalScore}\n" +
                    "Discovered places: ${profile.discoveredPoiIds.size}",
            )
            .setPositiveButton("Play again") { _, _ ->
                viewModel.startNewSession()
                renderRound()
            }
            .setNegativeButton("Back to menu") { _, _ ->
                finish()
            }
            .setCancelable(false)
            .show()
    }
}
