package com.hfad.playlistmaker.library.domain.repository

import com.hfad.playlistmaker.search.domain.models.Track
import kotlinx.coroutines.flow.Flow

interface FavoriteTracksRepository {
    suspend fun addTrackToFavorites(track: Track)
    suspend fun removeTrackFromFavorites(track: Track)
    fun getAllFavoriteTracks(): Flow<List<Track>>
}