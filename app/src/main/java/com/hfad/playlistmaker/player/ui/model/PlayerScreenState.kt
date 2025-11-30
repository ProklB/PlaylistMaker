package com.hfad.playlistmaker.player.ui.model

import com.hfad.playlistmaker.player.domain.models.PlayerState

data class PlayerScreenState(
    val playerState: PlayerState = PlayerState.DEFAULT,
    val currentPosition: Int = 0,
    val isFavorite: Boolean = false
)
