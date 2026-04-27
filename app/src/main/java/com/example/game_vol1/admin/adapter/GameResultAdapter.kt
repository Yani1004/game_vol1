package com.example.game_vol1.admin.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.game_vol1.R
import com.example.game_vol1.database.entity.GameResultEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GameResultAdapter : ListAdapter<GameResultEntity, GameResultAdapter.VH>(Diff) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(LayoutInflater.from(parent.context).inflate(R.layout.item_game_result, parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        private val tvLocation: TextView = view.findViewById(R.id.tvResultLocation)
        private val tvScore: TextView = view.findViewById(R.id.tvResultScore)
        private val tvDate: TextView = view.findViewById(R.id.tvResultDate)
        private val tvResult: TextView = view.findViewById(R.id.tvResultCorrect)
        private val tvDistance: TextView = view.findViewById(R.id.tvResultDistance)

        fun bind(r: GameResultEntity) {
            tvLocation.text = r.geoLocationName
            tvScore.text = "${r.score} pts"
            tvDate.text = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date(r.playedAt))
            tvDistance.text = "%.1f km off".format(r.distanceKm)
            if (r.isCorrect) {
                tvResult.text = "Correct"
                tvResult.setTextColor(ContextCompat.getColor(itemView.context, R.color.admin_green))
            } else {
                tvResult.text = "Incorrect"
                tvResult.setTextColor(ContextCompat.getColor(itemView.context, R.color.admin_red))
            }
        }
    }

    object Diff : DiffUtil.ItemCallback<GameResultEntity>() {
        override fun areItemsTheSame(a: GameResultEntity, b: GameResultEntity) = a.id == b.id
        override fun areContentsTheSame(a: GameResultEntity, b: GameResultEntity) = a == b
    }
}
