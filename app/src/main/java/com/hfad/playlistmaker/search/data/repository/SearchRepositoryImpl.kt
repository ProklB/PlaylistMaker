package com.hfad.playlistmaker.search.data.repository

import com.hfad.playlistmaker.search.data.network.NetworkClient
import com.hfad.playlistmaker.search.domain.models.Track
import com.hfad.playlistmaker.search.domain.repository.SearchRepository

class SearchRepositoryImpl(
    private val networkClient: NetworkClient
) : SearchRepository {
    override fun searchTracks(query: String): List<Track> {
        return networkClient.searchTracks(query).map { trackDto ->
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
                previewUrl = trackDto.previewUrl
            )
        }
    }
}