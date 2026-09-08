package com.example.mviplayer.ui.player

import com.example.mviplayer.base.UiEffect
import com.example.mviplayer.base.UiEvent
import com.example.mviplayer.base.UiState
import com.example.mviplayer.data.Song

class PlayerContract {

    sealed interface Event : UiEvent {
        object PlayPause : Event
        data class SeekTo(val positionMs: Long) : Event
        data class SelectSong(val song: Song) : Event
        object NextSong : Event
        object PreviousSong : Event
    }

    data class State(
        val isPlaying: Boolean = false,
        val durationMs: Long = 0L,
        val currentPositionMs: Long = 0L,
        val playlist: List<Song> = emptyList(),
        val currentSong: Song? = null
    ) : UiState

    sealed interface Effect : UiEffect {
        data class ShowToast(val message: String) : Effect
    }
}