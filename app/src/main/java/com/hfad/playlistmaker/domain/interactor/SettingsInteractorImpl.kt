package com.hfad.playlistmaker.domain.interactor

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.hfad.playlistmaker.domain.models.Settings
import com.hfad.playlistmaker.domain.repository.SettingsRepository

class SettingsInteractorImpl(
    private val settingsRepository: SettingsRepository,
    private val app: Application
) : SettingsInteractor {

    override fun getThemeSettings(): Settings {
        return settingsRepository.getSettings()
    }

    override fun updateThemeSetting(settings: Settings) {
        settingsRepository.saveSettings(settings)
        applyTheme(settings)
    }

    override fun applyTheme(settings: Settings) {
        AppCompatDelegate.setDefaultNightMode(
            if (settings.darkThemeEnabled) {
                AppCompatDelegate.MODE_NIGHT_YES
            } else {
                AppCompatDelegate.MODE_NIGHT_NO
            }
        )
    }
}