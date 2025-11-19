package com.hfad.playlistmaker.playlist.domain.interactor

import com.hfad.playlistmaker.playlist.domain.models.Playlist
import com.hfad.playlistmaker.playlist.domain.repository.PlaylistRepository
import com.hfad.playlistmaker.search.domain.models.Track
import kotlinx.coroutines.flow.Flow

class PlaylistInteractorImpl(
    private val repository: PlaylistRepository
) : PlaylistInteractor {

    override suspend fun createPlaylist(playlist: Playlist) {
        repository.createPlaylist(playlist)
    }

    override fun getAllPlaylists(): Flow<List<Playlist>> {
        return repository.getAllPlaylists()
    }

    override suspend fun addTrackToPlaylist(track: Track, playlist: Playlist) {
        repository.addTrackToPlaylist(track, playlist)
    }

    override suspend fun isTrackInPlaylist(trackId: Int, playlist: Playlist): Boolean {
        return repository.isTrackInPlaylist(trackId, playlist)
    }
}