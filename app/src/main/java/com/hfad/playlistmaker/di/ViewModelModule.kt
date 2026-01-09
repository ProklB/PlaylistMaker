package com.hfad.playlistmaker.di

import com.hfad.playlistmaker.library.ui.viewmodel.FavoritesViewModel
import com.hfad.playlistmaker.library.ui.viewmodel.PlaylistsViewModel
import com.hfad.playlistmaker.player.ui.MediaViewModel
import com.hfad.playlistmaker.playlist.ui.create.CreatePlaylistViewModel
import com.hfad.playlistmaker.playlist.ui.details.viewmodel.PlaylistDetailsViewModel
import com.hfad.playlistmaker.playlist.ui.edit.EditPlaylistViewModel
import com.hfad.playlistmaker.search.ui.viewmodel.SearchViewModel
import com.hfad.playlistmaker.settings.ui.SettingsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {

    viewModel {
        SearchViewModel(get(), get())
    }

    viewModel {
        MediaViewModel(get(), get())
    }

    viewModel {
        SettingsViewModel(get())
    }
    viewModel {
        PlaylistsViewModel(get())
    }
    viewModel {
        FavoritesViewModel(get())
    }

    viewModel {
        CreatePlaylistViewModel(get())
    }

    viewModel {
        PlaylistDetailsViewModel(get())
    }

    viewModel {
        EditPlaylistViewModel(get())
    }

}