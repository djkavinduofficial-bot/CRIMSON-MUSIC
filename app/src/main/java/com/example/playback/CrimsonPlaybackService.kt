package com.example.playback

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R

class CrimsonPlaybackService : Service() {

    override fun onCreate() {
        super.onCreate()
        isRunning = true
        ensureNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val controller = CrimsonPlaybackController.getInstance(applicationContext)
        when (intent?.action) {
            ACTION_PREV -> controller.previousTrack()
            ACTION_TOGGLE -> controller.togglePlayPause()
            ACTION_NEXT -> controller.nextTrack()
            ACTION_STOP -> {
                controller.pause()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_SYNC -> {
                val title = intent.getStringExtra(EXTRA_TITLE)
                    ?: controller.state.value.currentTrack?.title
                    ?: getString(R.string.app_name)
                val artist = intent.getStringExtra(EXTRA_ARTIST)
                    ?: controller.state.value.currentTrack?.artist
                    ?: getString(R.string.chip_offline_audio)
                val isPlaying = intent.getBooleanExtra(EXTRA_PLAYING, controller.state.value.isPlaying)
                val notification = buildNotification(title, artist, isPlaying)
                if (isPlaying) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        startForeground(
                            NOTIFICATION_ID,
                            notification,
                            ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
                        )
                    } else {
                        startForeground(NOTIFICATION_ID, notification)
                    }
                } else {
                    val manager = getSystemService(NotificationManager::class.java)
                    manager.notify(NOTIFICATION_ID, notification)
                }
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        isRunning = false
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun ensureNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.notification_channel_desc)
                setShowBadge(false)
            }
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    private fun buildNotification(
        title: String,
        artist: String,
        isPlaying: Boolean
    ): Notification {
        val contentIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val prevIntent = serviceActionPendingIntent(ACTION_PREV, 1)
        val toggleIntent = serviceActionPendingIntent(ACTION_TOGGLE, 2)
        val nextIntent = serviceActionPendingIntent(ACTION_NEXT, 3)
        val stopIntent = serviceActionPendingIntent(ACTION_STOP, 4)

        val playPauseIcon = if (isPlaying) {
            android.R.drawable.ic_media_pause
        } else {
            android.R.drawable.ic_media_play
        }
        val playPauseLabel = if (isPlaying) getString(R.string.cd_pause) else getString(R.string.cd_play)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentTitle(title)
            .setContentText(artist)
            .setContentIntent(contentIntent)
            .setOngoing(isPlaying)
            .setOnlyAlertOnce(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .addAction(android.R.drawable.ic_media_previous, getString(R.string.cd_previous), prevIntent)
            .addAction(playPauseIcon, playPauseLabel, toggleIntent)
            .addAction(android.R.drawable.ic_media_next, getString(R.string.cd_next), nextIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, getString(R.string.cd_close_sheet), stopIntent)
            .build()
    }

    private fun serviceActionPendingIntent(action: String, requestCode: Int): PendingIntent {
        val intent = Intent(this, CrimsonPlaybackService::class.java).apply {
            this.action = action
        }
        return PendingIntent.getService(
            this,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    companion object {
        @Volatile
        var isRunning: Boolean = false
            private set

        const val CHANNEL_ID = "crimson_playback_channel"
        const val NOTIFICATION_ID = 1042

        const val ACTION_SYNC = "com.example.playback.SYNC"
        const val ACTION_PREV = "com.example.playback.PREV"
        const val ACTION_TOGGLE = "com.example.playback.TOGGLE"
        const val ACTION_NEXT = "com.example.playback.NEXT"
        const val ACTION_STOP = "com.example.playback.STOP"

        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_ARTIST = "extra_artist"
        const val EXTRA_PLAYING = "extra_playing"
    }
}
