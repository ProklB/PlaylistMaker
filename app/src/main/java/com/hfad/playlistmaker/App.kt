package com.hfad.playlistmaker

import android.app.Application
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate

const val PLAYLISTMAKER_PREFERENCES = "playlistmaker_preferences"
const val THEME_SWITCHER_KEY = "theme_switcher_key"

class App : Application() {

    var darkTheme = false
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate() {
        super.onCreate()
        sharedPreferences = getSharedPreferences(PLAYLISTMAKER_PREFERENCES, MODE_PRIVATE)

        darkTheme = sharedPreferences.getBoolean(THEME_SWITCHER_KEY, false)
        applyTheme(darkTheme)
    }

    fun switchTheme(darkThemeEnabled: Boolean) {
        darkTheme = darkThemeEnabled
        sharedPreferences.edit()
            .putBoolean(THEME_SWITCHER_KEY, darkThemeEnabled)
            .apply()
        applyTheme(darkThemeEnabled)
    }

    private fun applyTheme(darkThemeEnabled: Boolean) {
        AppCompatDelegate.setDefaultNightMode(
            if (darkThemeEnabled) {
                AppCompatDelegate.MODE_NIGHT_YES
            } else {
                AppCompatDelegate.MODE_NIGHT_NO
            }
        )
    }
}
