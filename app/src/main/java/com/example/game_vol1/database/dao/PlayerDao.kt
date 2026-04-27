package com.example.game_vol1.database.dao

import androidx.room.*
import com.example.game_vol1.database.entity.PlayerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayerDao {
    @Query("SELECT * FROM players WHERE isAdmin = 0 ORDER BY totalScore DESC")
    fun getAllPlayers(): Flow<List<PlayerEntity>>

    @Query("SELECT * FROM players WHERE id = :id")
    fun getPlayerById(id: Int): Flow<PlayerEntity?>

    @Query("SELECT * FROM players WHERE isAdmin = 0 AND (username LIKE '%' || :q || '%' OR email LIKE '%' || :q || '%') ORDER BY totalScore DESC")
    fun searchPlayers(q: String): Flow<List<PlayerEntity>>

    @Query("SELECT COUNT(*) FROM players WHERE isAdmin = 0")
    fun getPlayerCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlayer(player: PlayerEntity): Long

    @Update
    suspend fun updatePlayer(player: PlayerEntity)

    @Delete
    suspend fun deletePlayer(player: PlayerEntity)
}
