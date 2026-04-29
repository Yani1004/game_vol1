package com.example.game_vol1.admin.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.game_vol1.admin.repository.AdminRepository
import com.example.game_vol1.database.AppDatabase
import kotlinx.coroutines.launch

class PlayerDetailsViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = AdminRepository(AppDatabase.getInstance(app))

    val averageScore = MutableLiveData<Double?>()
    val operationError = MutableLiveData<String>()

    fun getPlayer(id: Int) = repo.getPlayerById(id).asLiveData()
    fun getResults(playerId: Int) = repo.getResultsForPlayer(playerId).asLiveData()

    fun loadAverage(playerId: Int) = viewModelScope.launch {
        repo.getAverageScore(playerId)
            .onSuccess { averageScore.postValue(it) }
            .onFailure { operationError.postValue(it.message ?: "Failed to load average score.") }
    }
}
