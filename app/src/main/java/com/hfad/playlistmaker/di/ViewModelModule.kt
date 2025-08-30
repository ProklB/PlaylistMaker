package com.hfad.playlistmaker.di

import com.hfad.playlistmaker.player.ui.MediaViewModel
import com.hfad.playlistmaker.search.ui.viewmodel.SearchViewModel
import com.hfad.playlistmaker.settings.ui.SettingsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {

    viewModel {
        SearchViewModel(get(), get())
    }

    viewModel {
        MediaViewModel(get())
    }

    viewModel {
        SettingsViewModel(get())
    }
}