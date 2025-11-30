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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.tracklist_search, parent, false)
        return TrackViewHolder(view)
    }

    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        holder.bind(trackList[position], onClick)
    }

    override fun getItemCount(): Int = trackList.size

    fun updateTracks(newTracks: List<Track>) {
        trackList = newTracks
        notifyDataSetChanged()
    }
}