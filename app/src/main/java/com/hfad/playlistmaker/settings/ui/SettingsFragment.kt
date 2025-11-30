package com.hfad.playlistmaker.settings.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import com.hfad.playlistmaker.R
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatDelegate
import com.hfad.playlistmaker.databinding.FragmentSettingsBinding
import com.hfad.playlistmaker.settings.domain.models.Settings
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsFragment : Fragment(R.layout.fragment_settings) {

    private val viewModel: SettingsViewModel by viewModel()

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSettingsBinding.bind(view)

        setupViews()
        observeViewModel()
    }

    private fun setupViews() {
        binding.title.setNavigationOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        binding.themeSwitcher.setOnCheckedChangeListener { _, isChecked ->
            viewModel.onThemeSwitchChanged(isChecked)
        }

        binding.settingsShare.setOnClickListener {
            shareApp()
        }

        binding.settingsSupport.setOnClickListener {
            contactSupport()
        }

        binding.settingsAgreement.setOnClickListener {
            openUserAgreement()
        }
    }

    private fun observeViewModel() {
        viewModel.themeSwitchState.observe(viewLifecycleOwner) { isDarkTheme ->
            binding.themeSwitcher.isChecked = isDarkTheme
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = SettingsFragment()
    }
}