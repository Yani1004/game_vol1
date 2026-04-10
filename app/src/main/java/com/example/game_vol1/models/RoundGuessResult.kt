package com.example.game_vol1.models

data class RoundGuessResult(
    val poi: LandmarkRound,
    val distanceKm: Double,
    val roundScore: Int,
    val wasDiscovered: Boolean,
)
