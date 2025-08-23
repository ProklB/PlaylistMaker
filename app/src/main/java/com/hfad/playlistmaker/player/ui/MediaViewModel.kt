package com.hfad.playlistmaker.player.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hfad.playlistmaker.player.domain.interactor.PlayerInteractor
import com.hfad.playlistmaker.player.domain.models.PlayerState
import java.util.Timer
import java.util.TimerTask

class MediaViewModel(
    private val playerInteractor: PlayerInteractor
) : ViewModel() {

    private val _playerState = MutableLiveData<PlayerState>()
    val playerState: LiveData<PlayerState> = _playerState

    private val _currentPosition = MutableLiveData<Int>()
    val currentPosition: LiveData<Int> = _currentPosition

    private var timer: Timer? = null

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
            }
            PlayerState.PREPARED, PlayerState.PAUSED -> {
                playerInteractor.startPlayer()
                _playerState.value = PlayerState.PLAYING
            }
            else -> {}
        }
    }

    private fun startProgressUpdates() {
        timer = Timer().apply {
            scheduleAtFixedRate(object : TimerTask() {
                override fun run() {
                    if (playerInteractor.getPlayerState() == PlayerState.PLAYING) {
                        _currentPosition.postValue(playerInteractor.getCurrentPosition())
                    }
                }
            }, 0, 300)
        }
    }

    override fun onCleared() {
        super.onCleared()
        timer?.cancel()
        playerInteractor.releasePlayer()
    }
}