package com.hfad.playlistmaker.creator

import android.app.Application
import android.content.Context
import com.hfad.playlistmaker.player.data.repository.PlayerRepositoryImpl
import com.hfad.playlistmaker.player.domain.interactor.PlayerInteractor
import com.hfad.playlistmaker.player.domain.interactor.PlayerInteractorImpl
import com.hfad.playlistmaker.search.data.network.RetrofitNetworkClient
import com.hfad.playlistmaker.search.data.repository.SearchRepositoryImpl
import com.hfad.playlistmaker.settings.data.repository.SettingsRepositoryImpl
import com.hfad.playlistmaker.search.data.repository.SearchHistoryRepositoryImpl
import com.hfad.playlistmaker.search.domain.interactor.SearchHistoryInteractor
import com.hfad.playlistmaker.search.domain.interactor.SearchHistoryInteractorImpl
import com.hfad.playlistmaker.search.domain.interactor.SearchInteractor
import com.hfad.playlistmaker.search.domain.interactor.SearchInteractorImpl
import com.hfad.playlistmaker.settings.domain.interactor.SettingsInteractor
import com.hfad.playlistmaker.settings.domain.interactor.SettingsInteractorImpl
import com.hfad.playlistmaker.search.domain.repository.SearchHistoryRepository
import com.hfad.playlistmaker.search.domain.repository.SearchRepository
import com.hfad.playlistmaker.settings.domain.repository.SettingsRepository

object Creator {

    private fun getSearchRepository(): SearchRepository {
        return SearchRepositoryImpl(RetrofitNetworkClient())
    }

    fun provideSearchInteractor(): SearchInteractor {
        return SearchInteractorImpl(getSearchRepository())
    }

    private fun getSettingsRepository(context: Context): SettingsRepository {
        val sharedPreferences = context.getSharedPreferences(
            SettingsRepositoryImpl.PLAYLISTMAKER_PREFERENCES,
            Context.MODE_PRIVATE
        )
        return SettingsRepositoryImpl(sharedPreferences)
    }

    fun provideSettingsInteractor(app: Application): SettingsInteractor {
        return SettingsInteractorImpl(
            getSettingsRepository(app)
        )
    }

    private fun getSearchHistoryRepository(context: Context): SearchHistoryRepository {
        val sharedPreferences = context.getSharedPreferences(
            "search_history_prefs",
            Context.MODE_PRIVATE
        )
        return SearchHistoryRepositoryImpl(sharedPreferences)
    }

    fun provideSearchHistoryInteractor(context: Context): SearchHistoryInteractor {
        return SearchHistoryInteractorImpl(getSearchHistoryRepository(context))
    }

    fun providePlayerInteractor(): PlayerInteractor {
        return PlayerInteractorImpl(PlayerRepositoryImpl())
    }
}