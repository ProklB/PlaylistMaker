package com.hfad.playlistmaker

import android.app.Application
import android.content.Context
import com.hfad.playlistmaker.data.network.RetrofitNetworkClient
import com.hfad.playlistmaker.data.repositiry.SearchRepositoryImpl
import com.hfad.playlistmaker.data.repositiry.SettingsRepositoryImpl
import com.hfad.playlistmaker.data.repository.SearchHistoryRepositoryImpl
import com.hfad.playlistmaker.domain.interactor.SearchHistoryInteractor
import com.hfad.playlistmaker.domain.interactor.SearchHistoryInteractorImpl
import com.hfad.playlistmaker.domain.interactor.SearchInteractor
import com.hfad.playlistmaker.domain.interactor.SearchInteractorImpl
import com.hfad.playlistmaker.domain.interactor.SettingsInteractor
import com.hfad.playlistmaker.domain.interactor.SettingsInteractorImpl
import com.hfad.playlistmaker.domain.repository.SearchHistoryRepository
import com.hfad.playlistmaker.domain.repository.SearchRepository
import com.hfad.playlistmaker.domain.repository.SettingsRepository

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
            getSettingsRepository(app),
            app
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
}