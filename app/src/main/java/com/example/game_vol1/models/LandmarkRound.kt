package com.example.game_vol1.models

data class LandmarkRound(
    val id: String,
    val title: String,
    val city: String,
    val country: String,
    val clue: String,
    val funFact: String,
    val latitude: Double,
    val longitude: Double,
    val difficulty: String,
    val unlockScore: Int,
)
