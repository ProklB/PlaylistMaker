package com.hfad.playlistmaker.settings.data.repository

import android.content.SharedPreferences
import com.hfad.playlistmaker.settings.domain.models.Settings
import com.hfad.playlistmaker.settings.domain.repository.SettingsRepository

class SettingsRepositoryImpl(
    private val sharedPreferences: SharedPreferences
) : SettingsRepository {

    override fun getSettings(): Settings {
        val darkTheme = sharedPreferences.getBoolean(THEME_SWITCHER_KEY, false)
        return Settings(darkTheme)
    }

    override fun saveSettings(settings: Settings) {
        sharedPreferences.edit()
            .putBoolean(THEME_SWITCHER_KEY, settings.darkThemeEnabled)
            .apply()
    }

    companion object {
        const val PLAYLISTMAKER_PREFERENCES = "playlistmaker_preferences"
        const val THEME_SWITCHER_KEY = "theme_switcher_key"
    }
}