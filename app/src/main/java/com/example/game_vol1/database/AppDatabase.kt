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

            val g1 = db.geoLocationDao().insertGeoLocation(
                GeoLocationEntity(
                    name = "Alexander Nevsky Cathedral",
                    country = "Bulgaria",
                    city = "Sofia",
                    latitude = 42.6953,
                    longitude = 23.3328,
                    difficulty = "Easy",
                    description = "Landmark cathedral in central Sofia."
                )
            ).toInt()
            val g2 = db.geoLocationDao().insertGeoLocation(
                GeoLocationEntity(
                    name = "Rila Monastery",
                    country = "Bulgaria",
                    city = "Rila",
                    latitude = 42.1338,
                    longitude = 23.3405,
                    difficulty = "Medium",
                    description = "Historic Orthodox monastery in the Rila Mountains."
                )
            ).toInt()
            val g3 = db.geoLocationDao().insertGeoLocation(
                GeoLocationEntity(
                    name = "Ancient Theatre of Plovdiv",
                    country = "Bulgaria",
                    city = "Plovdiv",
                    latitude = 42.1466,
                    longitude = 24.7510,
                    difficulty = "Medium",
                    description = "Well-preserved Roman amphitheatre in Old Town Plovdiv."
                )
            ).toInt()
            val g4 = db.geoLocationDao().insertGeoLocation(
                GeoLocationEntity(
                    name = "Tsarevets Fortress",
                    country = "Bulgaria",
                    city = "Veliko Tarnovo",
                    latitude = 43.0841,
                    longitude = 25.6506,
                    difficulty = "Hard",
                    description = "Medieval hill fortress and key historic site."
                )
            ).toInt()
            val g5 = db.geoLocationDao().insertGeoLocation(
                GeoLocationEntity(
                    name = "Old Nessebar",
                    country = "Bulgaria",
                    city = "Nessebar",
                    latitude = 42.6598,
                    longitude = 27.7360,
                    difficulty = "Easy",
                    description = "Ancient coastal town and UNESCO world heritage site."
                )
            ).toInt()
            db.geoLocationDao().insertGeoLocation(
                GeoLocationEntity(
                    name = "Belogradchik Rocks",
                    country = "Bulgaria",
                    city = "Belogradchik",
                    latitude = 43.6271,
                    longitude = 22.6838,
                    difficulty = "Hard",
                    description = "Striking red sandstone rock formations and fortress walls."
                )
            )
            db.geoLocationDao().insertGeoLocation(
                GeoLocationEntity(
                    name = "Seven Rila Lakes",
                    country = "Bulgaria",
                    city = "Sapareva Banya",
                    latitude = 42.2122,
                    longitude = 23.3137,
                    difficulty = "Medium",
                    description = "Famous alpine cirque of glacial lakes in Rila."
                )
            )

            val p1 = db.playerDao().insertPlayer(PlayerEntity(username = "traveler_mike", email = "mike@example.com", totalScore = 8420, gamesPlayed = 47, bestScore = 4950, correctGuesses = 89, totalGuesses = 112, registrationDate = now - day * 90, lastPlayedDate = now - day)).toInt()
            val p2 = db.playerDao().insertPlayer(PlayerEntity(username = "geo_sophie", email = "sophie@example.com", totalScore = 6310, gamesPlayed = 35, bestScore = 3800, correctGuesses = 72, totalGuesses = 95, registrationDate = now - day * 60, lastPlayedDate = now - day * 3)).toInt()
            val p3 = db.playerDao().insertPlayer(PlayerEntity(username = "world_alex", email = "alex@example.com", totalScore = 4750, gamesPlayed = 22, bestScore = 2900, correctGuesses = 51, totalGuesses = 76, registrationDate = now - day * 30, lastPlayedDate = now - day * 7)).toInt()
            val p4 = db.playerDao().insertPlayer(PlayerEntity(username = "navigator_jana", email = "jana@example.com", totalScore = 3280, gamesPlayed = 18, bestScore = 2100, correctGuesses = 38, totalGuesses = 60, registrationDate = now - day * 14, lastPlayedDate = now - day * 2)).toInt()
            val p5 = db.playerDao().insertPlayer(PlayerEntity(username = "rookie_tom", email = "tom@example.com", totalScore = 920, gamesPlayed = 7, bestScore = 680, correctGuesses = 11, totalGuesses = 21, registrationDate = now - day * 5, lastPlayedDate = now - day * 5)).toInt()

            db.gameResultDao().insertResult(GameResultEntity(playerId = p1, geoLocationId = g1, geoLocationName = "Alexander Nevsky Cathedral", score = 4950, isCorrect = true, distanceKm = 0.3, playedAt = now - day, timeTakenSeconds = 45))
            db.gameResultDao().insertResult(GameResultEntity(playerId = p1, geoLocationId = g3, geoLocationName = "Ancient Theatre of Plovdiv", score = 1200, isCorrect = false, distanceKm = 145.2, playedAt = now - day * 2, timeTakenSeconds = 120))
            db.gameResultDao().insertResult(GameResultEntity(playerId = p1, geoLocationId = g2, geoLocationName = "Rila Monastery", score = 3800, isCorrect = true, distanceKm = 0.8, playedAt = now - day * 3, timeTakenSeconds = 60))
            db.gameResultDao().insertResult(GameResultEntity(playerId = p2, geoLocationId = g4, geoLocationName = "Tsarevets Fortress", score = 3800, isCorrect = true, distanceKm = 2.1, playedAt = now - day * 3, timeTakenSeconds = 90))
            db.gameResultDao().insertResult(GameResultEntity(playerId = p2, geoLocationId = g5, geoLocationName = "Old Nessebar", score = 2510, isCorrect = true, distanceKm = 5.4, playedAt = now - day * 5, timeTakenSeconds = 80))
            db.gameResultDao().insertResult(GameResultEntity(playerId = p3, geoLocationId = g3, geoLocationName = "Ancient Theatre of Plovdiv", score = 2900, isCorrect = true, distanceKm = 1.5, playedAt = now - day * 7, timeTakenSeconds = 75))
            db.gameResultDao().insertResult(GameResultEntity(playerId = p3, geoLocationId = g1, geoLocationName = "Alexander Nevsky Cathedral", score = 1850, isCorrect = false, distanceKm = 210.0, playedAt = now - day * 10, timeTakenSeconds = 110))
            db.gameResultDao().insertResult(GameResultEntity(playerId = p4, geoLocationId = g2, geoLocationName = "Rila Monastery", score = 2100, isCorrect = true, distanceKm = 3.2, playedAt = now - day * 2, timeTakenSeconds = 95))
            db.gameResultDao().insertResult(GameResultEntity(playerId = p5, geoLocationId = g4, geoLocationName = "Tsarevets Fortress", score = 680, isCorrect = false, distanceKm = 890.0, playedAt = now - day * 5, timeTakenSeconds = 180))
        }
    }
}
