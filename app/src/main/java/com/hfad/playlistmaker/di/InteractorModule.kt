package com.hfad.playlistmaker.di

import com.hfad.playlistmaker.player.domain.interactor.PlayerInteractor
import com.hfad.playlistmaker.player.domain.interactor.PlayerInteractorImpl
import com.hfad.playlistmaker.search.domain.interactor.SearchHistoryInteractor
import com.hfad.playlistmaker.search.domain.interactor.SearchHistoryInteractorImpl
import com.hfad.playlistmaker.search.domain.interactor.SearchInteractor
import com.hfad.playlistmaker.search.domain.interactor.SearchInteractorImpl
import com.hfad.playlistmaker.settings.domain.interactor.SettingsInteractor
import com.hfad.playlistmaker.settings.domain.interactor.SettingsInteractorImpl
import org.koin.dsl.module

val interactorModule = module {

    factory<SearchInteractor> {
        SearchInteractorImpl(get())
    }

    factory<SearchHistoryInteractor> {
        SearchHistoryInteractorImpl(get())
    }

    factory<PlayerInteractor> {
        PlayerInteractorImpl(get())
    }

    single<SettingsInteractor> {
        SettingsInteractorImpl(get())
    }
}