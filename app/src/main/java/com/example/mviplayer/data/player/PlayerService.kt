package com.example.mviplayer.data.player

import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService

/**
 * 前台服务：负责后台保活与通知栏控制
 */
class PlayerService : MediaSessionService() {

    private var mediaSession: MediaSession? = null

    override fun onCreate() {
        super.onCreate()

        // 1. 初始化 ExoPlayer
        val player = ExoPlayer.Builder(this).build()

        // 2. 为播放器设置系统通知栏要展示的元数据 (MediaMetadata)
        val metadata = MediaMetadata.Builder()
            .setTitle("SoundHelix Song 17")
            .setArtist("SoundHelix")
            .build()

        val mediaItem = MediaItem.Builder()
            .setUri("https://www.soundhelix.com/examples/mp3/SoundHelix-Song-17.mp3")
            .setMediaMetadata(metadata)
            .build()

        player.setMediaItem(mediaItem)
        player.prepare()

        // 3. 构建 MediaSession 绑定 Service
        mediaSession = MediaSession.Builder(this, player).build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }
}