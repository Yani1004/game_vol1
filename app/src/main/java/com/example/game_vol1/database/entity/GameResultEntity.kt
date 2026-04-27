package com.example.game_vol1.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_results")
data class GameResultEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val playerId: Int,
    val geoLocationId: Int = 0,
    val geoLocationName: String,
    val score: Int,
    val isCorrect: Boolean,
    val distanceKm: Double,
    val playedAt: Long = System.currentTimeMillis(),
    val timeTakenSeconds: Int = 0
)
