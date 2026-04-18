package com.example.game_vol1.models

data class PlayerProfile(
    val username: String = "Explorer",
    val email: String = "",
    val totalScore: Int = 0,
    val visitedCount: Int = 0,
    val discoveredPlaceIds: Set<String> = emptySet(),
    val completedDailyDate: String = "",
)
