package com.hfad.playlistmaker.presentation.search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.hfad.playlistmaker.R
import com.hfad.playlistmaker.domain.models.Track
import java.text.SimpleDateFormat
import java.util.Locale

class TrackAdapter(
    private var trackList:List<Track>,
    private val onClick: (Track) -> Unit
):RecyclerView.Adapter<TrackViewHolder> () {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.tracklist_search, parent, false)
        return TrackViewHolder(view)
    }

    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        holder.bind(trackList[position], onClick)
    }

    override fun getItemCount(): Int {
        return trackList.size
    }

    fun updateTracks(newTracks: List<Track>) {
        trackList = newTracks
        notifyDataSetChanged()
    }

}

class TrackViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val tracknameTextView: TextView = itemView.findViewById(R.id.trackNameTextView)
    private val artistNameTextView: TextView = itemView.findViewById(R.id.artistNameTextView)
    private val trackTimeTextView: TextView = itemView.findViewById(R.id.trackTimeTextView)
    private var artworkImageView: ImageView = itemView.findViewById(R.id.artworkImageView)

    fun bind(model: Track, onClick: (Track) -> Unit)  {
        tracknameTextView.text = model.trackName
        artistNameTextView.text = model.artistName
        trackTimeTextView.text = SimpleDateFormat("mm:ss", Locale.getDefault()).format(model.trackTimeMillis)

        Glide.with(itemView.context)
            .load(model.artworkUrl100)
            .centerCrop()
            .placeholder(R.drawable.placeholder)
            .into(artworkImageView)

        itemView.setOnClickListener {
            onClick(model)
        }

    }

}