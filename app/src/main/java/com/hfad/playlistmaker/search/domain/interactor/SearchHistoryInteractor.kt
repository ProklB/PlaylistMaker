package com.hfad.playlistmaker.search.domain.interactor

import com.hfad.playlistmaker.search.domain.models.Track

interface SearchHistoryInteractor {
    fun addTrackToHistory(track: Track)
    fun getSearchHistory(): List<Track>
    fun clearSearchHistory()
}