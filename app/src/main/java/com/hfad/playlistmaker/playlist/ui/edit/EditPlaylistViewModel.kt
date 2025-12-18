package com.hfad.playlistmaker.playlist.ui.edit

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hfad.playlistmaker.playlist.domain.interactor.PlaylistInteractor
import com.hfad.playlistmaker.playlist.domain.models.Playlist
import kotlinx.coroutines.launch

class EditPlaylistViewModel(
    private val playlistInteractor: PlaylistInteractor
) : ViewModel() {

    private val _uiState = MutableLiveData<EditPlaylistState>()
    val uiState: LiveData<EditPlaylistState> = _uiState

    private val _playlistLoaded = MutableLiveData<Boolean>()
    val playlistLoaded: LiveData<Boolean> = _playlistLoaded

    private var currentPlaylist: Playlist? = null
    private var originalName = ""
    private var originalDescription = ""
    private var originalCoverPath: String? = null
    private var currentName = ""
    private var currentDescription = ""
    private var currentCoverPath: String? = null
    private var hasUnsavedChanges = false

    fun initialize(playlist: Playlist) {
        currentPlaylist = playlist
        originalName = playlist.name
        originalDescription = playlist.description ?: ""
        originalCoverPath = playlist.coverPath

        currentName = originalName
        currentDescription = originalDescription
        currentCoverPath = originalCoverPath

        updateUiState()
    }

    fun loadPlaylist(playlistId: Long) {
        viewModelScope.launch {
            try {
                val playlist = playlistInteractor.getPlaylistById(playlistId)
                if (playlist != null) {
                    initialize(playlist)
                    _playlistLoaded.postValue(true)
                } else {
                    _playlistLoaded.postValue(false)
                }
            } catch (e: Exception) {
                _playlistLoaded.postValue(false)
            }
        }
    }

    fun onNameChanged(name: String) {
        currentName = name
        hasUnsavedChanges = true
        updateUiState()
    }

    fun onDescriptionChanged(description: String) {
        currentDescription = description
        hasUnsavedChanges = true
        updateUiState()
    }

    fun onCoverSelected(coverPath: String) {
        currentCoverPath = coverPath
        hasUnsavedChanges = true
        updateUiState()
    }

    fun savePlaylist(onComplete: (Boolean) -> Unit) {
        val playlist = currentPlaylist ?: run {
            onComplete(false)
            return
        }

        viewModelScope.launch {
            try {
                val updatedPlaylist = playlist.copy(
                    name = currentName,
                    description = currentDescription.ifEmpty { null },
                    coverPath = currentCoverPath
                )

                playlistInteractor.createPlaylist(updatedPlaylist)
                hasUnsavedChanges = false
                onComplete(true)
            } catch (e: Exception) {
                e.printStackTrace()
                onComplete(false)
            }
        }
    }

    private fun updateUiState() {
        val isSaveButtonEnabled = currentName.isNotEmpty()
        _uiState.value = EditPlaylistState(
            name = currentName,
            description = currentDescription,
            coverPath = currentCoverPath,
            isSaveButtonEnabled = isSaveButtonEnabled
        )
    }
}

data class EditPlaylistState(
    val name: String,
    val description: String,
    val coverPath: String?,
    val isSaveButtonEnabled: Boolean
)