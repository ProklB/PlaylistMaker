package com.hfad.playlistmaker

import android.app.Application
import com.hfad.playlistmaker.domain.interactor.SettingsInteractor

class App : Application() {

    private lateinit var settingsInteractor: SettingsInteractor

    override fun onCreate() {
        super.onCreate()

        settingsInteractor = Creator.provideSettingsInteractor(this)
        val settings = settingsInteractor.getThemeSettings()
        settingsInteractor.applyTheme(settings)
    }
}