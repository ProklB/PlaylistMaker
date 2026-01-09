package com.hfad.playlistmaker.player.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.hfad.playlistmaker.R
import com.hfad.playlistmaker.main.ui.MainActivity
import com.hfad.playlistmaker.player.domain.models.PlayerState
import com.hfad.playlistmaker.player.domain.repository.PlayerRepository
import com.hfad.playlistmaker.search.domain.models.Track
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class AudioPlayerService : Service(), PlayerServiceInterface {

    inner class AudioPlayerBinder : Binder() {
        fun getService(): AudioPlayerService = this@AudioPlayerService
    }

    private val binder = AudioPlayerBinder()
    private val playerRepository: PlayerRepository by inject()
    private var currentTrack: Track? = null
    private val _playerState = MutableStateFlow(PlayerState.DEFAULT)
    override fun getPlayerStateFlow(): StateFlow<PlayerState> = _playerState.asStateFlow()

    private val _currentPosition = MutableStateFlow(0)
    override fun getCurrentPositionFlow(): StateFlow<Int> = _currentPosition.asStateFlow()
    private var progressUpdateJob: Job? = null

    private var isNotificationShowing = false

    private val serviceScope = CoroutineScope(Dispatchers.Main)
    private var playDelayJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        playerRepository.setOnPreparedListener {
            _playerState.update { PlayerState.PREPARED }
        }

        playerRepository.setOnCompletionListener {
            _playerState.update { PlayerState.PREPARED }
            _currentPosition.update { 0 }
            stopProgressUpdates()
            hideNotification()
        }
    }

    override fun onBind(intent: Intent?): IBinder {
        val track = intent?.getParcelableExtra<Track>("track")
        track?.let {
            currentTrack = it
            preparePlayer(it)
        }
        return binder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        if (_playerState.value == PlayerState.PLAYING) {
            pause()
        }
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        playDelayJob?.cancel()
        playerRepository.releasePlayer()
        stopProgressUpdates()
    }

    override fun preparePlayer(track: Track) {
        currentTrack = track
        playerRepository.preparePlayer(track.previewUrl)
    }

    override fun play() {
        playerRepository.startPlayer()
        _playerState.update { PlayerState.PLAYING }

        playDelayJob?.cancel()
        playDelayJob = serviceScope.launch {
            delay(PLAY_DELAY_MS)
            startProgressUpdates()
        }
    }

    override fun pause() {
        playerRepository.pausePlayer()
        _playerState.update { PlayerState.PAUSED }
        stopProgressUpdates()
        playDelayJob?.cancel()
    }

    override fun getPlayerState(): PlayerState {
        return playerRepository.getPlayerState()
    }

    override fun getCurrentPosition(): Int {
        return playerRepository.getCurrentPosition()
    }

    override fun showNotification() {
        if (isNotificationShowing || currentTrack == null) return

        val notification = createNotification()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ServiceCompat.startForeground(
                this,
                NOTIFICATION_ID,
                notification,
                2
            )
        } else {
            ServiceCompat.startForeground(this, NOTIFICATION_ID, notification, 0)
        }

        isNotificationShowing = true
    }

    override fun hideNotification() {
        if (!isNotificationShowing) return

        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        isNotificationShowing = false
    }

    private fun startProgressUpdates() {
        stopProgressUpdates()

        progressUpdateJob = serviceScope.launch {
            while (true) {
                if (_playerState.value == PlayerState.PLAYING) {
                    _currentPosition.update { playerRepository.getCurrentPosition() }
                }
                delay(PROGRESS_UPDATE_INTERVAL_MS)
            }
        }
    }

    private fun stopProgressUpdates() {
        progressUpdateJob?.cancel()
        progressUpdateJob = null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.notification_channel_description)
                setShowBadge(false)
            }

            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val track = currentTrack ?: throw IllegalStateException(
            getString(R.string.exception_no_track_for_notification)
        )

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("notification_clicked", true)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText("${track.artistName} - ${track.trackName}")
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setSilent(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
    }

    companion object {
        private const val PLAY_DELAY_MS = 50L
        private const val PROGRESS_UPDATE_INTERVAL_MS = 300L
        private const val NOTIFICATION_CHANNEL_ID = "audio_player_channel"
        private const val NOTIFICATION_ID = 1
    }
}