package com.hfad.playlistmaker

data class ItunesResponse(
    val resultCount: Int,
    val results: List<Track>
)