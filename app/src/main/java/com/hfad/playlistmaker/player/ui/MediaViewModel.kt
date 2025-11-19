package com.hfad.playlistmaker.player.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hfad.playlistmaker.library.domain.interactor.FavoriteTracksInteractor
import com.hfad.playlistmaker.player.domain.interactor.PlayerInteractor
import com.hfad.playlistmaker.player.domain.models.PlayerState
import com.hfad.playlistmaker.playlist.domain.interactor.PlaylistInteractor
import com.hfad.playlistmaker.playlist.domain.models.Playlist
import com.hfad.playlistmaker.search.domain.models.Track
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class MediaViewModel(
    private val playerInteractor: PlayerInteractor,
    private val favoriteTracksInteractor: FavoriteTracksInteractor,
    private val playlistInteractor: PlaylistInteractor
) : ViewModel() {

    private val _addToPlaylistStatus = MutableLiveData<AddToPlaylistStatus>()
    val addToPlaylistStatus: LiveData<AddToPlaylistStatus> = _addToPlaylistStatus
    private val _playerState = MutableLiveData<PlayerState>()
    val playerState: LiveData<PlayerState> = _playerState

    private val _currentPosition = MutableLiveData<Int>()
    val currentPosition: LiveData<Int> = _currentPosition

    private val _isFavorite = MutableLiveData<Boolean>()
    val isFavorite: LiveData<Boolean> = _isFavorite

    private val _playlists = MutableLiveData<List<Playlist>>()
    val playlists: LiveData<List<Playlist>> = _playlists

    private var currentTrack: Track? = null
    private var progressUpdateJob: Job? = null

    init {
        _playerState.value = playerInteractor.getPlayerState()
        startProgressUpdates()

        playerInteractor.setOnCompletionListener {
            _playerState.postValue(PlayerState.PREPARED)
            _currentPosition.postValue(0)
            stopProgressUpdates()
        }
    }

    fun setTrack(track: Track) {
        currentTrack = track
        _isFavorite.value = track.isFavorite
    }

    fun loadPlaylists() {
        viewModelScope.launch {
            playlistInteractor.getAllPlaylists().collect { playlists ->
                _playlists.postValue(playlists)
            }
        }
    }

    fun preparePlayer(previewUrl: String) {
        playerInteractor.preparePlayer(previewUrl)
        playerInteractor.setOnPreparedListener {
            _playerState.postValue(PlayerState.PREPARED)
        }

        playerInteractor.setOnCompletionListener {
            _playerState.postValue(PlayerState.PREPARED)
            _currentPosition.postValue(0)
            stopProgressUpdates()
        }

        _playerState.value = PlayerState.DEFAULT
    }

    fun playPause() {
        when (playerInteractor.getPlayerState()) {
            PlayerState.PLAYING -> {
                playerInteractor.pausePlayer()
                _playerState.value = PlayerState.PAUSED
                stopProgressUpdates()
            }
            PlayerState.PREPARED, PlayerState.PAUSED -> {
                playerInteractor.startPlayer()
                _playerState.value = PlayerState.PLAYING
                startProgressUpdates()
            }
            else -> {}
        }
    }

    fun onFavoriteClicked() {
        val track = currentTrack ?: return
        viewModelScope.launch {
            if (track.isFavorite) {
                favoriteTracksInteractor.removeTrackFromFavorites(track)
            } else {
                favoriteTracksInteractor.addTrackToFavorites(track)
            }
            track.isFavorite = !track.isFavorite
            _isFavorite.postValue(track.isFavorite)
        }
    }

    private fun startProgressUpdates() {
        stopProgressUpdates()
        progressUpdateJob = viewModelScope.launch {
            while (isActive) {
                if (playerInteractor.getPlayerState() == PlayerState.PLAYING) {
                    _currentPosition.postValue(playerInteractor.getCurrentPosition())
                }
                delay(PROGRESS_UPDATE_DELAY)
            }
        }
    }

    private fun stopProgressUpdates() {
        progressUpdateJob?.cancel()
        progressUpdateJob = null
    }

    override fun onCleared() {
        super.onCleared()
        stopProgressUpdates()
        playerInteractor.releasePlayer()
    }

    fun addTrackToPlaylist(track: Track, playlist: Playlist) {
        viewModelScope.launch {
            try {
                val isTrackInPlaylist = playlistInteractor.isTrackInPlaylist(track.trackId, playlist)

                if (isTrackInPlaylist) {
                    _addToPlaylistStatus.postValue(
                        AddToPlaylistStatus.TrackAlreadyExists(playlist.name)
                    )
                } else {
                    playlistInteractor.addTrackToPlaylist(track, playlist)
                    _addToPlaylistStatus.postValue(
                        AddToPlaylistStatus.Success(playlist.name)
                    )
                }
            } catch (e: Exception) {
                _addToPlaylistStatus.postValue(AddToPlaylistStatus.Error)
            }
        }
    }

    companion object {
        private const val PROGRESS_UPDATE_DELAY = 300L
    }
}

sealed class AddToPlaylistStatus {
    data class Success(val playlistName: String) : AddToPlaylistStatus()
    data class TrackAlreadyExists(val playlistName: String) : AddToPlaylistStatus()
    object Error : AddToPlaylistStatus()
}