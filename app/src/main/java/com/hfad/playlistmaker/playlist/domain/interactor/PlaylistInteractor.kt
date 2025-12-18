package com.hfad.playlistmaker.playlist.domain.interactor

import com.hfad.playlistmaker.playlist.domain.models.Playlist
import com.hfad.playlistmaker.search.domain.models.Track
import kotlinx.coroutines.flow.Flow

interface PlaylistInteractor {
    suspend fun createPlaylist(playlist: Playlist)
    fun getAllPlaylists(): Flow<List<Playlist>>
    suspend fun addTrackToPlaylist(track: Track, playlist: Playlist): Boolean
    suspend fun isTrackInPlaylist(trackId: Int, playlist: Playlist): Boolean
    suspend fun getPlaylistTracks(playlist: Playlist): List<Track>
    suspend fun getPlaylistById(playlistId: Long): Playlist?
    suspend fun removeTrackFromPlaylist(trackId: Int, playlist: Playlist)
    suspend fun getShareText(playlist: Playlist): String
    suspend fun deletePlaylist(playlistId: Long)
}