package com.hfad.playlistmaker.search.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hfad.playlistmaker.R
import com.hfad.playlistmaker.search.domain.models.Track

class TrackAdapter(
    private var trackList: List<Track>,
    private val onClick: (Track) -> Unit
) : RecyclerView.Adapter<TrackViewHolder>() {

    private var onLongClick: ((Track) -> Unit)? = null

    fun setOnLongClickListener(listener: (Track) -> Unit) {
        onLongClick = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_track_search, parent, false)
        return TrackViewHolder(view)
    }

    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        holder.bind(trackList[position], onClick, onLongClick)
    }

    override fun getItemCount(): Int = trackList.size

    fun updateTracks(newTracks: List<Track>) {
        trackList = newTracks
        notifyDataSetChanged()
    }
}