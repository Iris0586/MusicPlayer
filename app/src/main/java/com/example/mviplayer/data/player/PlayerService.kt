package com.example.mviplayer.data.player

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.mviplayer.MainActivity
import com.example.mviplayer.ui.player.PlayerContract

class PlayerService : Service() {

    companion object {
        const val CHANNEL_ID = "music_player_channel"
        const val NOTIFICATION_ID = 1001

        const val ACTION_UPDATE = "com.example.mviplayer.ACTION_UPDATE"
        const val ACTION_STOP = "com.example.mviplayer.ACTION_STOP"
        const val ACTION_PREV = "com.example.mviplayer.ACTION_PREV"
        const val ACTION_PLAY_PAUSE = "com.example.mviplayer.ACTION_PLAY_PAUSE"
        const val ACTION_NEXT = "com.example.mviplayer.ACTION_NEXT"

        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_ARTIST = "extra_artist"
        const val EXTRA_IS_PLAYING = "extra_is_playing"

        fun startOrUpdate(context: Context, title: String, artist: String, isPlaying: Boolean) {
            val intent = Intent(context, PlayerService::class.java).apply {
                action = ACTION_UPDATE
                putExtra(EXTRA_TITLE, title)
                putExtra(EXTRA_ARTIST, artist)
                putExtra(EXTRA_IS_PLAYING, isPlaying)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, PlayerService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
            ACTION_UPDATE -> {
                val title = intent.getStringExtra(EXTRA_TITLE) ?: "未知歌曲"
                val artist = intent.getStringExtra(EXTRA_ARTIST) ?: "未知歌手"
                val isPlaying = intent.getBooleanExtra(EXTRA_IS_PLAYING, false)

                val notification = buildNotification(title, artist, isPlaying)
                startForeground(NOTIFICATION_ID, notification)
            }
            // 接收通知栏按钮点击，转发给 MVI 总线
            ACTION_PREV -> PlayerEventBus.post(PlayerContract.Event.PreviousSong)
            ACTION_PLAY_PAUSE -> PlayerEventBus.post(PlayerContract.Event.PlayPause)
            ACTION_NEXT -> PlayerEventBus.post(PlayerContract.Event.NextSong)
        }
        return START_NOT_STICKY
    }

    private fun buildNotification(title: String, artist: String, isPlaying: Boolean): Notification {
        val clickIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, clickIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 绑定上一首 PendingIntent
        val prevPendingIntent = PendingIntent.getService(
            this, 1,
            Intent(this, PlayerService::class.java).apply { action = ACTION_PREV },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 绑定播放/暂停 PendingIntent
        val playPausePendingIntent = PendingIntent.getService(
            this, 2,
            Intent(this, PlayerService::class.java).apply { action = ACTION_PLAY_PAUSE },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 绑定下一首 PendingIntent
        val nextPendingIntent = PendingIntent.getService(
            this, 3,
            Intent(this, PlayerService::class.java).apply { action = ACTION_NEXT },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(artist)
            .setSubText(if (isPlaying) "正在播放" else "已暂停")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentIntent(pendingIntent)
            .setOngoing(isPlaying)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            // 添加底部 3 个控制按钮
            .addAction(android.R.drawable.ic_media_previous, "上一首", prevPendingIntent)
            .addAction(
                if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play,
                if (isPlaying) "暂停" else "播放",
                playPausePendingIntent
            )
            .addAction(android.R.drawable.ic_media_next, "下一首", nextPendingIntent)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "音乐播放控制",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "展示后台媒体播放进度与控制条"
                setShowBadge(false)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }
}