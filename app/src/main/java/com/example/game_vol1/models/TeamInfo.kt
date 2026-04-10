package com.example.game_vol1.models

data class TeamInfo(
    val teamName: String = "",
    val description: String = "",
    val inviteCode: String = "",
    val ownerEmail: String = "",
    val maxMembers: Int = 4,
    val isOpen: Boolean = true,
    val memberNames: List<String> = emptyList(),
    val pendingRequests: List<String> = emptyList(),
    val memberScores: Map<String, Int> = emptyMap(),
    val teamScore: Int = 0,
) {
    val hasTeam: Boolean
        get() = teamName.isNotBlank() && inviteCode.isNotBlank()

    val memberCountLabel: String
        get() = "${memberNames.size}/$maxMembers members"
}
