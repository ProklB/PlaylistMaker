package com.hfad.playlistmaker.domain.repository

import com.hfad.playlistmaker.domain.models.Track

interface SearchRepository {
    fun searchTracks(query: String): List<Track>
}