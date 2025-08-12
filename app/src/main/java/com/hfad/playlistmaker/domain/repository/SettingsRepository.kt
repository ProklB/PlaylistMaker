package com.hfad.playlistmaker.domain.repository

import com.hfad.playlistmaker.domain.models.Settings

interface SettingsRepository {
    fun getSettings(): Settings
    fun saveSettings(settings: Settings)
}