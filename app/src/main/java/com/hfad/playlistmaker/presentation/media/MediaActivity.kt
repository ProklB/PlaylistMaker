package com.hfad.playlistmaker.presentation.media

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.android.material.appbar.MaterialToolbar
import com.hfad.playlistmaker.R
import com.hfad.playlistmaker.domain.models.Track
import com.hfad.playlistmaker.presentation.search.SearchActivity
import java.text.SimpleDateFormat
import java.util.Locale

class MediaActivity : AppCompatActivity() {

    private var playerState = PlayerState.DEFAULT
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var playButton: ImageButton
    private lateinit var progressText: TextView
    private lateinit var handler: Handler
    private lateinit var updateProgressRunnable: Runnable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_media)

        playButton = findViewById(R.id.playButton)
        progressText = findViewById(R.id.progressText)
        handler = Handler(Looper.getMainLooper())

        updateProgressRunnable = object : Runnable {
            override fun run() {
                if (playerState == PlayerState.PLAYING) {
                    val timeFormat = SimpleDateFormat("mm:ss", Locale.getDefault())
                    progressText.text = timeFormat.format(mediaPlayer?.currentPosition)
                    handler.postDelayed(this, PROGRESS_UPDATE_DELAY)
                }
            }
        }

        setupToolbar()
        displayTrackInfo()
        preparePlayer()
    }

    private fun setupToolbar() {
        val toolbar = findViewById<MaterialToolbar>(R.id.title)
        toolbar.setNavigationOnClickListener { finish() }
    }

    private fun displayTrackInfo() {
        val track = intent.getParcelableExtra<Track>(SearchActivity.TRACK_KEY) ?: return

        findViewById<TextView>(R.id.trackName).text = track.trackName
        findViewById<TextView>(R.id.artistName).text = track.artistName

        val coverImage = findViewById<ImageView>(R.id.coverImage)
        if (track.artworkUrl100.isNotEmpty()) {
            Glide.with(this)
                .load(track.getCoverArtwork())
                .placeholder(R.drawable.playsholder_play_light)
                .centerCrop()
                .transform(RoundedCorners(R.dimen.corner_radius_coverImage))
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

    private fun preparePlayer() {
        val track = intent.getParcelableExtra<Track>(SearchActivity.TRACK_KEY) ?: run {
            finish() // Закрываем активность, если трек не передан
            return
        }

        mediaPlayer = MediaPlayer().apply {
            setDataSource(track.previewUrl)
            prepareAsync()
            setOnPreparedListener {
                playerState = PlayerState.PREPARED
                playButton.isEnabled = true
            }
            setOnCompletionListener {
                playerState = PlayerState.PREPARED
                playButton.setImageResource(R.drawable.button_play)
                handler.removeCallbacks(updateProgressRunnable)
                progressText.text = "00:00"
            }
        }

        playButton.setOnClickListener {
            playbackControl()
        }
    }

    private fun playbackControl() {
        when(playerState) {
            PlayerState.PLAYING -> pausePlayer()
            PlayerState.PREPARED, PlayerState.PAUSED -> startPlayer()
            PlayerState.DEFAULT -> {
                playerState = PlayerState.PREPARED
                startPlayer()
            }
        }
    }

    private fun startPlayer() {
        mediaPlayer?.start()
        playButton.setImageResource(R.drawable.button_pause)
        playerState = PlayerState.PLAYING
        handler.post(updateProgressRunnable)
    }

    private fun pausePlayer() {
        mediaPlayer?.pause()
        playButton.setImageResource(R.drawable.button_play)
        playerState = PlayerState.PAUSED
        handler.removeCallbacks(updateProgressRunnable)
    }

    override fun onPause() {
        super.onPause()
        if (playerState == PlayerState.PLAYING) {
            pausePlayer()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(updateProgressRunnable)
        mediaPlayer?.release()
        mediaPlayer = null
    }

    companion object {
        private const val PROGRESS_UPDATE_DELAY = 300L
    }

    enum class PlayerState {
        DEFAULT,
        PREPARED,
        PLAYING,
        PAUSED
    }
}