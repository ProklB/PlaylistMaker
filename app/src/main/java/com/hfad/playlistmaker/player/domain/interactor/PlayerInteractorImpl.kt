package com.hfad.playlistmaker.player.domain.interactor

import com.hfad.playlistmaker.player.domain.models.PlayerState
import com.hfad.playlistmaker.player.domain.repository.PlayerRepository

class PlayerInteractorImpl(
    private val playerRepository: PlayerRepository
) : PlayerInteractor {

    override fun preparePlayer(previewUrl: String) {
        playerRepository.preparePlayer(previewUrl)
    }

    override fun startPlayer() {
        playerRepository.startPlayer()
    }

    override fun pausePlayer() {
        playerRepository.pausePlayer()
    }

    override fun releasePlayer() {
        playerRepository.releasePlayer()
    }

    override fun getCurrentPosition(): Int {
        return playerRepository.getCurrentPosition()
    }

    override fun getPlayerState(): PlayerState {
        return playerRepository.getPlayerState()
    }

    override fun setOnPreparedListener(listener: () -> Unit) {
        playerRepository.setOnPreparedListener(listener)
    }

    override fun setOnCompletionListener(listener: () -> Unit) {
        playerRepository.setOnCompletionListener(listener)
    }
}