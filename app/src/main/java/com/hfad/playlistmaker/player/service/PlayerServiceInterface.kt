package com.hfad.playlistmaker.player.service

import com.hfad.playlistmaker.player.domain.models.PlayerState
import com.hfad.playlistmaker.search.domain.models.Track
import kotlinx.coroutines.flow.StateFlow

interface PlayerServiceInterface {
    fun preparePlayer(track: Track)
    fun play()
    fun pause()
    fun getPlayerState(): PlayerState
    fun getCurrentPosition(): Int
    fun showNotification()
    fun hideNotification()
    fun getPlayerStateFlow(): StateFlow<PlayerState>
    fun getCurrentPositionFlow(): StateFlow<Int>

    companion object {
        const val PLAY_DELAY_MS = 50L
    }
}