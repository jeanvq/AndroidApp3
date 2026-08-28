package com.jeancarlo.androidapp3

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.jeancarlo.androidapp3.data.TreasurePlace
import com.jeancarlo.androidapp3.databinding.ItemTreasurePlaceBinding

/**
 * RecyclerView adapter for the 20 hunt locations.
 */
class TreasurePlaceAdapter(
    private val onPlaceClicked: (TreasurePlace) -> Unit
) : RecyclerView.Adapter<TreasurePlaceAdapter.PlaceViewHolder>() {

    private val places = mutableListOf<TreasurePlace>()

    fun submitPlaces(newPlaces: List<TreasurePlace>) {
        places.clear()
        places.addAll(newPlaces)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceViewHolder {
        val binding = ItemTreasurePlaceBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PlaceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PlaceViewHolder, position: Int) {
        holder.bind(places[position])
    }

    override fun getItemCount(): Int = places.size

    inner class PlaceViewHolder(
        private val binding: ItemTreasurePlaceBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(place: TreasurePlace) {
            binding.stopNumberText.text = "#${place.huntOrder}"
            binding.placeNameText.text = place.name
            binding.addressText.text = place.address
            binding.statusText.text =
                if (place.isVisited) "VISITED" else "NOT VISITED"

            val statusColor = if (place.isVisited) {
                R.color.success_green
            } else {
                R.color.text_secondary
            }

            binding.statusText.setTextColor(
                ContextCompat.getColor(binding.root.context, statusColor)
            )

            binding.root.setOnClickListener {
                onPlaceClicked(place)
            }
        }
    }
}
