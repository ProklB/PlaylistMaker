package com.hfad.playlistmaker.data.dto

class ItunesResponseDto(
    val resultCount: Int,
    val results: List<TrackDto>
) : Response()