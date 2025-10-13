package com.example.new1

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class EstablishmentAdapter(
    private val items: List<Establishment>
) : RecyclerView.Adapter<EstablishmentAdapter.EstablishmentViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EstablishmentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_establishment, parent, false)
        return EstablishmentViewHolder(view)
    }

    override fun onBindViewHolder(holder: EstablishmentViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class EstablishmentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleView: TextView = itemView.findViewById(R.id.text_establishment_name)
        private val photoIcon: ImageView = itemView.findViewById(R.id.icon_establishment_photos)
        private val menuIcon: ImageView = itemView.findViewById(R.id.icon_establishment_menu)

        fun bind(item: Establishment) {
            titleView.text = item.name
            photoIcon.contentDescription = itemView.context.getString(
                R.string.content_description_establishment_photos, item.name
            )
            menuIcon.contentDescription = itemView.context.getString(
                R.string.content_description_establishment_menu, item.name
            )
        }
    }
}

