package com.example.game_vol1.admin.repository

import com.example.game_vol1.database.AppDatabase
import com.example.game_vol1.database.entity.GameResultEntity
import com.example.game_vol1.database.entity.GeoLocationEntity
import com.example.game_vol1.database.entity.PlayerEntity
import kotlinx.coroutines.flow.Flow

class AdminRepository(private val db: AppDatabase) {

    fun getAllPlayers(): Flow<List<PlayerEntity>> = db.playerDao().getAllPlayers()
    fun getPlayerById(id: Int): Flow<PlayerEntity?> = db.playerDao().getPlayerById(id)
    fun searchPlayers(query: String): Flow<List<PlayerEntity>> = db.playerDao().searchPlayers(query)
    fun getPlayerCount(): Flow<Int> = db.playerDao().getPlayerCount()
    suspend fun deletePlayer(player: PlayerEntity): Result<Unit> = runCatching {
        db.playerDao().deletePlayer(player)
    }

    fun getAllGeoLocations(): Flow<List<GeoLocationEntity>> = db.geoLocationDao().getAllGeoLocations()
    fun searchGeoLocations(q: String): Flow<List<GeoLocationEntity>> = db.geoLocationDao().searchGeoLocations(q)
    fun filterByDifficulty(d: String): Flow<List<GeoLocationEntity>> = db.geoLocationDao().filterByDifficulty(d)
    fun getGeoLocationCount(): Flow<Int> = db.geoLocationDao().getGeoLocationCount()
    suspend fun addGeoLocation(geo: GeoLocationEntity): Result<Unit> = runCatching {
        db.geoLocationDao().insertGeoLocation(geo)
    }.map { Unit }

    suspend fun updateGeoLocation(geo: GeoLocationEntity): Result<Unit> = runCatching {
        db.geoLocationDao().updateGeoLocation(geo)
    }

    suspend fun deleteGeoLocation(geo: GeoLocationEntity): Result<Unit> = runCatching {
        db.geoLocationDao().deleteGeoLocation(geo)
    }

    fun getResultsForPlayer(playerId: Int): Flow<List<GameResultEntity>> = db.gameResultDao().getResultsForPlayer(playerId)
    fun getTotalGamesCount(): Flow<Int> = db.gameResultDao().getTotalGamesCount()
    suspend fun getAverageScore(playerId: Int): Result<Double?> = runCatching {
        db.gameResultDao().getAverageScoreForPlayer(playerId)
    }
}
