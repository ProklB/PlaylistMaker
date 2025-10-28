package com.hfad.playlistmaker.search.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hfad.playlistmaker.search.domain.interactor.SearchHistoryInteractor
import com.hfad.playlistmaker.search.domain.interactor.SearchInteractor
import com.hfad.playlistmaker.search.domain.models.Track
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

class SearchViewModel(
    private val searchInteractor: SearchInteractor,
    private val searchHistoryInteractor: SearchHistoryInteractor
) : ViewModel() {

    private val _searchState = MutableLiveData<SearchState>()
    val searchState: LiveData<SearchState> = _searchState

    private val _historyState = MutableLiveData<List<Track>>()
    val historyState: LiveData<List<Track>> = _historyState

    private var searchJob: Job? = null

    init {
        loadSearchHistory()
    }

    fun addTrackToHistory(track: Track) {
        searchHistoryInteractor.addTrackToHistory(track)
        loadSearchHistory()
    }

    fun clearSearchHistory() {
        searchHistoryInteractor.clearSearchHistory()
        loadSearchHistory()
    }

    fun loadSearchHistory() {
        val history = searchHistoryInteractor.getSearchHistory()
        _historyState.value = history
    }

    fun searchTracks(query: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_DELAY)

            searchInteractor.searchTracks(query)
                .onStart {
                    _searchState.value = SearchState.Loading
                }
                .catch { error ->
                    _searchState.value = SearchState.Error(error.message ?: "Unknown error")
                }
                .collect { tracks ->
                    if (tracks.isEmpty()) {
                        _searchState.value = SearchState.Empty
                    } else {
                        _searchState.value = SearchState.Content(tracks)
                    }
                }
        }
    }

    companion object {
        private const val SEARCH_DEBOUNCE_DELAY = 2000L
    }
}

sealed class SearchState {
    object Loading : SearchState()
    object Empty : SearchState()
    data class Error(val message: String) : SearchState()
    data class Content(val tracks: List<Track>) : SearchState()
}