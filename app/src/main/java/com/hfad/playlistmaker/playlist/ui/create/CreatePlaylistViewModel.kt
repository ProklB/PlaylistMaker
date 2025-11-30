package com.hfad.playlistmaker.playlist.ui.create

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hfad.playlistmaker.playlist.domain.interactor.PlaylistInteractor
import com.hfad.playlistmaker.playlist.domain.models.Playlist
import kotlinx.coroutines.launch

class CreatePlaylistViewModel(
    private val playlistInteractor: PlaylistInteractor
) : ViewModel() {

    private val _uiState = MutableLiveData<CreatePlaylistState>()
    val uiState: LiveData<CreatePlaylistState> = _uiState

    private var currentName = ""
    private var currentDescription = ""
    private var currentCoverPath: String? = null

    private var hasUnsavedChanges = false

    init {
        updateUiState()
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

    fun createPlaylist(onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val playlist = Playlist(
                    name = currentName,
                    description = currentDescription.ifEmpty { null },
                    coverPath = currentCoverPath,
                    trackIds = emptyList(),
                    trackCount = 0
                )
                playlistInteractor.createPlaylist(playlist)
                hasUnsavedChanges = false
                onComplete(true)
            } catch (e: Exception) {
                e.printStackTrace()
                onComplete(false)
            }
        }
    }

    fun hasUnsavedChanges(): Boolean {
        return hasUnsavedChanges
    }

    private fun updateUiState() {
        val isCreateButtonEnabled = currentName.isNotEmpty()
        _uiState.value = CreatePlaylistState(
            name = currentName,
            description = currentDescription,
            coverPath = currentCoverPath,
            isCreateButtonEnabled = isCreateButtonEnabled
        )
    }
}

data class CreatePlaylistState(
    val name: String,
    val description: String,
    val coverPath: String?,
    val isCreateButtonEnabled: Boolean
)