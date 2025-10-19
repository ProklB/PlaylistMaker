package com.hfad.playlistmaker.search.data.network

import com.hfad.playlistmaker.search.data.dto.TrackDto
import kotlinx.coroutines.flow.Flow

interface NetworkClient {
    fun searchTracks(query: String): Flow<List<TrackDto>>
}
