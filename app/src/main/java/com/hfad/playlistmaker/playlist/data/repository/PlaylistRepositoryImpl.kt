package com.hfad.playlistmaker.playlist.data.repository

import com.google.gson.Gson
import com.hfad.playlistmaker.data.db.PlaylistEntity
import com.hfad.playlistmaker.data.db.PlaylistTrackEntity
import com.hfad.playlistmaker.data.db.PlaylistTracksDao
import com.hfad.playlistmaker.data.db.PlaylistsDao
import com.hfad.playlistmaker.playlist.domain.models.Playlist
import com.hfad.playlistmaker.playlist.domain.repository.PlaylistRepository
import com.hfad.playlistmaker.search.domain.models.Track
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class PlaylistRepositoryImpl(
    private val playlistsDao: PlaylistsDao,
    private val playlistTracksDao: PlaylistTracksDao,
    private val gson: Gson
) : PlaylistRepository {

    override suspend fun addTrackToPlaylist(track: Track, playlist: Playlist): Boolean {
        if (playlist.trackIds.contains(track.trackId)) {
            return false
        }

        playlistTracksDao.insertTrack(track.toPlaylistTrackEntity())

        val updatedTrackIds = playlist.trackIds.toMutableList().apply {
            add(track.trackId)
        }

        val updatedPlaylist = playlist.copy(
            trackIds = updatedTrackIds,
            trackCount = updatedTrackIds.size
        )

        playlistsDao.insertPlaylist(updatedPlaylist.toEntity())
        return true
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

    override suspend fun getPlaylistTracks(playlist: Playlist): List<Track> {
        return if (playlist.trackIds.isNotEmpty()) {
            val tracks = playlistTracksDao.getTracksByIds(playlist.trackIds)
            val trackMap = tracks.associateBy { it.trackId }
            playlist.trackIds.mapNotNull { trackId ->
                trackMap[trackId]?.toTrack()
            }.reversed()
        } else {
            emptyList()
        }
    }

    override suspend fun getPlaylistById(playlistId: Long): Playlist? {
        val entity = playlistsDao.getPlaylistById(playlistId)
        return entity?.toPlaylist()
    }

    override suspend fun removeTrackFromPlaylist(trackId: Int, playlist: Playlist) {
        val updatedTrackIds = playlist.trackIds.toMutableList().apply {
            remove(trackId)
        }

        val updatedPlaylist = playlist.copy(
            trackIds = updatedTrackIds,
            trackCount = updatedTrackIds.size
        )

        playlistsDao.insertPlaylist(updatedPlaylist.toEntity())
        cleanupOrphanedTracks(trackId)
    }

    override suspend fun deletePlaylist(playlistId: Long) {
        val playlistEntity = playlistsDao.getPlaylistById(playlistId)
        playlistsDao.deletePlaylist(playlistId)
        playlistEntity?.let { entity ->
            val trackIds = gson.fromJson(entity.trackIds, Array<Int>::class.java).toList()
            for (trackId in trackIds) {
                cleanupOrphanedTracks(trackId)
            }
        }
    }

    private suspend fun cleanupOrphanedTracks(trackId: Int) {
        val allPlaylists = playlistsDao.getAllPlaylists().first()
        val isTrackUsed = allPlaylists.any { playlistEntity ->
            val trackIds = gson.fromJson(playlistEntity.trackIds, Array<Int>::class.java).toList()
            trackIds.contains(trackId)
        }

        if (!isTrackUsed) {
            playlistTracksDao.deleteTrack(trackId)
        }
    }

    private fun Track.toPlaylistTrackEntity(): PlaylistTrackEntity {
        return PlaylistTrackEntity(
            trackId = trackId,
            trackName = trackName,
            artistName = artistName,
            trackTimeMillis = trackTimeMillis,
            artworkUrl100 = artworkUrl100,
            collectionName = collectionName,
            releaseDate = releaseDate,
            primaryGenreName = primaryGenreName,
            country = country,
            previewUrl = previewUrl
        )
    }

    private fun PlaylistTrackEntity.toTrack(): Track {
        return Track(
            trackId = trackId,
            trackName = trackName,
            artistName = artistName,
            trackTimeMillis = trackTimeMillis,
            artworkUrl100 = artworkUrl100,
            collectionName = collectionName,
            releaseDate = releaseDate,
            primaryGenreName = primaryGenreName,
            country = country,
            previewUrl = previewUrl,
            isFavorite = false
        )
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

    override suspend fun getShareText(playlist: Playlist): String {
        val tracks = getPlaylistTracks(playlist)
        return formatShareText(playlist, tracks)
    }

    private fun formatShareText(playlist: Playlist, tracks: List<Track>): String {
        if (tracks.isEmpty()) {
            return "${playlist.name}\nПлейлист пуст"
        }

        val stringBuilder = StringBuilder()

        stringBuilder.append(playlist.name)
        stringBuilder.append("\n")

        playlist.description?.let { description ->
            stringBuilder.append(description)
            stringBuilder.append("\n")
        }

        val trackCountText = when (playlist.trackCount) {
            1 -> "[01] трек"
            in 2..4 -> "[${String.format("%02d", playlist.trackCount)}] трека"
            else -> "[${String.format("%02d", playlist.trackCount)}] треков"
        }
        stringBuilder.append(trackCountText)
        stringBuilder.append("\n\n")

        tracks.forEachIndexed { index, track ->
            val trackNumber = index + 1
            val duration = formatTrackDuration(track.trackTimeMillis)

            stringBuilder.append("${String.format("%02d", trackNumber)}. ${track.artistName} - ${track.trackName} ($duration)")
            stringBuilder.append("\n")
        }

        return stringBuilder.toString()
    }

    private fun formatTrackDuration(millis: Long): String {
        val seconds = millis / 1000
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format("%02d:%02d", minutes, remainingSeconds)
    }
}