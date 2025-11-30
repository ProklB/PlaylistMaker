package com.hfad.playlistmaker.search.data.network

import com.hfad.playlistmaker.search.data.dto.ItunesResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

interface ItunesApi {
    @GET("/search?entity=song")
    suspend fun search(@Query("term") text: String): ItunesResponseDto
}