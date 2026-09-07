package com.example.mviplayer.ui.player

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.example.mviplayer.base.BaseViewModel
import com.example.mviplayer.data.player.PlayerManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PlayerViewModel(application: Application) :
    BaseViewModel<PlayerContract.Event, PlayerContract.State, PlayerContract.Effect>(application) {

    private val playerManager = PlayerManager(application)

    init {
        // 监听播放状态
        viewModelScope.launch {
            playerManager.isPlaying.collect { isPlaying ->
                setState { copy(isPlaying = isPlaying) }
            }
        }

        // 监听总时长
        viewModelScope.launch {
            playerManager.duration.collect { duration ->
                setState { copy(durationMs = duration) }
            }
        }

        // 定时刷新进度
        viewModelScope.launch {
            while (true) {
                playerManager.updateProgress()
                setState { copy(currentPositionMs = playerManager.currentPosition.value) }
                delay(500)
            }
        }
    }

    override fun createInitialState(): PlayerContract.State {
        return PlayerContract.State(
            isPlaying = false,
            mediaTitle = "SoundHelix Song 17",
            currentPositionMs = 0L,
            durationMs = 0L
        )
    }

    override fun handleEvent(event: PlayerContract.Event) {
        when (event) {
            is PlayerContract.Event.PlayClick -> {
                playerManager.play()
                setEffect { PlayerContract.Effect.ShowToast("开始播放网络音频") }
            }
            is PlayerContract.Event.PauseClick -> {
                playerManager.pause()
                setEffect { PlayerContract.Effect.ShowToast("已暂停") }
            }
            is PlayerContract.Event.SeekTo -> {
                playerManager.seekTo(event.positionMs)
                setState { copy(currentPositionMs = event.positionMs) }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        playerManager.release()
    }
}