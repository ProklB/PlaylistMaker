package com.hfad.playlistmaker.di

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.hfad.playlistmaker.player.data.repository.PlayerRepositoryImpl
import com.hfad.playlistmaker.player.domain.repository.PlayerRepository
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

    single<SearchRepository> {
        SearchRepositoryImpl(get())
    }

    single<SearchHistoryRepository> {
        SearchHistoryRepositoryImpl(get(), get())
    }

    single<SettingsRepository> {
        SettingsRepositoryImpl(get())
    }

    single<PlayerRepository> {
        PlayerRepositoryImpl()
    }
}