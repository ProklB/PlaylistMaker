package com.hfad.playlistmaker.search.domain.repository

import com.hfad.playlistmaker.search.domain.models.Track

interface SearchHistoryRepository {
    fun addTrackToHistory(track: Track)
    suspend fun getSearchHistory(): List<Track>
    fun clearSearchHistory()
}