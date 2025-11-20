package com.hfad.playlistmaker.player.ui.model

sealed class AddToPlaylistStatus {
    data class Success(val playlistName: String) : AddToPlaylistStatus()
    data class TrackAlreadyExists(val playlistName: String) : AddToPlaylistStatus()
    object Error : AddToPlaylistStatus()
}
