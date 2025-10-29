package com.hfad.playlistmaker.search.data.repository

import com.hfad.playlistmaker.data.db.FavoriteTracksDao
import com.hfad.playlistmaker.search.data.network.NetworkClient
import com.hfad.playlistmaker.search.domain.models.Track
import com.hfad.playlistmaker.search.domain.repository.SearchRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SearchRepositoryImpl(
    private val networkClient: NetworkClient,
    private val favoriteTracksDao: FavoriteTracksDao
) : SearchRepository {
    override fun searchTracks(query: String): Flow<List<Track>> {
        return networkClient.searchTracks(query).map { trackDtos ->
            val favoriteTrackIds = favoriteTracksDao.getAllFavoriteTrackIds()
            trackDtos.map { trackDto ->
                Track(
                    trackId = trackDto.trackId,
                    trackName = trackDto.trackName,
                    artistName = trackDto.artistName,
                    trackTimeMillis = trackDto.trackTimeMillis,
                    artworkUrl100 = trackDto.artworkUrl100,
                    collectionName = trackDto.collectionName,
                    releaseDate = trackDto.releaseDate,
                    primaryGenreName = trackDto.primaryGenreName,
                    country = trackDto.country,
                    previewUrl = trackDto.previewUrl,
                    isFavorite = favoriteTrackIds.contains(trackDto.trackId)
                )
            }
        }
    }
}