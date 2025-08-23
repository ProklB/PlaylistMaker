package com.hfad.playlistmaker

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.hfad.playlistmaker.creator.Creator

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        val settingsInteractor = Creator.provideSettingsInteractor(this)
        val settings = settingsInteractor.getThemeSettings()

        AppCompatDelegate.setDefaultNightMode(
            if (settings.darkThemeEnabled) {
                AppCompatDelegate.MODE_NIGHT_YES
            } else {
                AppCompatDelegate.MODE_NIGHT_NO
            }
        )
    }
}