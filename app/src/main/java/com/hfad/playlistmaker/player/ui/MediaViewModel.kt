package com.hfad.playlistmaker.player.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hfad.playlistmaker.player.domain.interactor.PlayerInteractor
import com.hfad.playlistmaker.player.domain.models.PlayerState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class MediaViewModel(
    private val playerInteractor: PlayerInteractor
) : ViewModel() {

    private val _playerState = MutableLiveData<PlayerState>()
    val playerState: LiveData<PlayerState> = _playerState

    private val _currentPosition = MutableLiveData<Int>()
    val currentPosition: LiveData<Int> = _currentPosition

    private var progressUpdateJob: Job? = null

    init {
        _playerState.value = playerInteractor.getPlayerState()
        startProgressUpdates()
    }

    fun preparePlayer(previewUrl: String) {
        playerInteractor.preparePlayer(previewUrl)
        playerInteractor.setOnPreparedListener {
            _playerState.postValue(PlayerState.PREPARED)
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

    companion object {
        private const val PROGRESS_UPDATE_DELAY = 300L
    }
}