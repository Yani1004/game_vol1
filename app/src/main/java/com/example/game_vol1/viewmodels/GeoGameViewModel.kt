package com.example.game_vol1.viewmodels

import android.content.Context
import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.game_vol1.data.GameRepository
import com.example.game_vol1.models.LandmarkRound
import com.example.game_vol1.models.PlayerProfile
import com.example.game_vol1.models.RoundGuessResult
import com.google.android.gms.maps.model.LatLng

class GeoGameViewModel : ViewModel() {
    private val _currentPoi = MutableLiveData<LandmarkRound?>()
    val currentPoi: LiveData<LandmarkRound?> = _currentPoi

    private val _selectedGuess = MutableLiveData<LatLng?>()
    val selectedGuess: LiveData<LatLng?> = _selectedGuess

    private val _roundIndex = MutableLiveData(0)
    val roundIndex: LiveData<Int> = _roundIndex

    private val _totalScore = MutableLiveData(0)
    val totalScore: LiveData<Int> = _totalScore

    private val _profile = MutableLiveData<PlayerProfile>()
    val profile: LiveData<PlayerProfile> = _profile

    private val sessionRounds = mutableListOf<LandmarkRound>()
    private val discoveredThisRun = mutableSetOf<String>()

    val totalRounds: Int = 5

    fun loadProfile(context: Context) {
        _profile.value = GameRepository.loadProfile(context)
    }

    fun ensureSession() {
        if (sessionRounds.isEmpty()) {
            startNewSession()
        }
    }

    fun startNewSession() {
        sessionRounds.clear()
        sessionRounds += GameRepository.getAllRounds().shuffled().take(totalRounds)
        discoveredThisRun.clear()
        _roundIndex.value = 0
        _totalScore.value = 0
        _selectedGuess.value = null
        _currentPoi.value = sessionRounds.firstOrNull()
    }

    fun selectGuess(latLng: LatLng) {
        _selectedGuess.value = latLng
    }

    fun submitGuess(): RoundGuessResult? {
        val poi = _currentPoi.value ?: return null
        val guess = _selectedGuess.value ?: return null

        val results = FloatArray(1)
        Location.distanceBetween(
            guess.latitude,
            guess.longitude,
            poi.latitude,
            poi.longitude,
            results,
        )

        val distanceKm = results[0] / 1000.0
        val score = GameRepository.distanceToScore(distanceKm)
        val discovered = score >= poi.unlockScore

        if (discovered) {
            discoveredThisRun += poi.id
        }

        _totalScore.value = (_totalScore.value ?: 0) + score

        return RoundGuessResult(
            poi = poi,
            distanceKm = distanceKm,
            roundScore = score,
            wasDiscovered = discovered,
        )
    }

    fun advanceAfterRound(context: Context): Boolean {
        val nextIndex = (_roundIndex.value ?: 0) + 1
        if (nextIndex >= sessionRounds.size) {
            GameRepository.saveGameResult(
                context = context,
                sessionScore = _totalScore.value ?: 0,
                discoveredThisRun = discoveredThisRun,
            )
            loadProfile(context)
            return true
        }

        _roundIndex.value = nextIndex
        _currentPoi.value = sessionRounds[nextIndex]
        _selectedGuess.value = null
        return false
    }
}
