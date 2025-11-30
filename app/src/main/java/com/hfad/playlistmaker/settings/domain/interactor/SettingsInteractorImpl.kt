package com.hfad.playlistmaker.settings.domain.interactor

import com.hfad.playlistmaker.settings.domain.models.Settings
import com.hfad.playlistmaker.settings.domain.repository.SettingsRepository

class SettingsInteractorImpl(
    private val settingsRepository: SettingsRepository
) : SettingsInteractor {

    override fun getThemeSettings(): Settings {
        return settingsRepository.getSettings()
    }

    override fun updateThemeSetting(settings: Settings) {
        settingsRepository.saveSettings(settings)
    }
}