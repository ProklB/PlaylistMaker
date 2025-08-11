package com.hfad.playlistmaker.domain.interactor

import com.hfad.playlistmaker.domain.models.Track
import com.hfad.playlistmaker.domain.repository.SearchHistoryRepository

class SearchHistoryInteractorImpl(
    private val repository: SearchHistoryRepository
) : SearchHistoryInteractor {

    override fun addTrackToHistory(track: Track) {
        repository.addTrackToHistory(track)
    }

    override fun getSearchHistory(): List<Track> {
        return repository.getSearchHistory()
    }

    override fun clearSearchHistory() {
        repository.clearSearchHistory()
    }
}