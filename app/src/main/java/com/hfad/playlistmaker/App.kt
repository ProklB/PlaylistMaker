package com.hfad.playlistmaker

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.hfad.playlistmaker.di.dataModule
import com.hfad.playlistmaker.di.interactorModule
import com.hfad.playlistmaker.di.viewModelModule
import com.hfad.playlistmaker.settings.domain.interactor.SettingsInteractor
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.java.KoinJavaComponent.getKoin

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@App)
            modules(dataModule, interactorModule, viewModelModule)
        }

        val settingsInteractor = getKoin().get<SettingsInteractor>()
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