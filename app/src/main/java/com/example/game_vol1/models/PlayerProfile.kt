package com.example.game_vol1.models

data class PlayerProfile(
    val username: String = "Explorer",
    val email: String = "",
    val totalScore: Int = 0,
    val visitedCount: Int = 0,
    val discoveredPlaceIds: Set<String> = emptySet(),
    val completedDailyDate: String = "",
    val bestRunScore: Int = 0,
    val lastRunScore: Int = 0,
    val gamesPlayed: Int = 0,
    val discoveredPoiIds: Set<String> = discoveredPlaceIds,
)
