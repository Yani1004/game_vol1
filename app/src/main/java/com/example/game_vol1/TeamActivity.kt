package com.example.game_vol1

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.game_vol1.data.GameRepository
import com.example.game_vol1.data.JoinTeamResult
import com.example.game_vol1.data.MultiplayerRepository
import com.example.game_vol1.models.TeamInfo

class TeamActivity : AppCompatActivity() {
    private var currentTeam by mutableStateOf(TeamInfo())
    private var browseableTeams by mutableStateOf<List<TeamInfo>>(emptyList())
    private var teamLeaderboard by mutableStateOf<List<TeamInfo>>(emptyList())
    private var memberLeaderboard by mutableStateOf<List<Pair<String, Int>>>(emptyList())
    private var isOwner by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        refresh()
        setContent {
            TeamScreen(
                activity = this,
                team = currentTeam,
                isOwner = isOwner,
                browseableTeams = browseableTeams,
                teamLeaderboard = teamLeaderboard,
                memberLeaderboard = memberLeaderboard,
                onCreate = ::createTeam,
                onJoinCode = ::joinByCode,
                onJoinListed = ::joinListedTeam,
                onApprove = ::approveNextRequest,
                onLeave = ::leaveTeam,
            )
        }
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    private fun refresh() {
        currentTeam = GameRepository.loadTeam(this)
        browseableTeams = GameRepository.getBrowseableTeams(this)
        teamLeaderboard = GameRepository.getTeamLeaderboard(this)
        memberLeaderboard = GameRepository.getLeaderboard(this)
        isOwner = GameRepository.isTeamOwner(this)
    }

    private fun createTeam(name: String, description: String, maxMembers: String, open: Boolean) {
        val max = maxMembers.toIntOrNull()?.coerceIn(2, 20) ?: 4
        if (name.isBlank()) {
            Toast.makeText(this, "Enter a team name first.", Toast.LENGTH_SHORT).show()
            return
        }
        val created = GameRepository.createTeam(this, name, description, max, open)
        Toast.makeText(this, created?.let { "${it.teamName} is ready." } ?: "This account is already in a team.", Toast.LENGTH_SHORT).show()
        refresh()
    }

    private fun joinByCode(code: String) {
        if (code.isBlank()) {
            Toast.makeText(this, "Enter an invite code.", Toast.LENGTH_SHORT).show()
            return
        }
        when (val result = GameRepository.joinTeamByCode(this, code)) {
            JoinTeamResult.NotFound -> {
                if (MultiplayerRepository.isAvailable(this)) {
                    MultiplayerRepository.joinTeamByCode(this, code) { cloudResult ->
                        runOnUiThread {
                            showJoinResult(cloudResult, code)
                            refresh()
                        }
                    }
                } else {
                    showJoinResult(result, code)
                }
            }
            else -> showJoinResult(result, code)
        }
        refresh()
    }

    private fun joinListedTeam(team: TeamInfo) {
        val result = if (team.isOpen) GameRepository.joinOpenTeam(this, team.inviteCode) else GameRepository.requestJoinLockedTeam(this, team.inviteCode)
        showJoinResult(result, team.teamName)
        refresh()
    }

    private fun approveNextRequest() {
        val next = GameRepository.loadTeam(this).pendingRequests.firstOrNull()
        if (next == null) {
            Toast.makeText(this, "No pending requests.", Toast.LENGTH_SHORT).show()
            return
        }
        Toast.makeText(this, if (GameRepository.approveRequest(this, next) == null) "Could not approve the request." else "$next joined the team.", Toast.LENGTH_SHORT).show()
        refresh()
    }

    private fun leaveTeam() {
        val left = GameRepository.leaveCurrentTeam(this)
        Toast.makeText(this, left?.let { "You left ${it.teamName}." } ?: "Team owners cannot leave until ownership transfer exists.", Toast.LENGTH_LONG).show()
        refresh()
    }

    private fun showJoinResult(result: JoinTeamResult, teamName: String) {
        val message = when (result) {
            is JoinTeamResult.Joined -> "Joined ${result.team.teamName}."
            JoinTeamResult.PendingApproval -> "Request sent${if (teamName.isNotBlank()) " to $teamName" else ""}."
            JoinTeamResult.TeamFull -> "That team is full."
            JoinTeamResult.NotFound -> "Invite code not found on this device yet."
            JoinTeamResult.AlreadyInTeam -> "You are already in a team."
        }
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}

@Composable
private fun TeamScreen(
    activity: TeamActivity,
    team: TeamInfo,
    isOwner: Boolean,
    browseableTeams: List<TeamInfo>,
    teamLeaderboard: List<TeamInfo>,
    memberLeaderboard: List<Pair<String, Int>>,
    onCreate: (String, String, String, Boolean) -> Unit,
    onJoinCode: (String) -> Unit,
    onJoinListed: (TeamInfo) -> Unit,
    onApprove: () -> Unit,
    onLeave: () -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var maxMembers by remember { mutableStateOf("4") }
    var open by remember { mutableStateOf(true) }
    var inviteCode by remember { mutableStateOf("") }

    HuntTheme {
        HuntScaffold(activity = activity, selected = "profile") { modifier ->
            LazyColumn(
                modifier = modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item { HuntTitle("Teams", "Create, join, and climb the shared leaderboard.") }
                item {
                    HuntPanel(accent = if (team.hasTeam) HuntColors.Green else HuntColors.Blue) {
                        Text(if (team.hasTeam) team.teamName else "No team yet", color = HuntColors.Text, fontWeight = FontWeight.Black)
                        Text(
                            if (team.hasTeam) "${if (team.isOpen) "Open" else "Locked"} | ${team.memberNames.size}/${team.maxMembers} members | ${team.teamScore} pts" else "Create a team or join one with an invite code.",
                            color = HuntColors.Muted,
                        )
                        if (team.hasTeam) {
                            Text("Invite code: ${team.inviteCode}", color = HuntColors.Gold, fontWeight = FontWeight.Bold)
                            Text(team.description.ifBlank { "No description provided yet." }, color = HuntColors.Muted)
                            Text("Members: ${team.memberNames.joinToString(", ")}", color = HuntColors.BlueSoft)
                            Text(if (team.pendingRequests.isEmpty()) "No pending requests." else "Pending: ${team.pendingRequests.joinToString(", ")}", color = HuntColors.Muted)
                            if (isOwner) HuntButton("Approve Next Request", onApprove, color = HuntColors.Blue)
                            if (!isOwner) HuntButton("Leave Team", onLeave, color = HuntColors.Rose)
                        }
                    }
                }
                if (!team.hasTeam) {
                    item {
                        HuntPanel(accent = HuntColors.Green) {
                            Text("Create Team", color = HuntColors.Text, fontWeight = FontWeight.Black)
                            HuntField(name, { name = it }, "Team name")
                            HuntField(description, { description = it }, "Team description")
                            HuntField(maxMembers, { maxMembers = it }, "Max members", keyboardType = KeyboardType.Number)
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Switch(checked = open, onCheckedChange = { open = it })
                                Text("Open team", color = HuntColors.Muted)
                            }
                            HuntButton("Create Team", {
                                onCreate(name.trim(), description.trim(), maxMembers, open)
                                name = ""
                                description = ""
                                maxMembers = "4"
                                open = true
                            })
                        }
                    }
                    item {
                        HuntPanel(accent = HuntColors.Blue) {
                            Text("Join Team", color = HuntColors.Text, fontWeight = FontWeight.Black)
                            HuntField(inviteCode, { inviteCode = it }, "Invite code")
                            HuntButton("Join Team", {
                                onJoinCode(inviteCode.trim())
                                inviteCode = ""
                            }, color = HuntColors.Blue)
                        }
                    }
                }
                item {
                    HuntPanel(accent = HuntColors.Gold) {
                        Text("Team Leaderboard", color = HuntColors.Text, fontWeight = FontWeight.Black)
                        if (memberLeaderboard.isNotEmpty()) {
                            Text("Members", color = HuntColors.BlueSoft, fontWeight = FontWeight.Bold)
                            memberLeaderboard.forEachIndexed { index, entry -> Text("${index + 1}. ${entry.first} - ${entry.second} pts", color = HuntColors.Muted) }
                        }
                        Text("Teams", color = HuntColors.BlueSoft, fontWeight = FontWeight.Bold)
                        if (teamLeaderboard.isEmpty()) Text("No teams available.", color = HuntColors.Muted)
                        teamLeaderboard.forEachIndexed { index, entry -> Text("${index + 1}. ${entry.teamName} - ${entry.teamScore} pts", color = HuntColors.Muted) }
                    }
                }
                item {
                    Text("Available Teams", color = HuntColors.Text, fontWeight = FontWeight.Black)
                }
                if (browseableTeams.isEmpty()) {
                    item {
                        HuntPanel { Text("No other teams available right now.", color = HuntColors.Muted) }
                    }
                } else {
                    items(browseableTeams, key = { it.inviteCode }) { listed ->
                        HuntPanel(accent = if (listed.isOpen) HuntColors.Green else HuntColors.Rose) {
                            Text(listed.teamName, color = HuntColors.Text, fontWeight = FontWeight.Black)
                            Text("${if (listed.isOpen) "Open" else "Locked"} | ${listed.memberNames.size}/${listed.maxMembers} members | ${listed.teamScore} pts", color = HuntColors.Muted)
                            Text(listed.description.ifBlank { "No description provided." }, color = HuntColors.Muted)
                            HuntButton(if (listed.isOpen) "Join Team" else "Request Access", { onJoinListed(listed) }, color = HuntColors.Blue)
                        }
                    }
                }
            }
        }
    }
}
