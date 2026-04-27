package com.example.game_vol1.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "players")
data class PlayerEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String,
    val email: String,
    val totalScore: Int = 0,
    val gamesPlayed: Int = 0,
    val bestScore: Int = 0,
    val correctGuesses: Int = 0,
    val totalGuesses: Int = 0,
    val registrationDate: Long = System.currentTimeMillis(),
    val lastPlayedDate: Long = 0L,
    val isAdmin: Boolean = false
)
