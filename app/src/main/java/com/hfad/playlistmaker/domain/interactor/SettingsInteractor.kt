package com.hfad.playlistmaker.domain.interactor

import com.hfad.playlistmaker.domain.models.Settings

interface SettingsInteractor {
    fun getThemeSettings(): Settings
    fun updateThemeSetting(settings: Settings)
    fun applyTheme(settings: Settings)
}