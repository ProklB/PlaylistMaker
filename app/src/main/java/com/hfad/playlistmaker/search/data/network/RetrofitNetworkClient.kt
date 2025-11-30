package com.hfad.playlistmaker.search.data.network

import com.hfad.playlistmaker.search.data.dto.TrackDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class RetrofitNetworkClient(
    private val itunesApi: ItunesApi
) : NetworkClient {

    override fun searchTracks(query: String): Flow<List<TrackDto>> = flow {
        try {
            val response = itunesApi.search(query)
            emit(response.results ?: emptyList())
        } catch (e: Exception) {
            emit(emptyList())
        }
    }
}