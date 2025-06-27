package com.hfad.playlistmaker

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.android.material.appbar.MaterialToolbar
import java.text.SimpleDateFormat
import java.util.Locale

class MediaActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_media)

        setupToolbar()
        displayTrackInfo()
    }

    private fun setupToolbar() {
        val toolbar = findViewById<MaterialToolbar>(R.id.title)
//        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }
    }

    private fun displayTrackInfo() {
        val track = intent.getSerializableExtra("TRACK") as? Track ?: return

        findViewById<TextView>(R.id.trackName).text = track.trackName
        findViewById<TextView>(R.id.artistName).text = track.artistName

        val coverImage = findViewById<ImageView>(R.id.coverImage)
        if (track.artworkUrl100.isNotEmpty()) {
            Glide.with(this)
                .load(track.getCoverArtwork())
                .placeholder(R.drawable.playsholder_play_light)
                .centerCrop()
                .transform(RoundedCorners(8))
                .into(coverImage)
        } else {
            coverImage.setImageResource(R.drawable.playsholder_play_light)
        }

        val timeFormat = SimpleDateFormat("mm:ss", Locale.getDefault())
        findViewById<TextView>(R.id.trackTimeMillsValue).text = timeFormat.format(track.trackTimeMillis)
        findViewById<TextView>(R.id.progressText).text = "00:00"

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
}