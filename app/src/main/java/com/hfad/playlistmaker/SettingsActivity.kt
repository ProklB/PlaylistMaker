package com.hfad.playlistmaker
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.textview.MaterialTextView

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val toolbar = findViewById<MaterialToolbar>(R.id.title)
        setSupportActionBar(toolbar)

        toolbar.setNavigationOnClickListener {
            finish()
        }

        val settings_share = findViewById<MaterialTextView>(R.id.settings_share)
        settings_share.setOnClickListener {
            shareApp()
        }

        val settings_support = findViewById<MaterialTextView>(R.id.settings_support)
        settings_support.setOnClickListener {
            contactSupport()
        }

        val settings_agreement = findViewById<MaterialTextView>(R.id.settings_agreement)
        settings_agreement.setOnClickListener {
            openUserAgreement()
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
}