package com.hfad.playlistmaker.search.domain.interactor

import com.hfad.playlistmaker.search.domain.models.Track

interface SearchHistoryInteractor {
    fun addTrackToHistory(track: Track)
    suspend fun getSearchHistory(): List<Track>
    fun clearSearchHistory()
}