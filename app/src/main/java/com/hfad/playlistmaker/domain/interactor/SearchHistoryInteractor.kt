package com.hfad.playlistmaker.domain.interactor

import com.hfad.playlistmaker.domain.models.Track

interface SearchHistoryInteractor {
    fun addTrackToHistory(track: Track)
    fun getSearchHistory(): List<Track>
    fun clearSearchHistory()
}