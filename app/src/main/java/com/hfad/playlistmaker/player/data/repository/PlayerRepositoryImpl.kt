package com.hfad.playlistmaker.player.data.repository

import android.media.MediaPlayer
import com.hfad.playlistmaker.player.domain.models.PlayerState
import com.hfad.playlistmaker.player.domain.repository.PlayerRepository
import java.io.IOException

class PlayerRepositoryImpl(
    private val mediaPlayer: MediaPlayer
) : PlayerRepository {

    private var playerState: PlayerState = PlayerState.DEFAULT
    private var onPreparedListener: (() -> Unit)? = null
    private var onCompletionListener: (() -> Unit)? = null

    override fun preparePlayer(previewUrl: String) {
        mediaPlayer.apply {
            try {
                reset()
                setDataSource(previewUrl)
                prepareAsync()
                setOnPreparedListener {
                    playerState = PlayerState.PREPARED
                    onPreparedListener?.invoke()
                }
                setOnCompletionListener {
                    playerState = PlayerState.PREPARED
                    onCompletionListener?.invoke()
                }
            } catch (e: IOException) {
                playerState = PlayerState.DEFAULT
            }
        }
    }

    override fun setOnCompletionListener(listener: () -> Unit) {
        onCompletionListener = listener
    }

    override fun setOnPreparedListener(listener: () -> Unit) {
        onPreparedListener = listener
    }

    override fun startPlayer() {
        mediaPlayer.start()
        playerState = PlayerState.PLAYING
    }

    override fun pausePlayer() {
        mediaPlayer.pause()
        playerState = PlayerState.PAUSED
    }

    override fun releasePlayer() {
        mediaPlayer.release()
        playerState = PlayerState.DEFAULT
    }

    override fun getCurrentPosition(): Int {
        return mediaPlayer.currentPosition
    }

    override fun getPlayerState(): PlayerState {
        return playerState
    }
}