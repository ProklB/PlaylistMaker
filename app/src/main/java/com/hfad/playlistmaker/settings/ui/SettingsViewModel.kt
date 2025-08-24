package com.hfad.playlistmaker.settings.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hfad.playlistmaker.settings.domain.interactor.SettingsInteractor
import com.hfad.playlistmaker.settings.domain.models.Settings

class SettingsViewModel(
    private val settingsInteractor: SettingsInteractor
) : ViewModel() {

    private val _themeSwitchState = MutableLiveData<Boolean>()
    val themeSwitchState: LiveData<Boolean> = _themeSwitchState

    init {
        loadSettings()
    }

    private fun loadSettings() {
        _themeSwitchState.value = settingsInteractor.getThemeSettings().darkThemeEnabled
    }

    fun onThemeSwitchChanged(isChecked: Boolean) {
        _themeSwitchState.value = isChecked
        settingsInteractor.updateThemeSetting(Settings(isChecked))
    }
}