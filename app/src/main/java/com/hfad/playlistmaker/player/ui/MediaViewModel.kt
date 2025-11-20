package com.hfad.playlistmaker.player.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hfad.playlistmaker.library.domain.interactor.FavoriteTracksInteractor
import com.hfad.playlistmaker.player.domain.interactor.PlayerInteractor
import com.hfad.playlistmaker.player.domain.models.PlayerState
import com.hfad.playlistmaker.player.ui.model.AddToPlaylistStatus
import com.hfad.playlistmaker.player.ui.model.PlayerScreenState
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

    private val _playerScreenState = MutableLiveData<PlayerScreenState>()
    val playerScreenState: LiveData<PlayerScreenState> = _playerScreenState

    private val _playlists = MutableLiveData<List<Playlist>>()
    val playlists: LiveData<List<Playlist>> = _playlists

    private var currentTrack: Track? = null
    private var progressUpdateJob: Job? = null

    init {
        _playerScreenState.value = PlayerScreenState(
            playerState = playerInteractor.getPlayerState(),
            currentPosition = 0,
            isFavorite = false
        )
        startProgressUpdates()

        playerInteractor.setOnCompletionListener {
            updatePlayerScreenState { currentState ->
                currentState.copy(
                    playerState = PlayerState.PREPARED,
                    currentPosition = 0
                )
            }
            stopProgressUpdates()
        }
    }

    fun setTrack(track: Track) {
        currentTrack = track
        updatePlayerScreenState { currentState ->
            currentState.copy(isFavorite = track.isFavorite)
        }
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
            updatePlayerScreenState { currentState ->
                currentState.copy(playerState = PlayerState.PREPARED)
            }
        }

        playerInteractor.setOnCompletionListener {
            updatePlayerScreenState { currentState ->
                currentState.copy(
                    playerState = PlayerState.PREPARED,
                    currentPosition = 0
                )
            }
            stopProgressUpdates()
        }

        updatePlayerScreenState { currentState ->
            currentState.copy(playerState = PlayerState.DEFAULT)
        }
    }

    fun playPause() {
        when (playerInteractor.getPlayerState()) {
            PlayerState.PLAYING -> {
                playerInteractor.pausePlayer()
                updatePlayerScreenState { currentState ->
                    currentState.copy(playerState = PlayerState.PAUSED)
                }
                stopProgressUpdates()
            }
            PlayerState.PREPARED, PlayerState.PAUSED -> {
                playerInteractor.startPlayer()
                updatePlayerScreenState { currentState ->
                    currentState.copy(playerState = PlayerState.PLAYING)
                }
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
            updatePlayerScreenState { currentState ->
                currentState.copy(isFavorite = track.isFavorite)
            }
        }
    }

    private fun startProgressUpdates() {
        stopProgressUpdates()
        progressUpdateJob = viewModelScope.launch {
            while (isActive) {
                if (playerInteractor.getPlayerState() == PlayerState.PLAYING) {
                    updatePlayerScreenState { currentState ->
                        currentState.copy(currentPosition = playerInteractor.getCurrentPosition())
                    }
                }
                delay(PROGRESS_UPDATE_DELAY)
            }
        }
    }

    private fun stopProgressUpdates() {
        progressUpdateJob?.cancel()
        progressUpdateJob = null
    }

    private fun updatePlayerScreenState(update: (PlayerScreenState) -> PlayerScreenState) {
        val currentState = _playerScreenState.value ?: PlayerScreenState()
        _playerScreenState.postValue(update(currentState))
    }

    override fun onCleared() {
        super.onCleared()
        stopProgressUpdates()
        playerInteractor.releasePlayer()
    }

    fun addTrackToPlaylist(track: Track, playlist: Playlist) {
        viewModelScope.launch {
            try {
                val isAdded = playlistInteractor.addTrackToPlaylist(track, playlist)

                if (isAdded) {
                    _addToPlaylistStatus.postValue(
                        AddToPlaylistStatus.Success(playlist.name)
                    )
                } else {
                    _addToPlaylistStatus.postValue(
                        AddToPlaylistStatus.TrackAlreadyExists(playlist.name)
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