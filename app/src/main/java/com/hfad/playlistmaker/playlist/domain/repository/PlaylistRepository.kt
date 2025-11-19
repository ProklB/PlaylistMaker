package com.hfad.playlistmaker.playlist.domain.repository

import com.hfad.playlistmaker.playlist.domain.models.Playlist
import com.hfad.playlistmaker.search.domain.models.Track
import kotlinx.coroutines.flow.Flow

interface PlaylistRepository {
    suspend fun createPlaylist(playlist: Playlist)
    fun getAllPlaylists(): Flow<List<Playlist>>
    suspend fun addTrackToPlaylist(track: Track, playlist: Playlist)
    suspend fun isTrackInPlaylist(trackId: Int, playlist: Playlist): Boolean
}