package com.example.game_vol1.admin.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.game_vol1.admin.repository.AdminRepository
import com.example.game_vol1.database.AppDatabase
import com.example.game_vol1.database.entity.PlayerEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class PlayerViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = AdminRepository(AppDatabase.getInstance(app))

    private val _query = MutableStateFlow("")
    val operationError = MutableLiveData<String>()
    val players = _query.flatMapLatest { q ->
        if (q.isBlank()) repo.getAllPlayers() else repo.searchPlayers(q)
    }.asLiveData()

    fun search(query: String) { _query.value = query }

    fun delete(player: PlayerEntity) = viewModelScope.launch {
        repo.deletePlayer(player)
            .onFailure { operationError.postValue(it.message ?: "Failed to delete player.") }
    }
}
