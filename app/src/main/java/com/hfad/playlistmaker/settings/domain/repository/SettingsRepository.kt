package com.hfad.playlistmaker.settings.domain.repository

import com.hfad.playlistmaker.settings.domain.models.Settings

interface SettingsRepository {
    fun getSettings(): Settings
    fun saveSettings(settings: Settings)
}