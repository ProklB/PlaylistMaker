package com.hfad.playlistmaker.search.data.dto

class ItunesResponseDto(
    val resultCount: Int,
    val results: List<TrackDto>
) : Response()