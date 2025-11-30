package com.hfad.playlistmaker.library.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hfad.playlistmaker.library.domain.interactor.FavoriteTracksInteractor
import com.hfad.playlistmaker.search.domain.models.Track
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class FavoritesViewModel(
    private val favoriteTracksInteractor: FavoriteTracksInteractor
) : ViewModel() {

    private val _favoritesState = MutableLiveData<FavoritesState>()
    val favoritesState: LiveData<FavoritesState> = _favoritesState

    init {
        loadFavoriteTracks()
    }

    fun loadFavoriteTracks() {
        viewModelScope.launch {
            favoriteTracksInteractor.getAllFavoriteTracks().collectLatest { tracks ->
                if (tracks.isEmpty()) {
                    _favoritesState.value = FavoritesState.Empty
                } else {
                    _favoritesState.value = FavoritesState.Content(tracks)
                }
            }
        }
    }
}

sealed class FavoritesState {
    object Empty : FavoritesState()
    data class Content(val tracks: List<Track>) : FavoritesState()
}