package com.hfad.playlistmaker.search.ui.adapter

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.hfad.playlistmaker.R
import com.hfad.playlistmaker.search.domain.models.Track
import java.text.SimpleDateFormat
import java.util.Locale

class TrackViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val tracknameTextView: TextView = itemView.findViewById(R.id.trackNameTextView)
    private val artistNameTextView: TextView = itemView.findViewById(R.id.artistNameTextView)
    private val trackTimeTextView: TextView = itemView.findViewById(R.id.trackTimeTextView)
    private var artworkImageView: ImageView = itemView.findViewById(R.id.artworkImageView)

    fun bind(model: Track, onClick: (Track) -> Unit, onLongClick: ((Track) -> Unit)? = null) {
        tracknameTextView.text = model.trackName
        artistNameTextView.text = model.artistName
        trackTimeTextView.text = SimpleDateFormat("mm:ss", Locale.getDefault())
            .format(model.trackTimeMillis)

        if (model.artworkUrl100.isNotEmpty()) {
            Glide.with(itemView.context)
                .load(model.artworkUrl100)
                .centerCrop()
                .placeholder(R.drawable.placeholder)
                .into(artworkImageView)
        } else {
            artworkImageView.setImageResource(R.drawable.placeholder)
        }

        itemView.setOnClickListener { onClick(model) }

        itemView.setOnLongClickListener {
            onLongClick?.invoke(model)
            true
        }
    }
}