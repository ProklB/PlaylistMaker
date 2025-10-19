package com.hfad.playlistmaker.search.domain.repository

import com.hfad.playlistmaker.search.domain.models.Track
import kotlinx.coroutines.flow.Flow

interface SearchRepository {
    fun searchTracks(query: String): Flow<List<Track>>
}