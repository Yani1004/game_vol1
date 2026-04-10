package com.example.game_vol1.data

import android.content.Context
import com.example.game_vol1.models.DailyChallenge
import com.example.game_vol1.models.HeritagePlace
import com.example.game_vol1.models.LandmarkRound
import com.example.game_vol1.models.PlaceVisit
import com.example.game_vol1.models.PlayerProfile
import com.example.game_vol1.models.TeamInfo
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

object GameRepository {
    private const val PREFS_NAME = "heritage_hunt_prefs"
    private const val KEY_NAME = "name"
    private const val KEY_EMAIL = "email"
    private const val KEY_PASSWORD = "password"
    private const val KEY_ACTIVE_EMAIL = "active_email"
    private const val KEY_TOTAL_SCORE = "total_score"
    private const val KEY_VISITS = "visits"
    private const val KEY_COMPLETED_DAILY = "completed_daily"
    private const val KEY_TEAM_DIRECTORY = "team_directory"
    private const val KEY_CURRENT_TEAM_CODE = "current_team_code"

    private const val DISCOVERY_RADIUS_METERS = 250f
    private const val DAILY_BONUS_POINTS = 150

    fun getPlaces(): List<HeritagePlace> = listOf(
        HeritagePlace("alexander_nevsky", "Alexander Nevsky Cathedral", "Sofia", "Bulgaria", 42.6953, 23.3328, "A landmark cathedral in central Sofia with striking golden domes.", "Built in the early 20th century, the cathedral honors the Russian soldiers who died during the Russo-Turkish War of 1877-1878.", "Cathedral"),
        HeritagePlace("rila_monastery", "Rila Monastery", "Rila", "Bulgaria", 42.1338, 23.3405, "A mountain monastery surrounded by dramatic forest scenery.", "Founded in the 10th century by Saint Ivan of Rila, it became one of the country's most important spiritual and cultural centers.", "Monastery"),
        HeritagePlace("plovdiv_theatre", "Ancient Theatre of Plovdiv", "Plovdiv", "Bulgaria", 42.1466, 24.7510, "A remarkably preserved Roman theatre still used for events today.", "The theatre dates to the 1st century AD and is one of the best-preserved ancient theatres in the Balkans.", "Ancient Site"),
        HeritagePlace("tsarevets", "Tsarevets Fortress", "Veliko Tarnovo", "Bulgaria", 43.0841, 25.6506, "A hilltop fortress overlooking the old capital of Bulgaria.", "Tsarevets served as the main stronghold of the Second Bulgarian Empire and remains one of the country's most symbolic medieval sites.", "Fortress"),
        HeritagePlace("nessebar_old_town", "Old Nessebar", "Nessebar", "Bulgaria", 42.6598, 27.7360, "A historic peninsula town with churches, stone streets, and sea views.", "Nessebar has Thracian, Greek, Roman, and Byzantine layers of history and is listed as a UNESCO World Heritage site.", "Historic Town"),
        HeritagePlace("belogradchik_rocks", "Belogradchik Rocks", "Belogradchik", "Bulgaria", 43.6271, 22.6838, "Towering red rock formations paired with an old fortress.", "The natural rock formations stretch across a vast area and have inspired legends, local names, and strategic fortification for centuries.", "Natural Wonder"),
    )

    fun getAllRounds(): List<LandmarkRound> = getPlaces().map { place ->
        LandmarkRound(
            id = place.id,
            title = place.title,
            city = place.city,
            country = place.country,
            clue = place.shortDescription,
            funFact = place.historicalInfo,
            latitude = place.latitude,
            longitude = place.longitude,
            difficulty = place.category,
            unlockScore = 3500,
        )
    }

    fun hasRegisteredAccount(context: Context): Boolean = prefs(context).getString(KEY_EMAIL, null) != null

    fun register(context: Context, name: String, email: String, password: String): Boolean {
        if (hasRegisteredAccount(context)) return false
        prefs(context).edit()
            .putString(KEY_NAME, name.trim())
            .putString(KEY_EMAIL, email.trim().lowercase())
            .putString(KEY_PASSWORD, password)
            .putString(KEY_ACTIVE_EMAIL, email.trim().lowercase())
            .putInt(KEY_TOTAL_SCORE, 0)
            .putString(KEY_VISITS, "")
            .putString(KEY_COMPLETED_DAILY, "")
            .apply()
        return true
    }

    fun login(context: Context, email: String, password: String): Boolean {
        val registeredEmail = prefs(context).getString(KEY_EMAIL, null)?.lowercase()
        val registeredPassword = prefs(context).getString(KEY_PASSWORD, null)
        val matches = registeredEmail == email.trim().lowercase() && registeredPassword == password
        if (matches) {
            prefs(context).edit().putString(KEY_ACTIVE_EMAIL, registeredEmail).apply()
        }
        return matches
    }

    fun logout(context: Context) {
        prefs(context).edit().remove(KEY_ACTIVE_EMAIL).apply()
    }

    fun isLoggedIn(context: Context): Boolean = prefs(context).getString(KEY_ACTIVE_EMAIL, null) != null

    fun loadProfile(context: Context): PlayerProfile {
        val visits = loadVisits(context)
        return PlayerProfile(
            username = prefs(context).getString(KEY_NAME, "Explorer") ?: "Explorer",
            email = prefs(context).getString(KEY_EMAIL, "") ?: "",
            totalScore = prefs(context).getInt(KEY_TOTAL_SCORE, 0),
            visitedCount = visits.size,
            discoveredPlaceIds = visits.map { it.placeId }.toSet(),
            completedDailyDate = prefs(context).getString(KEY_COMPLETED_DAILY, "") ?: "",
        )
    }

    fun loadTeam(context: Context): TeamInfo {
        ensureTeamDirectory(context)
        val currentCode = prefs(context).getString(KEY_CURRENT_TEAM_CODE, "") ?: ""
        return loadAllTeams(context).firstOrNull { it.inviteCode == currentCode } ?: TeamInfo()
    }

    fun createTeam(
        context: Context,
        teamName: String,
        description: String,
        maxMembers: Int,
        isOpen: Boolean,
    ): TeamInfo? {
        ensureTeamDirectory(context)
        if (loadTeam(context).hasTeam) return null
        val profile = loadProfile(context)
        val code = buildInviteCode(teamName, profile.username)
        val newTeam = TeamInfo(
            teamName = teamName.trim(),
            description = description.trim(),
            inviteCode = code,
            ownerEmail = profile.email,
            maxMembers = maxMembers,
            isOpen = isOpen,
            memberNames = listOf(profile.username),
            pendingRequests = emptyList(),
            memberScores = mapOf(profile.username to 0),
            teamScore = 0,
        )
        val updatedTeams = loadAllTeams(context) + newTeam
        saveAllTeams(context, updatedTeams)
        prefs(context).edit().putString(KEY_CURRENT_TEAM_CODE, code).apply()
        return loadTeam(context)
    }

    fun joinTeamByCode(context: Context, inviteCode: String): JoinTeamResult {
        ensureTeamDirectory(context)
        if (loadTeam(context).hasTeam) return JoinTeamResult.AlreadyInTeam
        val teams = loadAllTeams(context)
        val target = teams.firstOrNull { it.inviteCode.equals(inviteCode.trim(), ignoreCase = true) }
            ?: return JoinTeamResult.NotFound
        val profile = loadProfile(context)
        if (target.memberNames.size >= target.maxMembers) {
            return JoinTeamResult.TeamFull
        }
        if (target.isOpen) {
            val updatedTarget = target.copy(
                memberNames = (target.memberNames + profile.username).distinct(),
                memberScores = target.memberScores + (profile.username to 0),
            )
            saveAllTeams(context, teams.map { if (it.inviteCode == target.inviteCode) updatedTarget else it })
            prefs(context).edit().putString(KEY_CURRENT_TEAM_CODE, updatedTarget.inviteCode).apply()
            return JoinTeamResult.Joined(updatedTarget)
        }
        if (profile.username in target.pendingRequests) {
            return JoinTeamResult.PendingApproval
        }
        val updatedTarget = target.copy(pendingRequests = target.pendingRequests + profile.username)
        saveAllTeams(context, teams.map { if (it.inviteCode == target.inviteCode) updatedTarget else it })
        return JoinTeamResult.PendingApproval
    }

    fun joinOpenTeam(context: Context, inviteCode: String): JoinTeamResult {
        val team = loadAllTeams(context).firstOrNull { it.inviteCode == inviteCode } ?: return JoinTeamResult.NotFound
        if (!team.isOpen) return JoinTeamResult.NotFound
        return joinTeamByCode(context, inviteCode)
    }

    fun requestJoinLockedTeam(context: Context, inviteCode: String): JoinTeamResult {
        val team = loadAllTeams(context).firstOrNull { it.inviteCode == inviteCode } ?: return JoinTeamResult.NotFound
        if (team.isOpen) return joinTeamByCode(context, inviteCode)
        return joinTeamByCode(context, inviteCode)
    }

    fun getBrowseableTeams(context: Context): List<TeamInfo> {
        ensureTeamDirectory(context)
        val currentCode = prefs(context).getString(KEY_CURRENT_TEAM_CODE, "") ?: ""
        return loadAllTeams(context)
            .filter { it.inviteCode != currentCode }
            .sortedWith(compareByDescending<TeamInfo> { it.teamScore }.thenBy { it.teamName.lowercase() })
    }

    fun getTeamLeaderboard(context: Context): List<TeamInfo> {
        ensureTeamDirectory(context)
        return loadAllTeams(context)
            .sortedWith(compareByDescending<TeamInfo> { it.teamScore }.thenBy { it.teamName.lowercase() })
    }

    fun leaveCurrentTeam(context: Context): TeamInfo? {
        ensureTeamDirectory(context)
        val current = loadTeam(context)
        val profile = loadProfile(context)
        if (!current.hasTeam) return null
        if (current.ownerEmail.equals(profile.email, ignoreCase = true)) return null
        val updated = current.copy(
            memberNames = current.memberNames - profile.username,
            pendingRequests = current.pendingRequests - profile.username,
            memberScores = current.memberScores - profile.username,
        )
        saveAllTeams(context, loadAllTeams(context).map { if (it.inviteCode == current.inviteCode) updated else it })
        prefs(context).edit().remove(KEY_CURRENT_TEAM_CODE).apply()
        return updated
    }

    fun approveRequest(context: Context, requesterName: String): TeamInfo? {
        ensureTeamDirectory(context)
        val team = loadTeam(context)
        if (!isTeamOwner(context) || requesterName !in team.pendingRequests || team.memberNames.size >= team.maxMembers) {
            return null
        }
        val updatedMembers = team.memberNames + requesterName
        val updatedPending = team.pendingRequests - requesterName
        val updatedScores = team.memberScores + (requesterName to 0)
        val updatedTeam = team.copy(
            memberNames = updatedMembers,
            pendingRequests = updatedPending,
            memberScores = updatedScores,
        )
        saveAllTeams(context, loadAllTeams(context).map { if (it.inviteCode == team.inviteCode) updatedTeam else it })
        return updatedTeam
    }

    fun getLeaderboard(context: Context): List<Pair<String, Int>> {
        val team = loadTeam(context)
        return team.memberScores.entries
            .sortedByDescending { it.value }
            .map { it.key to it.value }
    }

    fun isTeamOwner(context: Context): Boolean {
        val profile = loadProfile(context)
        val team = loadTeam(context)
        return team.hasTeam && team.ownerEmail.equals(profile.email, ignoreCase = true)
    }

    fun loadVisits(context: Context): List<PlaceVisit> {
        val raw = prefs(context).getString(KEY_VISITS, "").orEmpty()
        if (raw.isBlank()) return emptyList()
        return raw.split(";")
            .mapNotNull { token ->
                val parts = token.split("|")
                if (parts.size < 3) return@mapNotNull null
                PlaceVisit(parts[0], parts[1].toLongOrNull() ?: return@mapNotNull null, parts[2].toIntOrNull() ?: 0)
            }
            .sortedByDescending { it.visitedAtEpochMs }
    }

    fun placeById(id: String): HeritagePlace? = getPlaces().firstOrNull { it.id == id }

    fun discoveredRounds(context: Context): List<LandmarkRound> =
        loadProfile(context).discoveredPlaceIds.mapNotNull { id ->
            getAllRounds().firstOrNull { it.id == id }
        }

    fun getDailyChallenge(): DailyChallenge {
        val today = LocalDate.now()
        val place = getPlaces()[today.dayOfYear % getPlaces().size]
        return DailyChallenge(today.toString(), place, DAILY_BONUS_POINTS, "Travel to ${place.title} in ${place.city} and discover it today for a bonus.")
    }

    fun distanceMeters(userLatitude: Double, userLongitude: Double, place: HeritagePlace): Float {
        val results = FloatArray(1)
        android.location.Location.distanceBetween(userLatitude, userLongitude, place.latitude, place.longitude, results)
        return results[0]
    }

    fun discoveryRadiusMeters(): Float = DISCOVERY_RADIUS_METERS

    fun canDiscover(distanceMeters: Float): Boolean = distanceMeters <= DISCOVERY_RADIUS_METERS

    fun distanceToScore(distanceKm: Double): Int {
        val raw = 5000 - (distanceKm * 12)
        return raw.toInt().coerceIn(500, 5000)
    }

    fun accuracyLabel(distanceKm: Double): String = when {
        distanceKm < 5 -> "Perfect pin"
        distanceKm < 25 -> "Excellent"
        distanceKm < 75 -> "Strong guess"
        distanceKm < 150 -> "Solid"
        distanceKm < 300 -> "Close-ish"
        else -> "Way off"
    }

    fun discoverPlace(context: Context, place: HeritagePlace, userLatitude: Double, userLongitude: Double): DiscoveryOutcome {
        val distance = distanceMeters(userLatitude, userLongitude, place)
        if (!canDiscover(distance)) return DiscoveryOutcome(false, distance, 0, false)

        val visits = loadVisits(context)
        val isFirstVisit = visits.none { it.placeId == place.id }
        val daily = getDailyChallenge()
        val today = LocalDate.now().toString()
        val dailyBonusAwarded = daily.place.id == place.id && prefs(context).getString(KEY_COMPLETED_DAILY, "") != today
        val awardedPoints = (if (isFirstVisit) 100 else 35) + if (dailyBonusAwarded) daily.bonusPoints else 0

        val updatedVisits = listOf(PlaceVisit(place.id, System.currentTimeMillis(), awardedPoints)) + visits
        val team = loadTeam(context)
        val profile = loadProfile(context)
        val updatedMemberScores = if (team.hasTeam && profile.username in team.memberNames) {
            team.memberScores + (profile.username to ((team.memberScores[profile.username] ?: 0) + awardedPoints))
        } else {
            team.memberScores
        }
        val editor = prefs(context).edit()
            .putString(KEY_VISITS, updatedVisits.joinToString(";") { "${it.placeId}|${it.visitedAtEpochMs}|${it.pointsEarned}" })
            .putInt(KEY_TOTAL_SCORE, loadProfile(context).totalScore + awardedPoints)
        if (dailyBonusAwarded) editor.putString(KEY_COMPLETED_DAILY, today)
        editor.apply()

        if (team.hasTeam && profile.username in team.memberNames) {
            val updatedTeam = team.copy(
                teamScore = team.teamScore + awardedPoints,
                memberScores = updatedMemberScores,
            )
            saveAllTeams(context, loadAllTeams(context).map { if (it.inviteCode == team.inviteCode) updatedTeam else it })
        }

        return DiscoveryOutcome(true, distance, awardedPoints, dailyBonusAwarded)
    }

    fun saveGameResult(context: Context, sessionScore: Int, discoveredThisRun: Set<String>) {
        val current = loadProfile(context)
        val syntheticVisits = discoveredThisRun.map {
            PlaceVisit(it, System.currentTimeMillis(), sessionScore / maxOf(1, discoveredThisRun.size))
        } + loadVisits(context)
        prefs(context).edit()
            .putString(KEY_VISITS, syntheticVisits.joinToString(";") { "${it.placeId}|${it.visitedAtEpochMs}|${it.pointsEarned}" })
            .putInt(KEY_TOTAL_SCORE, current.totalScore + sessionScore)
            .apply()
    }

    fun formatVisitTime(epochMs: Long): String {
        val local = Instant.ofEpochMilli(epochMs).atZone(ZoneId.systemDefault())
        return "${local.toLocalDate()} ${local.toLocalTime().withSecond(0).withNano(0)}"
    }

    private fun prefs(context: Context) = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private fun ensureTeamDirectory(context: Context) {
        if ((prefs(context).getString(KEY_TEAM_DIRECTORY, "") ?: "").isNotBlank()) return
        val seeded = listOf(
            TeamInfo(
                teamName = "City Sprinters",
                description = "Fast-moving explorers focused on central city landmarks.",
                inviteCode = "CITY2401",
                ownerEmail = "owner1@demo.local",
                maxMembers = 6,
                isOpen = true,
                memberNames = listOf("Mira", "Niki", "Toni"),
                pendingRequests = emptyList(),
                memberScores = mapOf("Mira" to 430, "Niki" to 290, "Toni" to 185),
                teamScore = 905,
            ),
            TeamInfo(
                teamName = "Fortress Finders",
                description = "A locked team for players who love medieval places and long walks.",
                inviteCode = "FORT5512",
                ownerEmail = "owner2@demo.local",
                maxMembers = 5,
                isOpen = false,
                memberNames = listOf("Alex", "Bobi"),
                pendingRequests = listOf("Kris"),
                memberScores = mapOf("Alex" to 510, "Bobi" to 360),
                teamScore = 870,
            ),
            TeamInfo(
                teamName = "Sea Legends",
                description = "Coastal place hunters collecting old towns and sea views.",
                inviteCode = "SEA7730",
                ownerEmail = "owner3@demo.local",
                maxMembers = 4,
                isOpen = true,
                memberNames = listOf("Vesi"),
                pendingRequests = emptyList(),
                memberScores = mapOf("Vesi" to 260),
                teamScore = 260,
            ),
        )
        saveAllTeams(context, seeded)
    }

    private fun loadAllTeams(context: Context): List<TeamInfo> {
        val raw = prefs(context).getString(KEY_TEAM_DIRECTORY, "[]") ?: "[]"
        val array = JSONArray(raw)
        return buildList {
            for (i in 0 until array.length()) {
                val obj = array.optJSONObject(i) ?: continue
                add(
                    TeamInfo(
                        teamName = obj.optString("teamName"),
                        description = obj.optString("description"),
                        inviteCode = obj.optString("inviteCode"),
                        ownerEmail = obj.optString("ownerEmail"),
                        maxMembers = obj.optInt("maxMembers", 4),
                        isOpen = obj.optBoolean("isOpen", true),
                        memberNames = jsonArrayToList(obj.optJSONArray("memberNames")),
                        pendingRequests = jsonArrayToList(obj.optJSONArray("pendingRequests")),
                        memberScores = jsonObjectToScores(obj.optJSONObject("memberScores")),
                        teamScore = obj.optInt("teamScore", 0),
                    ),
                )
            }
        }
    }

    private fun saveAllTeams(context: Context, teams: List<TeamInfo>) {
        val array = JSONArray()
        teams.forEach { team ->
            val obj = JSONObject()
            obj.put("teamName", team.teamName)
            obj.put("description", team.description)
            obj.put("inviteCode", team.inviteCode)
            obj.put("ownerEmail", team.ownerEmail)
            obj.put("maxMembers", team.maxMembers)
            obj.put("isOpen", team.isOpen)
            obj.put("memberNames", JSONArray(team.memberNames))
            obj.put("pendingRequests", JSONArray(team.pendingRequests))
            obj.put("teamScore", team.teamScore)
            val scores = JSONObject()
            team.memberScores.forEach { (name, score) -> scores.put(name, score) }
            obj.put("memberScores", scores)
            array.put(obj)
        }
        prefs(context).edit().putString(KEY_TEAM_DIRECTORY, array.toString()).apply()
    }

    private fun jsonArrayToList(array: JSONArray?): List<String> =
        buildList {
            if (array == null) return@buildList
            for (i in 0 until array.length()) {
                add(array.optString(i))
            }
        }.filter { it.isNotBlank() }

    private fun jsonObjectToScores(obj: JSONObject?): Map<String, Int> {
        if (obj == null) return emptyMap()
        val result = mutableMapOf<String, Int>()
        val keys = obj.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            result[key] = obj.optInt(key, 0)
        }
        return result
    }

    private fun buildInviteCode(teamName: String, username: String): String {
        val seed = "${teamName.take(3)}${username.take(2)}${System.currentTimeMillis().toString().takeLast(4)}"
        return seed.uppercase().replace(" ", "").take(8)
    }
}

sealed class JoinTeamResult {
    data class Joined(val team: TeamInfo) : JoinTeamResult()
    data object PendingApproval : JoinTeamResult()
    data object TeamFull : JoinTeamResult()
    data object NotFound : JoinTeamResult()
    data object AlreadyInTeam : JoinTeamResult()
}

data class DiscoveryOutcome(
    val success: Boolean,
    val distanceMeters: Float,
    val pointsAwarded: Int,
    val dailyBonusAwarded: Boolean,
)
