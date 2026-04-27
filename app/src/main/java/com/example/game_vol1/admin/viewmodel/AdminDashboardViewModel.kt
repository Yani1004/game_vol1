package com.example.game_vol1.admin.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asLiveData
import com.example.game_vol1.admin.repository.AdminRepository
import com.example.game_vol1.database.AppDatabase

class AdminDashboardViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = AdminRepository(AppDatabase.getInstance(app))
    val playerCount = repo.getPlayerCount().asLiveData()
    val geoLocationCount = repo.getGeoLocationCount().asLiveData()
    val totalGamesCount = repo.getTotalGamesCount().asLiveData()
}
