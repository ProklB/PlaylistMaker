package com.hfad.playlistmaker.data.repositiry

import com.hfad.playlistmaker.data.network.RetrofitNetworkClient
import com.hfad.playlistmaker.domain.models.Track
import com.hfad.playlistmaker.domain.repository.SearchRepository

class SearchRepositoryImpl(
    private val networkClient: RetrofitNetworkClient
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