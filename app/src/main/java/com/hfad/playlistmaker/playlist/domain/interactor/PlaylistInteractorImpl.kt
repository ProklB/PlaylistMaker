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

    override suspend fun addTrackToPlaylist(track: Track, playlist: Playlist): Boolean {
        return repository.addTrackToPlaylist(track, playlist)
    }

    override suspend fun isTrackInPlaylist(trackId: Int, playlist: Playlist): Boolean {
        return repository.isTrackInPlaylist(trackId, playlist)
    }

    override suspend fun getPlaylistTracks(playlist: Playlist): List<Track> {
        return repository.getPlaylistTracks(playlist)
    }

    override suspend fun getPlaylistById(playlistId: Long): Playlist? {
        return repository.getPlaylistById(playlistId)
    }

    override suspend fun removeTrackFromPlaylist(trackId: Int, playlist: Playlist) {
        repository.removeTrackFromPlaylist(trackId, playlist)
    }

    override suspend fun getShareText(playlist: Playlist): String {
        return repository.getShareText(playlist)
    }

    override suspend fun deletePlaylist(playlistId: Long) {
        repository.deletePlaylist(playlistId)
    }
}