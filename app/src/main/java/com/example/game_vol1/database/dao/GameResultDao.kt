package com.example.game_vol1.database.dao

import androidx.room.*
import com.example.game_vol1.database.entity.GameResultEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GameResultDao {
    @Query("SELECT * FROM game_results WHERE playerId = :playerId ORDER BY playedAt DESC")
    fun getResultsForPlayer(playerId: Int): Flow<List<GameResultEntity>>

    @Query("SELECT COUNT(*) FROM game_results")
    fun getTotalGamesCount(): Flow<Int>

    @Query("SELECT AVG(score) FROM game_results WHERE playerId = :playerId")
    suspend fun getAverageScoreForPlayer(playerId: Int): Double?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResult(result: GameResultEntity): Long

    @Delete
    suspend fun deleteResult(result: GameResultEntity)
}
