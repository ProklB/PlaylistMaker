package com.hfad.playlistmaker.search.domain.repository

import com.hfad.playlistmaker.search.domain.models.Track

interface SearchRepository {
    fun searchTracks(query: String): List<Track>
}