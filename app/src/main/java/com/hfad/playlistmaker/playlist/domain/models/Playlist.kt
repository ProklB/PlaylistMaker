package com.hfad.playlistmaker.playlist.domain.models

data class Playlist(
    val id: Long = 0,
    val name: String,
    val description: String?,
    val coverPath: String?,
    val trackIds: List<Int>,
    val trackCount: Int
)