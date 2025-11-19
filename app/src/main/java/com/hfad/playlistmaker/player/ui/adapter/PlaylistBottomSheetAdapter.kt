package com.hfad.playlistmaker.player.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.hfad.playlistmaker.R
import com.hfad.playlistmaker.databinding.ItemPlaylistBottomSheetBinding
import com.hfad.playlistmaker.playlist.domain.models.Playlist

class PlaylistBottomSheetAdapter(
    private var playlists: List<Playlist>,
    private val onClick: (Playlist) -> Unit
) : RecyclerView.Adapter<PlaylistBottomSheetAdapter.PlaylistViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        val binding = ItemPlaylistBottomSheetBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PlaylistViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        holder.bind(playlists[position])
    }

    override fun getItemCount(): Int = playlists.size

    fun updatePlaylists(newPlaylists: List<Playlist>) {
        playlists = newPlaylists
        notifyDataSetChanged()
    }

    inner class PlaylistViewHolder(
        private val binding: ItemPlaylistBottomSheetBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(playlist: Playlist) {
            binding.playlistName.text = playlist.name
            binding.trackCount.text = itemView.context.resources.getQuantityString(
                R.plurals.tracks_count,
                playlist.trackCount,
                playlist.trackCount
            )

            playlist.coverPath?.let { coverPath ->
                Glide.with(itemView)
                    .load(coverPath)
                    .placeholder(R.drawable.playsholder_play_light)
                    .centerCrop()
                    .into(binding.playlistCover)
            } ?: run {
                binding.playlistCover.setImageResource(R.drawable.playsholder_play_light)
            }

            itemView.setOnClickListener {
                onClick(playlist)
            }
        }
    }
}