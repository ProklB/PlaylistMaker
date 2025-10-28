package com.hfad.playlistmaker.search.domain.interactor

import com.hfad.playlistmaker.search.domain.models.Track
import com.hfad.playlistmaker.search.domain.repository.SearchRepository
import kotlinx.coroutines.flow.Flow

interface SearchInteractor {
    fun searchTracks(query: String): Flow<List<Track>>
}

class SearchInteractorImpl(
    private val repository: SearchRepository
) : SearchInteractor {
    override fun searchTracks(query: String): Flow<List<Track>> {
        return repository.searchTracks(query)
    }
}