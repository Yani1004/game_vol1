package com.example.game_vol1.admin.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.example.game_vol1.R
import com.example.game_vol1.database.entity.PlayerEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PlayerAdapter(
    private val onViewDetails: (PlayerEntity) -> Unit
) : ListAdapter<PlayerEntity, PlayerAdapter.VH>(Diff) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(LayoutInflater.from(parent.context).inflate(R.layout.item_player, parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        private val tvAvatar: TextView = view.findViewById(R.id.tvPlayerAvatar)
        private val tvUsername: TextView = view.findViewById(R.id.tvPlayerUsername)
        private val tvEmail: TextView = view.findViewById(R.id.tvPlayerEmail)
        private val tvScore: TextView = view.findViewById(R.id.tvPlayerScore)
        private val tvGames: TextView = view.findViewById(R.id.tvPlayerGames)
        private val tvDate: TextView = view.findViewById(R.id.tvPlayerRegDate)
        private val tvRank: TextView = view.findViewById(R.id.tvPlayerRank)
        private val tvQuickStats: TextView = view.findViewById(R.id.tvPlayerQuickStats)
        private val btnViewDetails: MaterialButton = view.findViewById(R.id.btnViewDetails)

        fun bind(player: PlayerEntity) {
            val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            tvAvatar.text = player.username.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
            tvUsername.text = player.username
            tvEmail.text = player.email
            tvScore.text = "${player.totalScore} pts"
            tvGames.text = "${player.gamesPlayed} games"
            tvDate.text = formatter.format(Date(player.registrationDate))
            tvRank.text = rank(player.totalScore)

            val average = if (player.gamesPlayed > 0) player.totalScore / player.gamesPlayed else 0
            val accuracy = if (player.totalGuesses > 0) {
                "%.0f%%".format(player.correctGuesses * 100f / player.totalGuesses)
            } else {
                "N/A"
            }
            val lastPlayed = if (player.lastPlayedDate > 0) formatter.format(Date(player.lastPlayedDate)) else "Never"
            tvQuickStats.text = "Best: ${player.bestScore} · Avg: $average · Last: $lastPlayed · Correct: $accuracy"

            itemView.setOnClickListener { onViewDetails(player) }
            btnViewDetails.setOnClickListener { onViewDetails(player) }
        }

        private fun rank(score: Int) = when {
            score >= 8000 -> "Master"
            score >= 5000 -> "Expert"
            score >= 2500 -> "Navigator"
            score >= 1000 -> "Explorer"
            else -> "Beginner"
        }
    }

    object Diff : DiffUtil.ItemCallback<PlayerEntity>() {
        override fun areItemsTheSame(a: PlayerEntity, b: PlayerEntity) = a.id == b.id
        override fun areContentsTheSame(a: PlayerEntity, b: PlayerEntity) = a == b
    }
}
