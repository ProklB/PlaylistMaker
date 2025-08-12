package com.hfad.playlistmaker.domain.repository

import com.hfad.playlistmaker.domain.models.Track

interface SearchHistoryRepository {
    fun addTrackToHistory(track: Track)
    fun getSearchHistory(): List<Track>
    fun clearSearchHistory()
}