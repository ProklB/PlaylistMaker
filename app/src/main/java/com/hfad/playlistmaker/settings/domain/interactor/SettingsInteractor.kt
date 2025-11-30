package com.hfad.playlistmaker.settings.domain.interactor

import com.hfad.playlistmaker.settings.domain.models.Settings

interface SettingsInteractor {
    fun getThemeSettings(): Settings
    fun updateThemeSetting(settings: Settings)
}