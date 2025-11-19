package com.hfad.playlistmaker.data.db

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context

@Database(
    entities = [FavoriteTrackEntity::class, PlaylistEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun favoriteTracksDao(): FavoriteTracksDao
    abstract fun playlistsDao(): PlaylistsDao

    companion object {
        private const val DATABASE_NAME = "playlist_maker.db"

        fun getInstance(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                DATABASE_NAME
            ).build()
        }
    }
}