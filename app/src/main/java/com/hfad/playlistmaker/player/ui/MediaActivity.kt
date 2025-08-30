package com.hfad.playlistmaker.player.ui

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.android.material.appbar.MaterialToolbar
import com.hfad.playlistmaker.R
import com.hfad.playlistmaker.player.domain.models.PlayerState
import com.hfad.playlistmaker.search.domain.models.Track
import com.hfad.playlistmaker.search.ui.SearchActivity
import java.text.SimpleDateFormat
import java.util.Locale
import org.koin.androidx.viewmodel.ext.android.viewModel

class MediaActivity : AppCompatActivity() {
    private val viewModel: MediaViewModel by viewModel()

    private lateinit var playButton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_media)

        playButton = findViewById(R.id.playButton)

        val track = intent.getParcelableExtra<Track>(SearchActivity.TRACK_KEY) ?: return

        viewModel.playerState.observe(this) { state ->
            updatePlayButton(state)
        }

        viewModel.currentPosition.observe(this) { position ->
            updateProgress(position)
        }

        viewModel.preparePlayer(track.previewUrl)

        playButton.setOnClickListener {
            viewModel.playPause()
        }

        displayTrackInfo(track)

        setupToolbar()
    }

    private fun setupToolbar() {
        val toolbar = findViewById<MaterialToolbar>(R.id.title)
        toolbar.setNavigationOnClickListener { finish() }
    }

    private fun updatePlayButton(state: PlayerState) {
        playButton.isEnabled = state != PlayerState.DEFAULT
        playButton.setImageResource(
            if (state == PlayerState.PLAYING) R.drawable.button_pause
            else R.drawable.button_play
        )
    }

    private fun updateProgress(position: Int) {
        val timeFormat = SimpleDateFormat("mm:ss", Locale.getDefault())
        findViewById<TextView>(R.id.progressText).text = timeFormat.format(position)
    }

    private fun displayTrackInfo(track: Track) {
        val track = intent.getParcelableExtra<Track>(SearchActivity.TRACK_KEY) ?: return

        findViewById<TextView>(R.id.trackName).text = track.trackName
        findViewById<TextView>(R.id.artistName).text = track.artistName

        val coverImage = findViewById<ImageView>(R.id.coverImage)
        if (track.artworkUrl100.isNotEmpty()) {
            Glide.with(this)
                .load(track.getCoverArtwork())
                .placeholder(R.drawable.playsholder_play_light)
                .centerCrop()
                .transform(RoundedCorners(resources.getDimensionPixelSize(R.dimen.corner_radius_coverImage)))
                .into(coverImage)
        } else {
            coverImage.setImageResource(R.drawable.playsholder_play_light)
        }

        val timeFormat = SimpleDateFormat("mm:ss", Locale.getDefault())
        findViewById<TextView>(R.id.trackTimeMillsValue).text = timeFormat.format(track.trackTimeMillis)
        findViewById<TextView>(R.id.progressText).text = timeFormat.format(0)

        track.collectionName?.let {
            findViewById<TextView>(R.id.collectionNameValue).text = it
        } ?: run {
            findViewById<TextView>(R.id.collectionNameLabel).visibility = View.GONE
            findViewById<TextView>(R.id.collectionNameValue).visibility = View.GONE
        }

        track.releaseDate?.let {
            findViewById<TextView>(R.id.releaseDateValue).text = it.substring(0, 4)
        } ?: run {
            findViewById<TextView>(R.id.releaseDateLabel).visibility = View.GONE
            findViewById<TextView>(R.id.releaseDateValue).visibility = View.GONE
        }

        track.primaryGenreName?.let {
            findViewById<TextView>(R.id.genreNameValue).text = it
        } ?: run {
            findViewById<TextView>(R.id.genreNameLabel).visibility = View.GONE
            findViewById<TextView>(R.id.genreNameValue).visibility = View.GONE
        }

        track.country?.let {
            findViewById<TextView>(R.id.countryValue).text = it
        } ?: run {
            findViewById<TextView>(R.id.countryLabel).visibility = View.GONE
            findViewById<TextView>(R.id.countryValue).visibility = View.GONE
        }
    }

    override fun onPause() {
        super.onPause()
        if (viewModel.playerState.value == PlayerState.PLAYING) {
            viewModel.playPause()
        }
    }
}