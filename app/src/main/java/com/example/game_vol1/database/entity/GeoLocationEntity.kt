package com.example.game_vol1.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "geolocations")
data class GeoLocationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val country: String,
    val city: String,
    val latitude: Double,
    val longitude: Double,
    val difficulty: String,
    val imageUrl: String = "",
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
