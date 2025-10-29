package com.hfad.playlistmaker.search.domain.interactor

import com.hfad.playlistmaker.search.domain.models.Track
import com.hfad.playlistmaker.search.domain.repository.SearchHistoryRepository

class SearchHistoryInteractorImpl(
    private val repository: SearchHistoryRepository
) : SearchHistoryInteractor {

    override fun addTrackToHistory(track: Track) {
        repository.addTrackToHistory(track)
    }

    override suspend fun getSearchHistory(): List<Track> {
        return repository.getSearchHistory()
    }

    override fun clearSearchHistory() {
        repository.clearSearchHistory()
    }
}