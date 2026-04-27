package com.example.game_vol1.admin.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.game_vol1.R
import com.example.game_vol1.database.entity.GeoLocationEntity

class GeoLocationAdapter(
    private val onEdit: (GeoLocationEntity) -> Unit,
    private val onDelete: (GeoLocationEntity) -> Unit
) : ListAdapter<GeoLocationEntity, GeoLocationAdapter.VH>(Diff) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(LayoutInflater.from(parent.context).inflate(R.layout.item_geolocation, parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        private val tvName: TextView = view.findViewById(R.id.tvGeoName)
        private val tvLocation: TextView = view.findViewById(R.id.tvGeoLocation)
        private val tvDifficulty: TextView = view.findViewById(R.id.tvGeoDifficulty)
        private val tvCoords: TextView = view.findViewById(R.id.tvGeoCoords)
        private val btnEdit: ImageButton = view.findViewById(R.id.btnEditGeo)
        private val btnDelete: ImageButton = view.findViewById(R.id.btnDeleteGeo)

        fun bind(geo: GeoLocationEntity) {
            tvName.text = geo.name
            tvLocation.text = "${geo.city}, ${geo.country}"
            tvDifficulty.text = geo.difficulty
            tvCoords.text = "%.4f°, %.4f°".format(geo.latitude, geo.longitude)

            val diffColor = when (geo.difficulty) {
                "Easy" -> ContextCompat.getColor(itemView.context, R.color.admin_green)
                "Hard" -> ContextCompat.getColor(itemView.context, R.color.admin_red)
                else -> ContextCompat.getColor(itemView.context, R.color.admin_orange)
            }
            tvDifficulty.setTextColor(diffColor)

            btnEdit.setOnClickListener { onEdit(geo) }
            btnDelete.setOnClickListener { onDelete(geo) }
        }
    }

    object Diff : DiffUtil.ItemCallback<GeoLocationEntity>() {
        override fun areItemsTheSame(a: GeoLocationEntity, b: GeoLocationEntity) = a.id == b.id
        override fun areContentsTheSame(a: GeoLocationEntity, b: GeoLocationEntity) = a == b
    }
}
