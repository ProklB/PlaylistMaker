package com.hfad.playlistmaker.settings.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import com.hfad.playlistmaker.R
import com.hfad.playlistmaker.settings.domain.models.Settings
import com.hfad.playlistmaker.settings.ui.compose.SettingsScreen
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsFragment : Fragment() {

    private val viewModel: SettingsViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                SettingsScreen(
                    viewModel = viewModel,
                    onShareClick = ::shareApp,
                    onSupportClick = ::contactSupport,
                    onAgreementClick = ::openUserAgreement
                )
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.themeApplied.observe(viewLifecycleOwner) { settings ->
            applyTheme(settings)
        }
        viewModel.themeSwitchState.observe(viewLifecycleOwner) { isDarkTheme ->
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

    companion object {
        fun newInstance() = SettingsFragment()
    }
}