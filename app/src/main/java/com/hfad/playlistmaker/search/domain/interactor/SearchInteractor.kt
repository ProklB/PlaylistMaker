package com.hfad.playlistmaker.search.domain.interactor

import com.hfad.playlistmaker.search.domain.models.Track
import com.hfad.playlistmaker.search.domain.repository.SearchRepository
import java.util.concurrent.Executors

interface SearchInteractor {
    fun searchTracks(query: String, consumer: SearchConsumer)

    interface SearchConsumer {
        fun consume(foundTracks: List<Track>, error: String?)
    }
}

class SearchInteractorImpl(
    private val repository: SearchRepository
) : SearchInteractor {
    private val executor = Executors.newCachedThreadPool()

    override fun searchTracks(query: String, consumer: SearchInteractor.SearchConsumer) {
        executor.execute {
            try {
                val tracks = repository.searchTracks(query)
                consumer.consume(tracks, null)
            } catch (e: Exception) {
                consumer.consume(emptyList(), e.message)
            }
        }
    }
}