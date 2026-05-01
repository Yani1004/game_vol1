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
import com.example.game_vol1.data.MultiplayerRepository
import com.example.game_vol1.models.TeamInfo
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.switchmaterial.SwitchMaterial

class TeamActivity : AppCompatActivity() {
    private lateinit var createInput: EditText
    private lateinit var descriptionInput: EditText
    private lateinit var maxMembersInput: EditText
    private lateinit var openSwitch: SwitchMaterial
    private lateinit var joinInput: EditText
    private lateinit var teamNameView: TextView
    private lateinit var teamMetaView: TextView
    private lateinit var teamMembersView: TextView
    private lateinit var inviteView: TextView
    private lateinit var ownerView: TextView
    private lateinit var pendingView: TextView
    private lateinit var leaderboardView: TextView
    private lateinit var availableTeamsContainer: LinearLayout
    private lateinit var createButton: Button
    private lateinit var joinButton: Button
    private lateinit var approveButton: Button
    private lateinit var leaveButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.geo_activity_team)

        createInput = findViewById(R.id.etTeamName)
        descriptionInput = findViewById(R.id.etTeamDescription)
        maxMembersInput = findViewById(R.id.etMaxMembers)
        openSwitch = findViewById(R.id.switchOpenTeam)
        joinInput = findViewById(R.id.etInviteCode)
        teamNameView = findViewById(R.id.tvTeamName)
        teamMetaView = findViewById(R.id.tvTeamMeta)
        teamMembersView = findViewById(R.id.tvTeamMembers)
        inviteView = findViewById(R.id.tvInviteCodeValue)
        ownerView = findViewById(R.id.tvOwnerHint)
        pendingView = findViewById(R.id.tvPendingRequests)
        leaderboardView = findViewById(R.id.tvLeaderboard)
        availableTeamsContainer = findViewById(R.id.availableTeamsContainer)
        createButton = findViewById(R.id.btnCreateTeam)
        joinButton = findViewById(R.id.btnJoinTeam)
        approveButton = findViewById(R.id.btnApproveNext)
        leaveButton = findViewById(R.id.btnLeaveTeam)

        maxMembersInput.setText("4")
        openSwitch.isChecked = true

        createButton.setOnClickListener { createTeam() }
        joinButton.setOnClickListener { joinByCode() }
        approveButton.setOnClickListener { approveNextRequest() }
        leaveButton.setOnClickListener { leaveTeam() }
        listOf(createButton, joinButton, approveButton, leaveButton).forEach { it.applyPressFeedback() }
        findViewById<android.view.View>(android.R.id.content).fadeSlideIn()

        renderScreen()
    }

    override fun onResume() {
        super.onResume()
        if (::teamNameView.isInitialized) renderScreen()
    }

    private fun renderScreen() {
        val team = GameRepository.loadTeam(this)
        val hasTeam = team.hasTeam
        val isOwner = GameRepository.isTeamOwner(this)
        val isBg = UiLanguageStore.isBulgarian(this)
        val scoreLabel = UiLanguageStore.pick(this, "т.", "pts")

        findViewById<TextView>(R.id.tvTeamsSection).text = UiLanguageStore.pick(this, "Отбори", "Teams")
        findViewById<TextView>(R.id.tvCreateTeamLabel).text = UiLanguageStore.pick(this, "Създай отбор", "Create Team")
        findViewById<TextView>(R.id.tvJoinTeamLabel).text = UiLanguageStore.pick(this, "Присъедини се към отбор", "Join Team")
        findViewById<TextView>(R.id.tvInviteCodeLabel).text = UiLanguageStore.pick(this, "Код за покана", "Invite Code")
        findViewById<TextView>(R.id.tvMembersLabel).text = UiLanguageStore.pick(this, "Участници", "Members")
        findViewById<TextView>(R.id.tvLeaderboardLabel).text = UiLanguageStore.pick(this, "Класация", "Team Leaderboard")
        findViewById<TextView>(R.id.tvAvailableTeamsLabel).text = UiLanguageStore.pick(this, "Налични отбори", "Available Teams")
        createInput.hint = UiLanguageStore.pick(this, "Име на отбора", "Team name")
        descriptionInput.hint = UiLanguageStore.pick(this, "Описание на отбора", "Team description")
        maxMembersInput.hint = UiLanguageStore.pick(this, "Максимален брой участници", "Max members")
        openSwitch.text = UiLanguageStore.pick(this, "Отворен отбор (иначе се изисква одобрение)", "Open team (otherwise owner approval is required)")
        joinInput.hint = UiLanguageStore.pick(this, "Код за покана", "Invite code")
        createButton.text = UiLanguageStore.pick(this, "Създай отбор", "Create Team")
        joinButton.text = UiLanguageStore.pick(this, "Присъедини се", "Join Team")
        approveButton.text = UiLanguageStore.pick(this, "Одобри следваща заявка", "Approve Next Request")
        leaveButton.text = UiLanguageStore.pick(this, "Напусни отбора", "Leave Team")

        createInput.isEnabled = !hasTeam
        descriptionInput.isEnabled = !hasTeam
        maxMembersInput.isEnabled = !hasTeam
        openSwitch.isEnabled = !hasTeam
        joinInput.isEnabled = !hasTeam
        createButton.isEnabled = !hasTeam
        joinButton.isEnabled = !hasTeam

        if (!hasTeam) {
            teamNameView.text = UiLanguageStore.pick(this, "Все още нямаш отбор", "No team yet")
            teamMetaView.text = UiLanguageStore.pick(
                this,
                "Създай отбор с лимит и описание или се присъедини чрез код за покана.",
                "Create a team with a member cap and description, or join one with an invite code.",
            )
            teamMembersView.text = UiLanguageStore.pick(this, "Участниците ще се покажат тук.", "Members will appear here.")
            inviteView.text = "-"
            ownerView.text = UiLanguageStore.pick(this, "Собственикът на отбора може да кани приятели.", "Only team owners can invite friends.")
            pendingView.text = UiLanguageStore.pick(this, "Няма чакащи заявки.", "No pending requests.")
            approveButton.visibility = View.GONE
            leaveButton.visibility = View.GONE
        } else {
            teamNameView.text = team.teamName
            val membersLabel = if (isBg) "членове" else "members"
            teamMetaView.text = buildString {
                append(UiLanguageStore.pick(this@TeamActivity, if (team.isOpen) "Отворен отбор" else "Затворен отбор", if (team.isOpen) "Open team" else "Locked team"))
                append(" | ${team.memberNames.size}/${team.maxMembers} $membersLabel | ")
                append(UiLanguageStore.pick(this@TeamActivity, "общ резултат", "shared score"))
                append(" ${team.teamScore}\n")
                append(team.description.ifBlank { UiLanguageStore.pick(this@TeamActivity, "Няма описание.", "No description provided yet.") })
            }
            teamMembersView.text = team.memberNames.joinToString("\n")
            inviteView.text = team.inviteCode
            ownerView.text = if (isOwner) {
                UiLanguageStore.pick(this, "Ти си собственик. Сподели кода и одобрявай заявки за затворен отбор.", "You are the owner. Share this code with friends and review locked-team requests.")
            } else {
                UiLanguageStore.pick(this, "Ти си член на този отбор.", "You're a member of this team.")
            }
            pendingView.text = if (team.pendingRequests.isEmpty()) {
                UiLanguageStore.pick(this, "Няма чакащи заявки.", "No pending requests.")
            } else {
                UiLanguageStore.pick(this, "Чакащи: ${team.pendingRequests.joinToString(", ")}", "Pending: ${team.pendingRequests.joinToString(", ")}")
            }
            approveButton.visibility = if (isOwner) View.VISIBLE else View.GONE
            leaveButton.visibility = if (isOwner) View.GONE else View.VISIBLE
        }

        leaderboardView.text = buildLeaderboard(hasTeam, scoreLabel)
        renderAvailableTeams()
        AppNavigation.bind(this, findViewById<BottomNavigationView>(R.id.bottomNav), R.id.nav_profile)
    }

    private fun buildLeaderboard(hasTeam: Boolean, scoreLabel: String): String {
        val teamLeaderboard = GameRepository.getTeamLeaderboard(this)
            .mapIndexed { index, entry -> "${index + 1}. ${entry.teamName} - ${entry.teamScore} $scoreLabel" }
            .joinToString("\n")
            .ifBlank { UiLanguageStore.pick(this, "Няма налични отбори.", "No teams available.") }

        if (!hasTeam) return teamLeaderboard

        val memberLeaderboard = GameRepository.getLeaderboard(this)
            .mapIndexed { index, pair -> "${index + 1}. ${pair.first} - ${pair.second} $scoreLabel" }
            .joinToString("\n")
            .ifBlank { UiLanguageStore.pick(this, "Все още няма точки по участници.", "No member points yet.") }

        return "${UiLanguageStore.pick(this, "Участници", "Members")}\n$memberLeaderboard\n\n${UiLanguageStore.pick(this, "Отбори", "Teams")}\n$teamLeaderboard"
    }

    private fun renderAvailableTeams() {
        availableTeamsContainer.removeAllViews()
        val teams = GameRepository.getBrowseableTeams(this)
        if (teams.isEmpty()) {
            availableTeamsContainer.addView(TextView(this).apply {
                text = UiLanguageStore.pick(this@TeamActivity, "В момента няма други налични отбори.", "No other teams available right now.")
                setTextColor(0xFFD7DFEC.toInt())
            })
            return
        }
        teams.forEach { availableTeamsContainer.addView(makeTeamCard(it)) }
    }

    private fun makeTeamCard(team: TeamInfo): View {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 24, 24, 24)
            background = getDrawable(R.drawable.bg_dark_card)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            ).apply { bottomMargin = 20 }
        }

        val title = TextView(this).apply {
            text = team.teamName
            textSize = 18f
            setTextColor(0xFFFFFFFF.toInt())
        }
        val meta = TextView(this).apply {
            val accessLabel = UiLanguageStore.pick(this@TeamActivity, if (team.isOpen) "Отворен" else "Затворен", if (team.isOpen) "Open" else "Locked")
            val membersLabel = UiLanguageStore.pick(this@TeamActivity, "членове", "members")
            val scoreLabel = UiLanguageStore.pick(this@TeamActivity, "т.", "pts")
            text = "$accessLabel | ${team.memberNames.size}/${team.maxMembers} $membersLabel | ${team.teamScore} $scoreLabel"
            textSize = 14f
            setTextColor(0xFFB8C7E0.toInt())
        }
        val description = TextView(this).apply {
            text = team.description.ifBlank { UiLanguageStore.pick(this@TeamActivity, "Няма описание.", "No description provided.") }
            textSize = 14f
            setTextColor(0xFFD7DFEC.toInt())
        }
        val actionButton = Button(this).apply {
            text = UiLanguageStore.pick(this@TeamActivity, if (team.isOpen) "Присъедини се" else "Изпрати заявка", if (team.isOpen) "Join Team" else "Request Access")
            isEnabled = team.memberNames.size < team.maxMembers
            background = getDrawable(R.drawable.bg_secondary_button)
            setTextColor(0xFFFFFFFF.toInt())
            setOnClickListener { joinListedTeam(team) }
            applyPressFeedback()
        }

        card.addView(title)
        card.addView(meta)
        card.addView(description)
        card.addView(actionButton)
        return card
    }

    private fun joinListedTeam(team: TeamInfo) {
        val result = if (team.isOpen) {
            GameRepository.joinOpenTeam(this, team.inviteCode)
        } else {
            GameRepository.requestJoinLockedTeam(this, team.inviteCode)
        }
        showJoinResult(result, team.teamName)
        renderScreen()
    }

    private fun createTeam() {
        val name = createInput.text.toString().trim()
        val description = descriptionInput.text.toString().trim()
        val maxMembers = maxMembersInput.text.toString().toIntOrNull()?.coerceIn(2, 20) ?: 4
        if (name.isBlank()) {
            Toast.makeText(this, UiLanguageStore.pick(this, "Въведи име на отбора.", "Enter a team name first."), Toast.LENGTH_SHORT).show()
            return
        }

        val created = GameRepository.createTeam(this, name, description, maxMembers, openSwitch.isChecked)
        if (created == null) {
            Toast.makeText(this, UiLanguageStore.pick(this, "Този профил вече е в отбор.", "This account is already in a team."), Toast.LENGTH_SHORT).show()
            return
        }

        createInput.text?.clear()
        descriptionInput.text?.clear()
        maxMembersInput.setText("4")
        openSwitch.isChecked = true
        Toast.makeText(this, UiLanguageStore.pick(this, "${created.teamName} е създаден.", "${created.teamName} is ready."), Toast.LENGTH_SHORT).show()
        renderScreen()
    }

    private fun joinByCode() {
        val code = joinInput.text.toString().trim()
        if (code.isBlank()) {
            Toast.makeText(this, UiLanguageStore.pick(this, "Въведи код за покана.", "Enter an invite code."), Toast.LENGTH_SHORT).show()
            return
        }

        when (val result = GameRepository.joinTeamByCode(this, code)) {
            is JoinTeamResult.Joined -> {
                showJoinResult(result, result.team.teamName)
                joinInput.text?.clear()
                renderScreen()
            }
            JoinTeamResult.NotFound -> {
                if (MultiplayerRepository.isAvailable(this)) {
                    joinButton.isEnabled = false
                    MultiplayerRepository.joinTeamByCode(this, code) { cloudResult ->
                        runOnUiThread {
                            joinButton.isEnabled = true
                            showJoinResult(cloudResult, code)
                            if (cloudResult is JoinTeamResult.Joined || cloudResult is JoinTeamResult.PendingApproval) {
                                joinInput.text?.clear()
                            }
                            renderScreen()
                        }
                    }
                } else {
                    showJoinResult(result, "")
                    renderScreen()
                }
            }
            else -> {
                showJoinResult(result, "")
                if (result is JoinTeamResult.PendingApproval) joinInput.text?.clear()
                renderScreen()
            }
        }
    }

    private fun approveNextRequest() {
        val nextRequest = GameRepository.loadTeam(this).pendingRequests.firstOrNull()
        if (nextRequest == null) {
            Toast.makeText(this, UiLanguageStore.pick(this, "Няма чакащи заявки.", "No pending requests."), Toast.LENGTH_SHORT).show()
            return
        }

        val approved = GameRepository.approveRequest(this, nextRequest)
        if (approved == null) {
            Toast.makeText(this, UiLanguageStore.pick(this, "Заявката не може да бъде одобрена.", "Could not approve the request."), Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, UiLanguageStore.pick(this, "$nextRequest вече е в отбора.", "$nextRequest joined the team."), Toast.LENGTH_SHORT).show()
            renderScreen()
        }
    }

    private fun leaveTeam() {
        val leftTeam = GameRepository.leaveCurrentTeam(this)
        if (leftTeam == null) {
            Toast.makeText(this, UiLanguageStore.pick(this, "Собственикът не може да напусне преди да има прехвърляне на собственост.", "Team owners cannot leave until ownership transfer exists."), Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, UiLanguageStore.pick(this, "Напусна ${leftTeam.teamName}.", "You left ${leftTeam.teamName}."), Toast.LENGTH_SHORT).show()
            renderScreen()
        }
    }

    private fun showJoinResult(result: JoinTeamResult, teamName: String) {
        when (result) {
            is JoinTeamResult.Joined -> Toast.makeText(this, UiLanguageStore.pick(this, "Присъедини се към ${result.team.teamName}.", "Joined ${result.team.teamName}."), Toast.LENGTH_SHORT).show()
            JoinTeamResult.PendingApproval -> Toast.makeText(this, UiLanguageStore.pick(this, "Заявката е изпратена${if (teamName.isNotBlank()) " към $teamName" else ""}.", "Request sent${if (teamName.isNotBlank()) " to $teamName" else ""}."), Toast.LENGTH_SHORT).show()
            JoinTeamResult.TeamFull -> Toast.makeText(this, UiLanguageStore.pick(this, "Този отбор е пълен.", "That team is full."), Toast.LENGTH_SHORT).show()
            JoinTeamResult.NotFound -> Toast.makeText(this, UiLanguageStore.pick(this, "Кодът за покана не е намерен.", "Invite code not found on this device yet."), Toast.LENGTH_LONG).show()
            JoinTeamResult.AlreadyInTeam -> Toast.makeText(this, UiLanguageStore.pick(this, "Вече си в отбор.", "You are already in a team."), Toast.LENGTH_SHORT).show()
        }
    }
}
