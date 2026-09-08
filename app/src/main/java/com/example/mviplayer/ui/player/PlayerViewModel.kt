package com.example.mviplayer.ui.player

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.example.mviplayer.base.BaseViewModel
import com.example.mviplayer.data.SampleMusicData
import com.example.mviplayer.data.Song
import com.example.mviplayer.data.player.PlayerManager
import com.example.mviplayer.data.player.PlayerService
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.mviplayer.data.player.PlayerEventBus

class PlayerViewModel(application: Application) :
    BaseViewModel<PlayerContract.Event, PlayerContract.State, PlayerContract.Effect>(application) {

    private val playerManager = PlayerManager(application)

    init {
        // 加载默认播放列表，并默认初始化第一首
        val defaultList = SampleMusicData.songs
        val firstSong = defaultList.firstOrNull()

        setState {
            copy(
                playlist = defaultList,
                currentSong = firstSong
            )
        }

        // 默认载入第一首音频并弹出通知栏
        firstSong?.let { switchSong(it) }

        // 监听播放/暂停状态流
        viewModelScope.launch {
            playerManager.isPlaying.collect { isPlaying ->
                setState { copy(isPlaying = isPlaying) }
            }
        }

        // 监听音频总时长流
        viewModelScope.launch {
            playerManager.duration.collect { duration ->
                setState { copy(durationMs = duration) }
            }
        }

        // 轮询当前播放进度
        viewModelScope.launch {
            while (true) {
                playerManager.updateProgress()
                setState { copy(currentPositionMs = playerManager.currentPosition.value) }
                delay(500)
            }
        }

        // 监听来自通知栏的按键意图
        viewModelScope.launch {
            PlayerEventBus.events.collect { event ->
                handleEvent(event)
            }
        }
    }

    override fun createInitialState(): PlayerContract.State = PlayerContract.State()

    // 显式声明为 public，允许 MainActivity 无障碍分发 UI 事件
    public override fun handleEvent(event: PlayerContract.Event) {
        when (event) {
            is PlayerContract.Event.PlayPause -> {
                val willPlay = !uiState.value.isPlaying
                if (willPlay) playerManager.play() else playerManager.pause()

                // 同步前台通知栏状态
                uiState.value.currentSong?.let { song ->
                    PlayerService.startOrUpdate(
                        context = getApplication(),
                        title = song.title,
                        artist = song.artist,
                        isPlaying = willPlay
                    )
                }
            }

            is PlayerContract.Event.SeekTo -> {
                playerManager.seekTo(event.positionMs)
            }

            is PlayerContract.Event.SelectSong -> {
                switchSong(event.song)
            }

            is PlayerContract.Event.NextSong -> {
                val list = uiState.value.playlist
                val currentIndex = list.indexOf(uiState.value.currentSong)
                if (currentIndex != -1 && list.isNotEmpty()) {
                    val nextIndex = (currentIndex + 1) % list.size
                    switchSong(list[nextIndex])
                }
            }

            is PlayerContract.Event.PreviousSong -> {
                val list = uiState.value.playlist
                val currentIndex = list.indexOf(uiState.value.currentSong)
                if (currentIndex != -1 && list.isNotEmpty()) {
                    val prevIndex = if (currentIndex - 1 < 0) list.size - 1 else currentIndex - 1
                    switchSong(list[prevIndex])
                }
            }
        }
    }

    // 切换音源并启动/刷新前台悬浮通知栏
    private fun switchSong(song: Song) {
        setState { copy(currentSong = song, currentPositionMs = 0L) }
        playerManager.playUrl(song.mediaUrl)

        PlayerService.startOrUpdate(
            context = getApplication(),
            title = song.title,
            artist = song.artist,
            isPlaying = true
        )
    }

    override fun onCleared() {
        super.onCleared()
        playerManager.release()
        PlayerService.stop(getApplication())
    }
}