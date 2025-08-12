package com.hfad.playlistmaker.data.network

import com.hfad.playlistmaker.data.dto.TrackDto

interface NetworkClient {
    fun searchTracks(query: String): List<TrackDto>
}
