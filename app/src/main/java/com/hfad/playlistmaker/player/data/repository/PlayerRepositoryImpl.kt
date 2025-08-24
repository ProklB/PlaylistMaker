package com.hfad.playlistmaker.player.data.repository

import android.media.MediaPlayer
import com.hfad.playlistmaker.player.domain.models.PlayerState
import com.hfad.playlistmaker.player.domain.repository.PlayerRepository
import java.io.IOException

class PlayerRepositoryImpl : PlayerRepository {

    private var mediaPlayer: MediaPlayer? = null
    private var playerState: PlayerState = PlayerState.DEFAULT
    private var onPreparedListener: (() -> Unit)? = null

    override fun preparePlayer(previewUrl: String) {
        mediaPlayer = MediaPlayer().apply {
            try {
                setDataSource(previewUrl)
                prepareAsync()
                setOnPreparedListener {
                    playerState = PlayerState.PREPARED
                    onPreparedListener?.invoke()
                }
                setOnCompletionListener {
                    playerState = PlayerState.PREPARED
                }
            } catch (e: IOException) {
                playerState = PlayerState.DEFAULT
            }
        }
    }

    override fun setOnPreparedListener(listener: () -> Unit) {
        onPreparedListener = listener
    }

    override fun startPlayer() {
        mediaPlayer?.start()
        playerState = PlayerState.PLAYING
    }

    override fun pausePlayer() {
        mediaPlayer?.pause()
        playerState = PlayerState.PAUSED
    }

    override fun releasePlayer() {
        mediaPlayer?.release()
        mediaPlayer = null
        playerState = PlayerState.DEFAULT
    }

    override fun getCurrentPosition(): Int {
        return mediaPlayer?.currentPosition ?: 0
    }

    override fun getPlayerState(): PlayerState {
        return playerState
    }
}