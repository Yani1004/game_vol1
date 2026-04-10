package com.example.game_vol1.models

data class HeritagePlace(
    val id: String,
    val title: String,
    val city: String,
    val country: String,
    val latitude: Double,
    val longitude: Double,
    val shortDescription: String,
    val historicalInfo: String,
    val category: String,
)
