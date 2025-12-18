package com.hfad.playlistmaker.library.data.repository

import com.hfad.playlistmaker.library.domain.repository.FavoriteTracksRepository
import com.hfad.playlistmaker.search.domain.models.Track
import com.hfad.playlistmaker.data.db.FavoriteTracksDao
import com.hfad.playlistmaker.data.db.FavoriteTrackEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FavoriteTracksRepositoryImpl(
    private val favoriteTracksDao: FavoriteTracksDao
) : FavoriteTracksRepository {

    override suspend fun addTrackToFavorites(track: Track) {
        val existingTrack = favoriteTracksDao.getTrackByTrackId(track.trackId)
        if (existingTrack == null) {
            favoriteTracksDao.addTrackToFavorites(track.toEntity())
        }
    }

    override suspend fun removeTrackFromFavorites(track: Track) {
        val entity = favoriteTracksDao.getTrackByTrackId(track.trackId)
        entity?.let { favoriteTracksDao.removeTrackFromFavorites(it) }
    }

    override fun getAllFavoriteTracks(): Flow<List<Track>> {
        return favoriteTracksDao.getAllFavoriteTracks().map { entities ->
            entities.map { it.toTrack() }
        }
    }

    override suspend fun isTrackInFavorites(trackId: Int): Boolean {
        return favoriteTracksDao.getTrackByTrackId(trackId) != null
    }

    private fun Track.toEntity(): FavoriteTrackEntity {
        return FavoriteTrackEntity(
            trackId = trackId,
            trackName = trackName,
            artistName = artistName,
            trackTimeMillis = trackTimeMillis,
            artworkUrl100 = artworkUrl100,
            collectionName = collectionName,
            releaseDate = releaseDate,
            primaryGenreName = primaryGenreName,
            country = country,
            previewUrl = previewUrl
        )
    }

    private fun FavoriteTrackEntity.toTrack(): Track {
        return Track(
            trackId = trackId,
            trackName = trackName,
            artistName = artistName,
            trackTimeMillis = trackTimeMillis,
            artworkUrl100 = artworkUrl100,
            collectionName = collectionName,
            releaseDate = releaseDate,
            primaryGenreName = primaryGenreName,
            country = country,
            previewUrl = previewUrl,
            isFavorite = true
        )
    }
}