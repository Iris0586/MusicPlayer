package com.example.mviplayer.data.player

import com.example.mviplayer.ui.player.PlayerContract
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object PlayerEventBus {
    private val _events = MutableSharedFlow<PlayerContract.Event>(extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    fun post(event: PlayerContract.Event) {
        _events.tryEmit(event)
    }
}