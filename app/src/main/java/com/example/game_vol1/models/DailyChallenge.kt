package com.example.game_vol1.models

data class DailyChallenge(
    val date: String,
    val place: HeritagePlace,
    val bonusPoints: Int,
    val prompt: String,
)
