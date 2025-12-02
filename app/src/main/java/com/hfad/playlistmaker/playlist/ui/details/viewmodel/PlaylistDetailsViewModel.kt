package com.hfad.playlistmaker.playlist.ui.details.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hfad.playlistmaker.playlist.domain.interactor.PlaylistInteractor
import com.hfad.playlistmaker.playlist.domain.models.Playlist
import com.hfad.playlistmaker.search.domain.models.Track
import kotlinx.coroutines.launch

class PlaylistDetailsViewModel(
    private val playlistInteractor: PlaylistInteractor
) : ViewModel() {

    private val _playlistDetailsState = MutableLiveData<PlaylistDetailsState>()
    val playlistDetailsState: LiveData<PlaylistDetailsState> = _playlistDetailsState

    private val _navigateToLibrary = MutableLiveData<Boolean>()
    val navigateToLibrary: LiveData<Boolean> = _navigateToLibrary

    private var currentPlaylist: Playlist? = null

    fun loadPlaylist(playlistId: Long) {
        viewModelScope.launch {
            _playlistDetailsState.value = PlaylistDetailsState.Loading

            try {
                val playlist = playlistInteractor.getPlaylistById(playlistId)
                currentPlaylist = playlist

                if (playlist != null) {
                    val tracks = playlistInteractor.getPlaylistTracks(playlist)
                    val totalDuration = tracks.sumOf { it.trackTimeMillis }

                    _playlistDetailsState.value = PlaylistDetailsState.Content(
                        playlist = playlist,
                        tracks = tracks,
                        totalDuration = totalDuration
                    )
                } else {
                    _playlistDetailsState.value = PlaylistDetailsState.Error
                }
            } catch (e: Exception) {
                _playlistDetailsState.value = PlaylistDetailsState.Error
            }
        }
    }

    fun removeTrack(trackId: Int) {
        viewModelScope.launch {
            val playlist = currentPlaylist ?: return@launch
            playlistInteractor.removeTrackFromPlaylist(trackId, playlist)
            loadPlaylist(playlist.id)
        }
    }

    suspend fun getShareText(): String? {
        val playlist = currentPlaylist ?: return null
        return playlistInteractor.getShareText(playlist)
    }

    fun deletePlaylist() {
        val playlistId = currentPlaylist?.id ?: return

        viewModelScope.launch {
            try {
                playlistInteractor.deletePlaylist(playlistId)
                _navigateToLibrary.postValue(true)
            } catch (e: Exception) {
                _navigateToLibrary.postValue(true)
            }
        }
    }

    fun onNavigationComplete() {
        _navigateToLibrary.value = false
    }
}

sealed class PlaylistDetailsState {
    object Loading : PlaylistDetailsState()
    object Error : PlaylistDetailsState()
    data class Content(
        val playlist: Playlist,
        val tracks: List<Track>,
        val totalDuration: Long
    ) : PlaylistDetailsState()
}