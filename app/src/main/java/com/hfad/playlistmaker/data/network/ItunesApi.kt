package com.hfad.playlistmaker.data.network

import com.hfad.playlistmaker.data.dto.ItunesResponseDto
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ItunesApi {
    @GET("/search?entity=song")
    fun search(@Query("term") text: String): Call<ItunesResponseDto>
}