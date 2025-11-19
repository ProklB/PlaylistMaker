package com.hfad.playlistmaker.playlist.data.repository

import com.google.gson.Gson
import com.hfad.playlistmaker.data.db.PlaylistEntity
import com.hfad.playlistmaker.data.db.PlaylistsDao
import com.hfad.playlistmaker.playlist.domain.models.Playlist
import com.hfad.playlistmaker.playlist.domain.repository.PlaylistRepository
import com.hfad.playlistmaker.search.domain.models.Track
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PlaylistRepositoryImpl(
    private val playlistsDao: PlaylistsDao,
    private val gson: Gson
) : PlaylistRepository {

    override suspend fun addTrackToPlaylist(track: Track, playlist: Playlist) {
        val updatedTrackIds = playlist.trackIds.toMutableList().apply {
            if (!contains(track.trackId)) {
                add(track.trackId)
            }
        }

        val updatedPlaylist = playlist.copy(
            trackIds = updatedTrackIds,
            trackCount = updatedTrackIds.size
        )

        playlistsDao.insertPlaylist(updatedPlaylist.toEntity())
    }

    override suspend fun isTrackInPlaylist(trackId: Int, playlist: Playlist): Boolean {
        return playlist.trackIds.contains(trackId)
    }

    override suspend fun createPlaylist(playlist: Playlist) {
        val entity = playlist.toEntity()
        playlistsDao.insertPlaylist(entity)
    }

    override fun getAllPlaylists(): Flow<List<Playlist>> {
        return playlistsDao.getAllPlaylists().map { entities ->
            entities.map { it.toPlaylist() }
        }
    }

    private fun Playlist.toEntity(): PlaylistEntity {
        return PlaylistEntity(
            id = id,
            name = name,
            description = description,
            coverPath = coverPath,
            trackIds = gson.toJson(trackIds),
            trackCount = trackCount
        )
    }

    private fun PlaylistEntity.toPlaylist(): Playlist {
        val trackIdsList = gson.fromJson(trackIds, Array<Int>::class.java).toList()
        return Playlist(
            id = id,
            name = name,
            description = description,
            coverPath = coverPath,
            trackIds = trackIdsList,
            trackCount = trackCount
        )
    }

}