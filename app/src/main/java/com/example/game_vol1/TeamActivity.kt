package com.example.game_vol1

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.game_vol1.data.GameRepository
import com.example.game_vol1.data.JoinTeamResult
import com.example.game_vol1.models.TeamInfo
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.switchmaterial.SwitchMaterial

class TeamActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.geo_activity_team)

        val createInput = findViewById<EditText>(R.id.etTeamName)
        val descriptionInput = findViewById<EditText>(R.id.etTeamDescription)
        val maxMembersInput = findViewById<EditText>(R.id.etMaxMembers)
        val openSwitch = findViewById<SwitchMaterial>(R.id.switchOpenTeam)
        val joinInput = findViewById<EditText>(R.id.etInviteCode)
        val teamNameView = findViewById<TextView>(R.id.tvTeamName)
        val teamMetaView = findViewById<TextView>(R.id.tvTeamMeta)
        val teamMembersView = findViewById<TextView>(R.id.tvTeamMembers)
        val inviteView = findViewById<TextView>(R.id.tvInviteCodeValue)
        val ownerView = findViewById<TextView>(R.id.tvOwnerHint)
        val pendingView = findViewById<TextView>(R.id.tvPendingRequests)
        val leaderboardView = findViewById<TextView>(R.id.tvLeaderboard)
        val availableTeamsContainer = findViewById<LinearLayout>(R.id.availableTeamsContainer)
        val createButton = findViewById<Button>(R.id.btnCreateTeam)
        val joinButton = findViewById<Button>(R.id.btnJoinTeam)
        val approveButton = findViewById<Button>(R.id.btnApproveNext)
        val leaveButton = findViewById<Button>(R.id.btnLeaveTeam)
        lateinit var refreshTeam: () -> Unit

        fun makeTeamCard(team: TeamInfo): View {
            val card = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(24, 24, 24, 24)
                background = getDrawable(R.drawable.bg_dark_card)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                ).apply {
                    bottomMargin = 20
                }
            }

            val title = TextView(this).apply {
                text = team.teamName
                textSize = 18f
                setTextColor(0xFFFFFFFF.toInt())
            }
            val meta = TextView(this).apply {
                val accessLabel = UiLanguageStore.pick(
                    this@TeamActivity,
                    if (team.isOpen) "???????" else "????????",
                    if (team.isOpen) "Open" else "Locked",
                )
                val membersLabel = UiLanguageStore.pick(this@TeamActivity, "?????", "members")
                val scoreLabel = UiLanguageStore.pick(this@TeamActivity, "?.", "pts")
                text = "$accessLabel | ${team.memberNames.size}/${team.maxMembers} $membersLabel | ${team.teamScore} $scoreLabel"
                textSize = 14f
                setTextColor(0xFFB8C7E0.toInt())
            }
            val description = TextView(this).apply {
                text = team.description.ifBlank { UiLanguageStore.pick(this@TeamActivity, "???? ????????.", "No description provided.") }
                textSize = 14f
                setTextColor(0xFFD7DFEC.toInt())
            }
            val actionButton = Button(this).apply {
                text = UiLanguageStore.pick(
                    this@TeamActivity,
                    if (team.isOpen) "?????????? ??" else "??????? ??????",
                    if (team.isOpen) "Join Team" else "Request Access",
                )
                isEnabled = team.memberNames.size < team.maxMembers
                setOnClickListener {
                    val result = if (team.isOpen) {
                        GameRepository.joinOpenTeam(this@TeamActivity, team.inviteCode)
                    } else {
                        GameRepository.requestJoinLockedTeam(this@TeamActivity, team.inviteCode)
                    }
                    when (result) {
                        is JoinTeamResult.Joined -> Toast.makeText(this@TeamActivity, UiLanguageStore.pick(this@TeamActivity, "?????????? ?? ??? ${team.teamName}.", "Joined ${team.teamName}."), Toast.LENGTH_SHORT).show()
                        JoinTeamResult.PendingApproval -> Toast.makeText(this@TeamActivity, UiLanguageStore.pick(this@TeamActivity, "???????? ? ????????? ?? ${team.teamName}.", "Request sent to ${team.teamName}."), Toast.LENGTH_SHORT).show()
                        JoinTeamResult.TeamFull -> Toast.makeText(this@TeamActivity, UiLanguageStore.pick(this@TeamActivity, "${team.teamName} ? ?????.", "${team.teamName} is full."), Toast.LENGTH_SHORT).show()
                        JoinTeamResult.NotFound -> Toast.makeText(this@TeamActivity, UiLanguageStore.pick(this@TeamActivity, "??????? ?? ? ???????.", "Team is not available."), Toast.LENGTH_SHORT).show()
                        JoinTeamResult.AlreadyInTeam -> Toast.makeText(this@TeamActivity, UiLanguageStore.pick(this@TeamActivity, "???? ???????? ? ?????.", "You are already in a team."), Toast.LENGTH_SHORT).show()
                    }
                    refreshTeam()
                }
            }

            card.addView(title)
            card.addView(meta)
            card.addView(description)
            card.addView(actionButton)
            return card
        }

        fun renderAvailableTeams() {
            availableTeamsContainer.removeAllViews()
            val teams = GameRepository.getBrowseableTeams(this)
            if (teams.isEmpty()) {
                val empty = TextView(this).apply {
                    text = UiLanguageStore.pick(this@TeamActivity, "???? ????? ??????? ?????? ? ???????.", "No other teams available right now.")
                    setTextColor(0xFFD7DFEC.toInt())
                }
                availableTeamsContainer.addView(empty)
                return
            }
            teams.forEach { availableTeamsContainer.addView(makeTeamCard(it)) }
        }

        refreshTeam = {
            val team = GameRepository.loadTeam(this)
            val hasTeam = team.hasTeam
            val isOwner = GameRepository.isTeamOwner(this)
            val isBg = UiLanguageStore.isBulgarian(this)
            val scoreLabel = UiLanguageStore.pick(this, "?.", "pts")

            findViewById<TextView>(R.id.tvTeamsSection).text = UiLanguageStore.pick(this, "??????", "Teams")
            findViewById<TextView>(R.id.tvCreateTeamLabel).text = UiLanguageStore.pick(this, "?????? ?????", "Create Team")
            findViewById<TextView>(R.id.tvJoinTeamLabel).text = UiLanguageStore.pick(this, "?????????? ?? ??? ?????", "Join Team")
            findViewById<TextView>(R.id.tvInviteCodeLabel).text = UiLanguageStore.pick(this, "??? ?? ??????", "Invite Code")
            findViewById<TextView>(R.id.tvMembersLabel).text = UiLanguageStore.pick(this, "???????", "Members")
            findViewById<TextView>(R.id.tvLeaderboardLabel).text = UiLanguageStore.pick(this, "???????? ?? ????????", "Team Leaderboard")
            findViewById<TextView>(R.id.tvAvailableTeamsLabel).text = UiLanguageStore.pick(this, "??????? ??????", "Available Teams")
            createInput.hint = UiLanguageStore.pick(this, "??? ?? ??????", "Team name")
            descriptionInput.hint = UiLanguageStore.pick(this, "???????? ?? ??????", "Team description")
            maxMembersInput.hint = UiLanguageStore.pick(this, "?????????? ???? ???????", "Max members")
            openSwitch.text = UiLanguageStore.pick(this, "??????? ????? (????? ???????????? ???????? ????????)", "Open team (otherwise owner approval is required)")
            joinInput.hint = UiLanguageStore.pick(this, "??? ?? ??????", "Invite code")
            createButton.text = UiLanguageStore.pick(this, "?????? ?????", "Create Team")
            joinButton.text = UiLanguageStore.pick(this, "?????????? ??", "Join Team")
            approveButton.text = UiLanguageStore.pick(this, "?????? ?????????? ??????", "Approve Next Request")
            leaveButton.text = UiLanguageStore.pick(this, "??????? ??????", "Leave Team")

            createInput.isEnabled = !hasTeam
            descriptionInput.isEnabled = !hasTeam
            maxMembersInput.isEnabled = !hasTeam
            openSwitch.isEnabled = !hasTeam
            joinInput.isEnabled = !hasTeam
            createButton.isEnabled = !hasTeam
            joinButton.isEnabled = !hasTeam

            if (!hasTeam) {
                teamNameView.text = UiLanguageStore.pick(this, "??? ??? ????? ?????", "No team yet")
                teamMetaView.text = UiLanguageStore.pick(this, "?????? ????? ? ????? ?? ??????? ? ???????? ??? ?? ?????????? ? ??? ?? ??????.", "Create a team with a member cap and description, or join one with an invite code.")
                teamMembersView.text = UiLanguageStore.pick(this, "????????? ?? ?? ?????? ???.", "Members will appear here.")
                inviteView.text = "-"
                ownerView.text = UiLanguageStore.pick(this, "???? ???????????? ?? ?????? ???? ?? ???? ????????.", "Only team owners can invite friends.")
                pendingView.text = UiLanguageStore.pick(this, "???? ?????? ??????.", "No pending requests.")
                approveButton.visibility = View.GONE
                leaveButton.visibility = View.GONE
                leaderboardView.text = GameRepository.getTeamLeaderboard(this)
                    .mapIndexed { index, entry -> "${index + 1}. ${entry.teamName} - ${entry.teamScore} $scoreLabel" }
                    .joinToString("\n")
                    .ifBlank { UiLanguageStore.pick(this, "??? ??? ???? ??????? ??????.", "No teams available yet.") }
                renderAvailableTeams()
            } else {
                teamNameView.text = team.teamName
                val membersLabel = if (isBg) "?????" else "members"
                teamMetaView.text = buildString {
                    append(UiLanguageStore.pick(this@TeamActivity, if (team.isOpen) "??????? ?????" else "???????? ?????", if (team.isOpen) "Open team" else "Locked team"))
                    append(" | ")
                    append("${team.memberNames.size}/${team.maxMembers} $membersLabel")
                    append(" | ")
                    append(UiLanguageStore.pick(this@TeamActivity, "??????? ?????", "Shared score"))
                    append(" ")
                    append(team.teamScore)
                    append("\n")
                    append(team.description.ifBlank { UiLanguageStore.pick(this@TeamActivity, "??? ??? ???? ????????.", "No description provided yet.") })
                }
                teamMembersView.text = team.memberNames.joinToString("\n")
                inviteView.text = team.inviteCode
                ownerView.text = if (isOwner) {
                    UiLanguageStore.pick(this, "?? ?? ????????????. ??????? ???? ? ????????, ?????????? ???????? ? ???????? ??????.", "You are the owner. Share this code with friends, review locked-team requests, and grow the squad.")
                } else {
                    UiLanguageStore.pick(this, "?? ?? ???? ?? ???? ?????.", "You're a member of this team.")
                }
                pendingView.text = if (team.pendingRequests.isEmpty()) {
                    UiLanguageStore.pick(this, "???? ?????? ??????.", "No pending requests.")
                } else {
                    UiLanguageStore.pick(this, "??????: ${team.pendingRequests.joinToString(", ")}", "Pending: ${team.pendingRequests.joinToString(", ")}")
                }
                approveButton.visibility = if (isOwner) View.VISIBLE else View.GONE
                leaveButton.visibility = if (isOwner) View.GONE else View.VISIBLE

                val memberLeaderboard = GameRepository.getLeaderboard(this)
                    .mapIndexed { index, pair -> "${index + 1}. ${pair.first} - ${pair.second} $scoreLabel" }
                    .joinToString("\n")
                    .ifBlank { UiLanguageStore.pick(this, "??? ??? ???? ????? ?? ???????.", "No member points yet.") }
                val teamLeaderboard = GameRepository.getTeamLeaderboard(this)
                    .mapIndexed { index, entry -> "${index + 1}. ${entry.teamName} - ${entry.teamScore} $scoreLabel" }
                    .joinToString("\n")
                    .ifBlank { UiLanguageStore.pick(this, "???? ??????? ??????.", "No teams available.") }
                leaderboardView.text = "${UiLanguageStore.pick(this, "???????", "Members")}\n$memberLeaderboard\n\n${UiLanguageStore.pick(this, "??????", "Teams")}\n$teamLeaderboard"

                renderAvailableTeams()
            }

            AppNavigation.bind(this, findViewById<BottomNavigationView>(R.id.bottomNav), R.id.nav_profile)
        }

        createButton.setOnClickListener {
            val name = createInput.text.toString().trim()
            val description = descriptionInput.text.toString().trim()
            val maxMembers = maxMembersInput.text.toString().toIntOrNull()?.coerceIn(2, 20) ?: 4
            if (name.isBlank()) {
                Toast.makeText(this, UiLanguageStore.pick(this, "????? ?????? ??? ?? ?????.", "Enter a team name first."), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val created = GameRepository.createTeam(this, name, description, maxMembers, openSwitch.isChecked)
            if (created == null) {
                Toast.makeText(this, UiLanguageStore.pick(this, "???? ?????? ???? ? ? ?????.", "This account is already in a team."), Toast.LENGTH_SHORT).show()
            } else {
                createInput.text?.clear()
                descriptionInput.text?.clear()
                maxMembersInput.setText("4")
                openSwitch.isChecked = true
                Toast.makeText(this, UiLanguageStore.pick(this, "${created.teamName} ? ????????.", "${created.teamName} is ready."), Toast.LENGTH_SHORT).show()
                refreshTeam()
            }
        }

        joinButton.setOnClickListener {
            val code = joinInput.text.toString().trim()
            if (code.isBlank()) {
                Toast.makeText(this, UiLanguageStore.pick(this, "?????? ??? ?? ??????.", "Enter an invite code."), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            when (val result = GameRepository.joinTeamByCode(this, code)) {
                is JoinTeamResult.Joined -> {
                    Toast.makeText(this, UiLanguageStore.pick(this, "?????????? ?? ??? ${result.team.teamName}.", "Joined ${result.team.teamName}."), Toast.LENGTH_SHORT).show()
                    joinInput.text?.clear()
                    refreshTeam()
                }
                JoinTeamResult.PendingApproval -> {
                    Toast.makeText(this, UiLanguageStore.pick(this, "???????? ? ?????????. ???????????? ?????? ?? ?? ??????.", "Request sent. The team owner needs to approve you."), Toast.LENGTH_LONG).show()
                    joinInput.text?.clear()
                    refreshTeam()
                }
                JoinTeamResult.TeamFull -> {
                    Toast.makeText(this, UiLanguageStore.pick(this, "???? ????? ? ?????.", "That team is full."), Toast.LENGTH_SHORT).show()
                }
                JoinTeamResult.NotFound -> {
                    Toast.makeText(this, UiLanguageStore.pick(this, "???? ??? ?? ?????? ?? ? ??????? ?? ????????????.", "Invite code not found on this device yet."), Toast.LENGTH_LONG).show()
                }
                JoinTeamResult.AlreadyInTeam -> {
                    Toast.makeText(this, UiLanguageStore.pick(this, "???? ?????? ???? ? ? ?????.", "This account is already in a team."), Toast.LENGTH_SHORT).show()
                }
            }
        }

        approveButton.setOnClickListener {
            val team = GameRepository.loadTeam(this)
            val nextRequest = team.pendingRequests.firstOrNull()
            if (nextRequest == null) {
                Toast.makeText(this, UiLanguageStore.pick(this, "???? ?????? ??????.", "No pending requests."), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val approved = GameRepository.approveRequest(this, nextRequest)
            if (approved == null) {
                Toast.makeText(this, UiLanguageStore.pick(this, "???????? ?? ???? ?? ???? ????????.", "Could not approve the request."), Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, UiLanguageStore.pick(this, "$nextRequest ???? ? ? ??????.", "$nextRequest joined the team."), Toast.LENGTH_SHORT).show()
                refreshTeam()
            }
        }

        leaveButton.setOnClickListener {
            val leftTeam = GameRepository.leaveCurrentTeam(this)
            if (leftTeam == null) {
                Toast.makeText(this, UiLanguageStore.pick(this, "???????????? ?? ???? ?? ??????? ????? ?? ??? ??????????? ?? ?????????????.", "Team owners cannot leave until ownership transfer exists."), Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, UiLanguageStore.pick(this, "??????? ${leftTeam.teamName}.", "You left ${leftTeam.teamName}."), Toast.LENGTH_SHORT).show()
                refreshTeam()
            }
        }

        maxMembersInput.setText("4")
        openSwitch.isChecked = true
        refreshTeam()
    }

    override fun onResume() {
        super.onResume()
        findViewById<EditText>(R.id.etMaxMembers).setText(findViewById<EditText>(R.id.etMaxMembers).text)
    }
}
