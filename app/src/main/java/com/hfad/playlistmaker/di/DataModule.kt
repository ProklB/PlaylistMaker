package com.hfad.playlistmaker.di

import android.content.Context
import android.content.SharedPreferences
import android.media.MediaPlayer
import com.google.gson.Gson
import com.hfad.playlistmaker.data.db.AppDatabase
import com.hfad.playlistmaker.library.data.repository.FavoriteTracksRepositoryImpl
import com.hfad.playlistmaker.library.domain.repository.FavoriteTracksRepository
import com.hfad.playlistmaker.player.data.repository.PlayerRepositoryImpl
import com.hfad.playlistmaker.player.domain.repository.PlayerRepository
import com.hfad.playlistmaker.playlist.data.repository.PlaylistRepositoryImpl
import com.hfad.playlistmaker.playlist.domain.repository.PlaylistRepository
import com.hfad.playlistmaker.search.data.network.ItunesApi
import com.hfad.playlistmaker.search.data.network.NetworkClient
import com.hfad.playlistmaker.search.data.network.RetrofitNetworkClient
import com.hfad.playlistmaker.search.data.repository.SearchHistoryRepositoryImpl
import com.hfad.playlistmaker.search.data.repository.SearchRepositoryImpl
import com.hfad.playlistmaker.search.domain.repository.SearchHistoryRepository
import com.hfad.playlistmaker.search.domain.repository.SearchRepository
import com.hfad.playlistmaker.settings.data.repository.SettingsRepositoryImpl
import com.hfad.playlistmaker.settings.domain.repository.SettingsRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val dataModule = module {

    single<SharedPreferences> {
        androidContext().getSharedPreferences(
            "app_preferences",
            Context.MODE_PRIVATE
        )
    }

    factory { Gson() }

    single<ItunesApi> {
        Retrofit.Builder()
            .baseUrl("https://itunes.apple.com")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ItunesApi::class.java)
    }

    single<NetworkClient> {
        RetrofitNetworkClient(get())
    }

    factory<SearchRepository> {
        SearchRepositoryImpl(get(), get())
    }

    factory<SearchHistoryRepository> {
        SearchHistoryRepositoryImpl(get(), get(), get())
    }

    single<SettingsRepository> {
        SettingsRepositoryImpl(get())
    }

    factory { MediaPlayer() }

    factory<PlayerRepository> {
        PlayerRepositoryImpl(get())
    }

    single<AppDatabase> {
        AppDatabase.getInstance(androidContext())
    }

    single {
        get<AppDatabase>().favoriteTracksDao()
    }

    factory<FavoriteTracksRepository> {
        FavoriteTracksRepositoryImpl(get())
    }

    single {
        get<AppDatabase>().playlistsDao()
    }

    factory<PlaylistRepository> {
        PlaylistRepositoryImpl(get(), get())
    }
}