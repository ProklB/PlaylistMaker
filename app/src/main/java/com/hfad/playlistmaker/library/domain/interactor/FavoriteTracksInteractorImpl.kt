package com.hfad.playlistmaker.library.domain.interactor

import com.hfad.playlistmaker.library.domain.repository.FavoriteTracksRepository
import com.hfad.playlistmaker.search.domain.models.Track
import kotlinx.coroutines.flow.Flow


class FavoriteTracksInteractorImpl(
    private val repository: FavoriteTracksRepository
) : FavoriteTracksInteractor {

    override suspend fun addTrackToFavorites(track: Track) {
        repository.addTrackToFavorites(track)
    }

    override suspend fun removeTrackFromFavorites(track: Track) {
        repository.removeTrackFromFavorites(track)
    }

    override fun getAllFavoriteTracks(): Flow<List<Track>> {
        return repository.getAllFavoriteTracks()
    }
}