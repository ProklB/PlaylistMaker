package com.hfad.playlistmaker.player.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hfad.playlistmaker.library.domain.interactor.FavoriteTracksInteractor
import com.hfad.playlistmaker.player.domain.models.PlayerState
import com.hfad.playlistmaker.player.service.PlayerServiceInterface
import com.hfad.playlistmaker.player.ui.model.AddToPlaylistStatus
import com.hfad.playlistmaker.player.ui.model.PlayerScreenState
import com.hfad.playlistmaker.playlist.domain.interactor.PlaylistInteractor
import com.hfad.playlistmaker.playlist.domain.models.Playlist
import com.hfad.playlistmaker.search.domain.models.Track
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MediaViewModel(
    private val favoriteTracksInteractor: FavoriteTracksInteractor,
    private val playlistInteractor: PlaylistInteractor
) : ViewModel() {
    private val _addToPlaylistStatus = MutableLiveData<AddToPlaylistStatus>()

    private val _playerScreenState = MutableLiveData<PlayerScreenState>()
    val playerScreenState: LiveData<PlayerScreenState> = _playerScreenState

    private val _playlists = MutableLiveData<List<Playlist>>()
    val playlists: LiveData<List<Playlist>> = _playlists

    private val _isBottomSheetOpen = MutableLiveData(false)

    private var currentTrack: Track? = null
    private var playerService: PlayerServiceInterface? = null

    private var playerStateJob: Job? = null
    private var progressJob: Job? = null

    init {
        _playerScreenState.value = PlayerScreenState(
            playerState = PlayerState.DEFAULT,
            currentPosition = 0,
            isFavorite = false
        )
    }

    fun setPlayerService(service: PlayerServiceInterface) {
        playerService = service
        startObservingPlayerState()
        startObservingProgress()

        _playerScreenState.value = _playerScreenState.value?.copy(
            playerState = service.getPlayerState()
        )
    }

    fun clearPlayerService() {
        stopObservingPlayerState()
        stopObservingProgress()
        playerService = null
    }

    fun setTrack(track: Track) {
        currentTrack = track
        viewModelScope.launch {
            val isFavorite = favoriteTracksInteractor.isTrackInFavorites(track.trackId)
            updatePlayerScreenState { currentState ->
                currentState.copy(isFavorite = isFavorite)
            }
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
        currentTrack?.let { track ->
            playerService?.preparePlayer(track)
        }
    }

    fun playPause() {
        val service = playerService ?: return

        when (service.getPlayerState()) {
            PlayerState.PLAYING -> {
                service.pause()
                hideNotificationIfNeeded()
            }
            PlayerState.PREPARED, PlayerState.PAUSED -> {
                viewModelScope.launch {
                    service.play()
                    delay(PlayerServiceInterface.PLAY_DELAY_MS)
                }
            }
            PlayerState.DEFAULT -> {
                currentTrack?.let { track ->
                    service.preparePlayer(track)
                }
            }
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

    private fun startObservingPlayerState() {
        playerStateJob?.cancel()
        playerStateJob = viewModelScope.launch {
            playerService?.getPlayerStateFlow()?.collect { newState ->
                updatePlayerScreenState { currentState ->
                    currentState.copy(playerState = newState)
                }
            }
        }
    }

    private fun stopObservingPlayerState() {
        playerStateJob?.cancel()
        playerStateJob = null
    }

    private fun startObservingProgress() {
        progressJob?.cancel()
        progressJob = viewModelScope.launch {
            playerService?.getCurrentPositionFlow()?.collect { position ->
                if (!(_isBottomSheetOpen.value ?: false)) {
                    updatePlayerScreenState { currentState ->
                        currentState.copy(currentPosition = position)
                    }
                }
            }
        }
    }

    private fun stopObservingProgress() {
        progressJob?.cancel()
        progressJob = null
    }

    private fun updatePlayerScreenState(update: (PlayerScreenState) -> PlayerScreenState) {
        val currentState = _playerScreenState.value ?: PlayerScreenState()
        val newState = update(currentState)

        if (_isBottomSheetOpen.value == true) {
            _playerScreenState.postValue(newState.copy(currentPosition = currentState.currentPosition))
        } else {
            _playerScreenState.postValue(newState)
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopObservingPlayerState()
        stopObservingProgress()
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

    fun setBottomSheetOpen(isOpen: Boolean) {
        _isBottomSheetOpen.value = isOpen
        if (isOpen) {
            stopObservingProgress()
        } else {
            if (playerService?.getPlayerState() == PlayerState.PLAYING) {
                startObservingProgress()
            }
        }
    }

    fun showNotification() {
        playerService?.showNotification()
    }

    fun hideNotification() {
        playerService?.hideNotification()
    }

    private fun hideNotificationIfNeeded() {
        val state = _playerScreenState.value
        if (state?.playerState != PlayerState.PLAYING) {
            playerService?.hideNotification()
        }
    }
}