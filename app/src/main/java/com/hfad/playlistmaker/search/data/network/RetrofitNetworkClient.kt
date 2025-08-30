package com.hfad.playlistmaker.search.data.network

import com.hfad.playlistmaker.search.data.dto.TrackDto

class RetrofitNetworkClient(
    private val itunesApi: ItunesApi
) : NetworkClient {

    override fun searchTracks(query: String): List<TrackDto> {
        val response = itunesApi.search(query).execute()
        return if (response.isSuccessful) {
            response.body()?.results ?: emptyList()
        } else {
            emptyList()
        }
    }
}