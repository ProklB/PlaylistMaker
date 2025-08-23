package com.hfad.playlistmaker.search.data.network

import com.hfad.playlistmaker.search.data.dto.TrackDto

interface NetworkClient {
    fun searchTracks(query: String): List<TrackDto>
}
