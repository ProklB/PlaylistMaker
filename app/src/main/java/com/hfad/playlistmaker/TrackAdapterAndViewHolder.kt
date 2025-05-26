package com.hfad.playlistmaker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class TrackAdapter(private val trackList: List<Track>) : RecyclerView.Adapter<TrackViewHolder> () {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.tracklist_search, parent, false)
        return TrackViewHolder(view)
    }

    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        holder.bind(trackList[position])
    }

    override fun getItemCount(): Int {
        return trackList.size
    }
}

class TrackViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val tracknameTextView: TextView = itemView.findViewById(R.id.trackNameTextView)
    private val artistNameTextView: TextView = itemView.findViewById(R.id.artistNameTextView)
    private val trackTimeTextView: TextView = itemView.findViewById(R.id.trackTimeTextView)
    private var artworkImageView: ImageView = itemView.findViewById(R.id.artworkImageView)

    fun bind(model: Track){
        tracknameTextView.text = model.trackName
        artistNameTextView.text = model.artistName
        trackTimeTextView.text = model.trackTime

        Glide.with(itemView.context)
            .load(model.artworkUrl)
            .centerCrop()
            .placeholder(R.drawable.placeholder)
            .into(artworkImageView)
    }

}