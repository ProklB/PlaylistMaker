package com.hfad.playlistmaker.player.domain.interactor

import com.hfad.playlistmaker.player.domain.models.PlayerState

interface PlayerInteractor {
    fun preparePlayer(previewUrl: String)
    fun setOnPreparedListener(listener: () -> Unit)
    fun setOnCompletionListener(listener: () -> Unit)
    fun startPlayer()
    fun pausePlayer()
    fun releasePlayer()
    fun getCurrentPosition(): Int
    fun getPlayerState(): PlayerState
}