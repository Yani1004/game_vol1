package com.example.game_vol1.models

data class PlaceVisit(
    val placeId: String,
    val visitedAtEpochMs: Long,
    val pointsEarned: Int,
)
