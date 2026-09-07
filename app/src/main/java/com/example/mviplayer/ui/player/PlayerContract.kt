package com.example.mviplayer.ui.player

import com.example.mviplayer.base.UiEffect
import com.example.mviplayer.base.UiEvent
import com.example.mviplayer.base.UiState

class PlayerContract {

    // 1. 定义播放器状态 (State)
    data class State(
        val isPlaying: Boolean = false,
        val mediaTitle: String = "未开始播放",
        val currentPositionMs: Long = 0L,
        val durationMs: Long = 0L,
        val isLoading: Boolean = false
    ) : UiState

    // 2. 定义用户意图 (Event)
    sealed class Event : UiEvent {
        object PlayClick : Event()
        object PauseClick : Event()
        data class SeekTo(val positionMs: Long) : Event()
    }

    // 3. 定义一次性副作用 (Effect)
    sealed class Effect : UiEffect {
        data class ShowToast(val message: String) : Effect()
    }
}