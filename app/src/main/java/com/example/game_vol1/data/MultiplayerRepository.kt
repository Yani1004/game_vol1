package com.example.game_vol1.data

import android.content.Context
import com.example.game_vol1.models.HeritagePlace
import com.example.game_vol1.models.PlayerProfile
import com.example.game_vol1.models.TeamInfo
import com.google.firebase.FirebaseApp
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions

object MultiplayerRepository {
    data class LeaderboardEntry(
        val username: String,
        val totalScore: Int,
        val visitedCount: Int,
        val rank: Int = 0,
    )

    fun isAvailable(context: Context): Boolean = FirebaseApp.getApps(context).isNotEmpty()

    fun currentUserId(context: Context): String? {
        if (!isAvailable(context)) return null
        return FirebaseAuth.getInstance().currentUser?.uid
    }

    fun register(
        context: Context,
        username: String,
        email: String,
        password: String,
        onResult: (Boolean, String?) -> Unit,
    ) {
        if (!isAvailable(context)) {
            onResult(false, "Firebase is not configured on this build.")
            return
        }

        FirebaseAuth.getInstance()
            .createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid
                if (uid == null) {
                    onResult(false, "Firebase did not return a user id.")
                    return@addOnSuccessListener
                }
                val profile = mapOf(
                    "username" to username,
                    "email" to email.trim().lowercase(),
                    "totalScore" to 0,
                    "visitedCount" to 0,
                    "currentTeamCode" to "",
                    "updatedAt" to FieldValue.serverTimestamp(),
                    "lastSeenAt" to FieldValue.serverTimestamp(),
                )
                FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(uid)
                    .set(profile, SetOptions.merge())
                    .addOnSuccessListener { onResult(true, null) }
                    .addOnFailureListener { onResult(false, it.localizedMessage) }
            }
            .addOnFailureListener { onResult(false, it.localizedMessage) }
    }

    fun login(
        context: Context,
        email: String,
        password: String,
        onResult: (Boolean, String?) -> Unit,
    ) {
        if (!isAvailable(context)) {
            onResult(false, "Firebase is not configured on this build.")
            return
        }

        FirebaseAuth.getInstance()
            .signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                touchCurrentUser(context)
                onResult(true, null)
            }
            .addOnFailureListener { onResult(false, it.localizedMessage) }
    }

    fun logout(context: Context) {
        if (isAvailable(context)) {
            FirebaseAuth.getInstance().signOut()
        }
    }

    fun syncLocalProfile(context: Context) {
        val uid = currentUserId(context) ?: return
        val profile = GameRepository.loadProfile(context)
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(uid)
            .set(
                mapOf(
                    "username" to profile.username,
                    "email" to profile.email,
                    "totalScore" to profile.totalScore,
                    "visitedCount" to profile.visitedCount,
                    "updatedAt" to FieldValue.serverTimestamp(),
                    "lastSeenAt" to FieldValue.serverTimestamp(),
                ),
                SetOptions.merge(),
            )
    }

    fun loadRemoteProfile(context: Context, onResult: (PlayerProfile?) -> Unit) {
        val uid = currentUserId(context)
        if (uid == null) {
            onResult(null)
            return
        }

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { doc ->
                if (!doc.exists()) {
                    onResult(null)
                    return@addOnSuccessListener
                }
                onResult(
                    PlayerProfile(
                        username = doc.getString("username") ?: "Explorer",
                        email = doc.getString("email") ?: "",
                        totalScore = (doc.getLong("totalScore") ?: 0L).toInt(),
                        visitedCount = (doc.getLong("visitedCount") ?: 0L).toInt(),
                    ),
                )
            }
            .addOnFailureListener { onResult(null) }
    }

    fun recordDiscovery(
        context: Context,
        place: HeritagePlace,
        pointsAwarded: Int,
        distanceMeters: Float,
        dailyBonusAwarded: Boolean,
    ) {
        val uid = currentUserId(context) ?: return
        val profile = GameRepository.loadProfile(context)
        val db = FirebaseFirestore.getInstance()
        val userRef = db.collection("users").document(uid)
        val visitRef = userRef.collection("visits").document(place.id)

        db.runTransaction { transaction ->
            val visitSnapshot = transaction.get(visitRef)
            val firstCloudVisit = !visitSnapshot.exists()
            transaction.set(
                userRef,
                mapOf(
                    "username" to profile.username,
                    "email" to profile.email,
                    "totalScore" to FieldValue.increment(pointsAwarded.toLong()),
                    "visitedCount" to if (firstCloudVisit) FieldValue.increment(1) else FieldValue.increment(0),
                    "updatedAt" to FieldValue.serverTimestamp(),
                    "lastSeenAt" to FieldValue.serverTimestamp(),
                ),
                SetOptions.merge(),
            )
            transaction.set(
                visitRef,
                mapOf(
                    "placeId" to place.id,
                    "title" to place.title,
                    "city" to place.city,
                    "pointsEarned" to FieldValue.increment(pointsAwarded.toLong()),
                    "lastPointsAwarded" to pointsAwarded,
                    "distanceMeters" to distanceMeters.toDouble(),
                    "dailyBonusAwarded" to dailyBonusAwarded,
                    "lastVisitedAt" to FieldValue.serverTimestamp(),
                    "firstVisitedAt" to (visitSnapshot.getTimestamp("firstVisitedAt") ?: Timestamp.now()),
                ),
                SetOptions.merge(),
            )
            null
        }

        val team = GameRepository.loadTeam(context)
        if (team.hasTeam) {
            syncTeam(context, team)
            db.collection("teams")
                .document(team.inviteCode)
                .collection("members")
                .document(uid)
                .set(
                    mapOf(
                        "username" to profile.username,
                        "score" to FieldValue.increment(pointsAwarded.toLong()),
                        "updatedAt" to FieldValue.serverTimestamp(),
                    ),
                    SetOptions.merge(),
                )
            db.collection("teams")
                .document(team.inviteCode)
                .set(
                    mapOf(
                        "teamScore" to FieldValue.increment(pointsAwarded.toLong()),
                        "updatedAt" to FieldValue.serverTimestamp(),
                    ),
                    SetOptions.merge(),
                )
        }
    }

    fun syncTeam(context: Context, team: TeamInfo) {
        val uid = currentUserId(context) ?: return
        if (!team.hasTeam) return
        val profile = GameRepository.loadProfile(context)
        val db = FirebaseFirestore.getInstance()
        db.collection("teams")
            .document(team.inviteCode)
            .set(
                mapOf(
                    "teamName" to team.teamName,
                    "description" to team.description,
                    "inviteCode" to team.inviteCode,
                    "ownerEmail" to team.ownerEmail,
                    "maxMembers" to team.maxMembers,
                    "open" to team.isOpen,
                    "teamScore" to team.teamScore,
                    "memberNames" to team.memberNames,
                    "updatedAt" to FieldValue.serverTimestamp(),
                ),
                SetOptions.merge(),
            )
        db.collection("teams")
            .document(team.inviteCode)
            .collection("members")
            .document(uid)
            .set(
                mapOf(
                    "username" to profile.username,
                    "email" to profile.email,
                    "score" to (team.memberScores[profile.username] ?: 0),
                    "updatedAt" to FieldValue.serverTimestamp(),
                ),
                SetOptions.merge(),
            )
    }

    fun joinTeamByCode(
        context: Context,
        inviteCode: String,
        onResult: (JoinTeamResult) -> Unit,
    ) {
        val uid = currentUserId(context)
        if (uid == null) {
            onResult(JoinTeamResult.NotFound)
            return
        }

        val profile = GameRepository.loadProfile(context)
        val normalizedCode = inviteCode.trim().uppercase()
        val teamRef = FirebaseFirestore.getInstance().collection("teams").document(normalizedCode)

        teamRef.get()
            .addOnSuccessListener { doc ->
                if (!doc.exists()) {
                    onResult(JoinTeamResult.NotFound)
                    return@addOnSuccessListener
                }

                val team = TeamInfo(
                    teamName = doc.getString("teamName") ?: normalizedCode,
                    description = doc.getString("description") ?: "",
                    inviteCode = doc.getString("inviteCode") ?: normalizedCode,
                    ownerEmail = doc.getString("ownerEmail") ?: "",
                    maxMembers = (doc.getLong("maxMembers") ?: 4L).toInt(),
                    isOpen = doc.getBoolean("open") ?: true,
                    memberNames = stringList(doc.get("memberNames")),
                    pendingRequests = stringList(doc.get("pendingRequests")),
                    teamScore = (doc.getLong("teamScore") ?: 0L).toInt(),
                )

                if (profile.username in team.memberNames) {
                    GameRepository.saveTeamFromCloud(context, team)
                    onResult(JoinTeamResult.Joined(team))
                    return@addOnSuccessListener
                }

                if (team.memberNames.size >= team.maxMembers) {
                    onResult(JoinTeamResult.TeamFull)
                    return@addOnSuccessListener
                }

                if (!team.isOpen) {
                    teamRef.set(
                        mapOf(
                            "pendingRequests" to FieldValue.arrayUnion(profile.username),
                            "updatedAt" to FieldValue.serverTimestamp(),
                        ),
                        SetOptions.merge(),
                    )
                    onResult(JoinTeamResult.PendingApproval)
                    return@addOnSuccessListener
                }

                val joinedTeam = team.copy(memberNames = (team.memberNames + profile.username).distinct())
                teamRef.set(
                    mapOf(
                        "memberNames" to FieldValue.arrayUnion(profile.username),
                        "updatedAt" to FieldValue.serverTimestamp(),
                    ),
                    SetOptions.merge(),
                )
                teamRef.collection("members")
                    .document(uid)
                    .set(
                        mapOf(
                            "username" to profile.username,
                            "email" to profile.email,
                            "score" to 0,
                            "updatedAt" to FieldValue.serverTimestamp(),
                        ),
                        SetOptions.merge(),
                    )
                GameRepository.saveTeamFromCloud(context, joinedTeam)
                onResult(JoinTeamResult.Joined(joinedTeam))
            }
            .addOnFailureListener { onResult(JoinTeamResult.NotFound) }
    }

    fun observeGlobalLeaderboard(
        context: Context,
        onResult: (List<LeaderboardEntry>) -> Unit,
    ): ListenerRegistration? {
        if (!isAvailable(context)) return null

        return FirebaseFirestore.getInstance()
            .collection("users")
            .orderBy("totalScore", Query.Direction.DESCENDING)
            .limit(50)
            .addSnapshotListener { snapshot, _ ->
                val entries = snapshot?.documents.orEmpty().mapIndexed { index, doc ->
                    LeaderboardEntry(
                        username = doc.getString("username") ?: "Explorer",
                        totalScore = (doc.getLong("totalScore") ?: 0L).toInt(),
                        visitedCount = (doc.getLong("visitedCount") ?: 0L).toInt(),
                        rank = index + 1,
                    )
                }
                onResult(entries)
            }
    }

    private fun touchCurrentUser(context: Context) {
        val uid = currentUserId(context) ?: return
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(uid)
            .set(mapOf("lastSeenAt" to FieldValue.serverTimestamp()), SetOptions.merge())
    }

    private fun stringList(value: Any?): List<String> =
        (value as? List<*>)?.mapNotNull { it as? String }.orEmpty()
}
