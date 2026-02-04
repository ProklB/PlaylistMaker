package com.hfad.playlistmaker.settings.ui

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.graphics.drawable.DrawableCompat.applyTheme
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
        applyTheme(isChecked)
    }

    private fun applyTheme(isDarkTheme: Boolean) {
        AppCompatDelegate.setDefaultNightMode(
            if (isDarkTheme) {
                AppCompatDelegate.MODE_NIGHT_YES
            } else {
                AppCompatDelegate.MODE_NIGHT_NO
            }
        )
    }
}