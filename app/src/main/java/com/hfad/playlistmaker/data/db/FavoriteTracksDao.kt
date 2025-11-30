package com.hfad.playlistmaker.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteTracksDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addTrackToFavorites(track: FavoriteTrackEntity)

    @Delete
    suspend fun removeTrackFromFavorites(track: FavoriteTrackEntity)

    @Query("SELECT * FROM favorite_tracks ORDER BY id DESC")
    fun getAllFavoriteTracks(): Flow<List<FavoriteTrackEntity>>

    @Query("SELECT trackId FROM favorite_tracks")
    suspend fun getAllFavoriteTrackIds(): List<Int>

    @Query("SELECT * FROM favorite_tracks WHERE trackId = :trackId")
    suspend fun getTrackByTrackId(trackId: Int): FavoriteTrackEntity?
}