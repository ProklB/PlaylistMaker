package com.hfad.playlistmaker.search.domain.repository

import com.hfad.playlistmaker.search.domain.models.Track

interface SearchHistoryRepository {
    fun addTrackToHistory(track: Track)
    fun getSearchHistory(): List<Track>
    fun clearSearchHistory()
}