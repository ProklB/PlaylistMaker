package com.hfad.playlistmaker.player.domain.repository

import com.hfad.playlistmaker.player.domain.models.PlayerState

interface PlayerRepository {
    fun preparePlayer(previewUrl: String)
    fun startPlayer()
    fun pausePlayer()
    fun releasePlayer()
    fun getCurrentPosition(): Int
    fun getPlayerState(): PlayerState
    fun setOnPreparedListener(listener: () -> Unit)
    fun setOnCompletionListener(listener: () -> Unit)
}