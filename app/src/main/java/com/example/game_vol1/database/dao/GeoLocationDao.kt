package com.example.game_vol1.database.dao

import androidx.room.*
import com.example.game_vol1.database.entity.GeoLocationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GeoLocationDao {
    @Query("SELECT * FROM geolocations ORDER BY createdAt DESC")
    fun getAllGeoLocations(): Flow<List<GeoLocationEntity>>

    @Query("SELECT * FROM geolocations WHERE country LIKE '%' || :q || '%' OR name LIKE '%' || :q || '%' OR city LIKE '%' || :q || '%' ORDER BY createdAt DESC")
    fun searchGeoLocations(q: String): Flow<List<GeoLocationEntity>>

    @Query("SELECT * FROM geolocations WHERE difficulty = :difficulty ORDER BY createdAt DESC")
    fun filterByDifficulty(difficulty: String): Flow<List<GeoLocationEntity>>

    @Query("SELECT COUNT(*) FROM geolocations")
    fun getGeoLocationCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGeoLocation(geo: GeoLocationEntity): Long

    @Update
    suspend fun updateGeoLocation(geo: GeoLocationEntity)

    @Delete
    suspend fun deleteGeoLocation(geo: GeoLocationEntity)
}
