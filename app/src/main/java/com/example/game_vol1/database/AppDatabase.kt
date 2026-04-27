package com.example.game_vol1.database

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.game_vol1.database.dao.GameResultDao
import com.example.game_vol1.database.dao.GeoLocationDao
import com.example.game_vol1.database.dao.PlayerDao
import com.example.game_vol1.database.entity.GameResultEntity
import com.example.game_vol1.database.entity.GeoLocationEntity
import com.example.game_vol1.database.entity.PlayerEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [PlayerEntity::class, GeoLocationEntity::class, GameResultEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun playerDao(): PlayerDao
    abstract fun geoLocationDao(): GeoLocationDao
    abstract fun gameResultDao(): GameResultDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "geoguesser_admin.db"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Post to main-thread handler so the onCreate transaction
                            // is fully closed before we open DAO transactions for seeding.
                            Handler(Looper.getMainLooper()).post {
                                INSTANCE?.let { database ->
                                    CoroutineScope(Dispatchers.IO).launch {
                                        try { seed(database) } catch (_: Exception) {}
                                    }
                                }
                            }
                        }
                    })
                    .build()
                    .also { INSTANCE = it }
            }

        private suspend fun seed(db: AppDatabase) {
            val now = System.currentTimeMillis()
            val day = 86_400_000L

            val g1 = db.geoLocationDao().insertGeoLocation(GeoLocationEntity(name = "Eiffel Tower", country = "France", city = "Paris", latitude = 48.8584, longitude = 2.2945, difficulty = "Easy", description = "Iconic iron lattice tower on the Champ de Mars.")).toInt()
            val g2 = db.geoLocationDao().insertGeoLocation(GeoLocationEntity(name = "Colosseum", country = "Italy", city = "Rome", latitude = 41.8902, longitude = 12.4922, difficulty = "Easy", description = "Ancient amphitheater in the center of Rome.")).toInt()
            val g3 = db.geoLocationDao().insertGeoLocation(GeoLocationEntity(name = "Machu Picchu", country = "Peru", city = "Cusco Region", latitude = -13.1631, longitude = -72.5450, difficulty = "Hard", description = "15th-century Inca citadel high in the Andes.")).toInt()
            val g4 = db.geoLocationDao().insertGeoLocation(GeoLocationEntity(name = "Taj Mahal", country = "India", city = "Agra", latitude = 27.1751, longitude = 78.0421, difficulty = "Medium", description = "Ivory-white marble mausoleum on the Yamuna river.")).toInt()
            val g5 = db.geoLocationDao().insertGeoLocation(GeoLocationEntity(name = "Great Wall of China", country = "China", city = "Beijing", latitude = 40.4319, longitude = 116.5704, difficulty = "Medium", description = "Ancient fortification stretching across northern China.")).toInt()
            db.geoLocationDao().insertGeoLocation(GeoLocationEntity(name = "Santorini Caldera", country = "Greece", city = "Santorini", latitude = 36.4618, longitude = 25.3758, difficulty = "Medium", description = "Volcanic island with white-washed buildings and blue domes."))
            db.geoLocationDao().insertGeoLocation(GeoLocationEntity(name = "Angkor Wat", country = "Cambodia", city = "Siem Reap", latitude = 13.4125, longitude = 103.8670, difficulty = "Hard", description = "Massive temple complex and UNESCO World Heritage Site."))

            val p1 = db.playerDao().insertPlayer(PlayerEntity(username = "traveler_mike", email = "mike@example.com", totalScore = 8420, gamesPlayed = 47, bestScore = 4950, correctGuesses = 89, totalGuesses = 112, registrationDate = now - day * 90, lastPlayedDate = now - day)).toInt()
            val p2 = db.playerDao().insertPlayer(PlayerEntity(username = "geo_sophie", email = "sophie@example.com", totalScore = 6310, gamesPlayed = 35, bestScore = 3800, correctGuesses = 72, totalGuesses = 95, registrationDate = now - day * 60, lastPlayedDate = now - day * 3)).toInt()
            val p3 = db.playerDao().insertPlayer(PlayerEntity(username = "world_alex", email = "alex@example.com", totalScore = 4750, gamesPlayed = 22, bestScore = 2900, correctGuesses = 51, totalGuesses = 76, registrationDate = now - day * 30, lastPlayedDate = now - day * 7)).toInt()
            val p4 = db.playerDao().insertPlayer(PlayerEntity(username = "navigator_jana", email = "jana@example.com", totalScore = 3280, gamesPlayed = 18, bestScore = 2100, correctGuesses = 38, totalGuesses = 60, registrationDate = now - day * 14, lastPlayedDate = now - day * 2)).toInt()
            val p5 = db.playerDao().insertPlayer(PlayerEntity(username = "rookie_tom", email = "tom@example.com", totalScore = 920, gamesPlayed = 7, bestScore = 680, correctGuesses = 11, totalGuesses = 21, registrationDate = now - day * 5, lastPlayedDate = now - day * 5)).toInt()

            db.gameResultDao().insertResult(GameResultEntity(playerId = p1, geoLocationId = g1, geoLocationName = "Eiffel Tower", score = 4950, isCorrect = true, distanceKm = 0.3, playedAt = now - day, timeTakenSeconds = 45))
            db.gameResultDao().insertResult(GameResultEntity(playerId = p1, geoLocationId = g3, geoLocationName = "Machu Picchu", score = 1200, isCorrect = false, distanceKm = 145.2, playedAt = now - day * 2, timeTakenSeconds = 120))
            db.gameResultDao().insertResult(GameResultEntity(playerId = p1, geoLocationId = g2, geoLocationName = "Colosseum", score = 3800, isCorrect = true, distanceKm = 0.8, playedAt = now - day * 3, timeTakenSeconds = 60))
            db.gameResultDao().insertResult(GameResultEntity(playerId = p2, geoLocationId = g4, geoLocationName = "Taj Mahal", score = 3800, isCorrect = true, distanceKm = 2.1, playedAt = now - day * 3, timeTakenSeconds = 90))
            db.gameResultDao().insertResult(GameResultEntity(playerId = p2, geoLocationId = g5, geoLocationName = "Great Wall of China", score = 2510, isCorrect = true, distanceKm = 5.4, playedAt = now - day * 5, timeTakenSeconds = 80))
            db.gameResultDao().insertResult(GameResultEntity(playerId = p3, geoLocationId = g3, geoLocationName = "Machu Picchu", score = 2900, isCorrect = true, distanceKm = 1.5, playedAt = now - day * 7, timeTakenSeconds = 75))
            db.gameResultDao().insertResult(GameResultEntity(playerId = p3, geoLocationId = g1, geoLocationName = "Eiffel Tower", score = 1850, isCorrect = false, distanceKm = 210.0, playedAt = now - day * 10, timeTakenSeconds = 110))
            db.gameResultDao().insertResult(GameResultEntity(playerId = p4, geoLocationId = g2, geoLocationName = "Colosseum", score = 2100, isCorrect = true, distanceKm = 3.2, playedAt = now - day * 2, timeTakenSeconds = 95))
            db.gameResultDao().insertResult(GameResultEntity(playerId = p5, geoLocationId = g4, geoLocationName = "Taj Mahal", score = 680, isCorrect = false, distanceKm = 890.0, playedAt = now - day * 5, timeTakenSeconds = 180))
        }
    }
}
