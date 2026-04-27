package com.example.game_vol1.admin.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.game_vol1.admin.repository.AdminRepository
import com.example.game_vol1.database.AppDatabase
import com.example.game_vol1.database.entity.GeoLocationEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class GeoLocationViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = AdminRepository(AppDatabase.getInstance(app))

    private val _searchQuery = MutableStateFlow("")
    private val _difficultyFilter = MutableStateFlow<String?>(null)

    val geoLocations = combine(_searchQuery, _difficultyFilter) { q, diff -> q to diff }
        .flatMapLatest { (q, diff) ->
            when {
                diff != null -> repo.filterByDifficulty(diff)
                q.isNotBlank() -> repo.searchGeoLocations(q)
                else -> repo.getAllGeoLocations()
            }
        }
        .asLiveData()

    val operationResult = MutableLiveData<String>()

    fun search(query: String) {
        _searchQuery.value = query
        _difficultyFilter.value = null
    }

    fun filterByDifficulty(difficulty: String?) {
        _difficultyFilter.value = difficulty
        _searchQuery.value = ""
    }

    fun add(geo: GeoLocationEntity) = viewModelScope.launch {
        repo.addGeoLocation(geo)
        operationResult.postValue("Location added successfully!")
    }

    fun update(geo: GeoLocationEntity) = viewModelScope.launch {
        repo.updateGeoLocation(geo)
        operationResult.postValue("Location updated!")
    }

    fun delete(geo: GeoLocationEntity) = viewModelScope.launch {
        repo.deleteGeoLocation(geo)
        operationResult.postValue("Location deleted.")
    }
}
