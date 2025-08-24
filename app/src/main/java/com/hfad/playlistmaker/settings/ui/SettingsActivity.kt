package com.hfad.playlistmaker.settings.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textview.MaterialTextView
import com.hfad.playlistmaker.R
import com.hfad.playlistmaker.creator.Creator
import com.hfad.playlistmaker.settings.domain.models.Settings

class SettingsActivity : AppCompatActivity() {

    private lateinit var viewModel: SettingsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        viewModel = ViewModelProvider(this, SettingsViewModelFactory(
            Creator.provideSettingsInteractor(application)
        )).get(SettingsViewModel::class.java)

        setupToolbar()
        setupViews()
        observeViewModel()
    }

    private fun setupToolbar() {
        val toolbar = findViewById<MaterialToolbar>(R.id.title)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupViews() {
        val themeSwitcher = findViewById<SwitchMaterial>(R.id.themeSwitcher)
        themeSwitcher.setOnCheckedChangeListener { _, isChecked ->
            viewModel.onThemeSwitchChanged(isChecked)
        }

        findViewById<MaterialTextView>(R.id.settings_share).setOnClickListener {
            shareApp()
        }

        findViewById<MaterialTextView>(R.id.settings_support).setOnClickListener {
            contactSupport()
        }

        findViewById<MaterialTextView>(R.id.settings_agreement).setOnClickListener {
            openUserAgreement()
        }
    }

    private fun observeViewModel() {
        viewModel.themeSwitchState.observe(this) { isDarkTheme ->
            findViewById<SwitchMaterial>(R.id.themeSwitcher).isChecked = isDarkTheme
            applyTheme(Settings(isDarkTheme))
        }
    }

    private fun shareApp() {
        val shareMessage = getString(R.string.share_message, getString(R.string.share_link))
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareMessage)
        }
        startActivity(Intent.createChooser(shareIntent, getString(R.string.textview_share)))
    }

    private fun contactSupport() {
        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf(getString(R.string.support_email)))
            putExtra(Intent.EXTRA_SUBJECT, getString(R.string.support_theme))
            putExtra(Intent.EXTRA_TEXT, getString(R.string.support_text))
        }
        startActivity(Intent.createChooser(emailIntent, getString(R.string.textview_support)))
    }

    private fun openUserAgreement() {
        val agreementUri = Uri.parse(getString(R.string.agreement_link))
        val browserIntent = Intent(Intent.ACTION_VIEW, agreementUri)
        startActivity(browserIntent)
    }

    private fun applyTheme(settings: Settings) {
        AppCompatDelegate.setDefaultNightMode(
            if (settings.darkThemeEnabled) {
                AppCompatDelegate.MODE_NIGHT_YES
            } else {
                AppCompatDelegate.MODE_NIGHT_NO
            }
        )
    }
}