package com.example.mviplayer.ui.player

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.collectLatest

@Composable
fun PlayerScreen() {
    val context = LocalContext.current

    val viewModel: PlayerViewModel = viewModel(
        factory = androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.getInstance(
            context.applicationContext as Application
        )
    )

    val state by viewModel.uiState.collectAsState()

    // 面试打印点：日志观察主页面重组
    Log.d("ComposeOptimization", "--> PlayerScreen (全页) 重组")

    LaunchedEffect(key1 = Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is PlayerContract.Effect.ShowToast -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 标题部分（低频变化）
        Text(
            text = state.mediaTitle,
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(32.dp))

        // 核心亮点：抽离的高频进度条小组件（隔离重组范围）
        PlayerProgressBar(
            currentPositionMs = state.currentPositionMs,
            durationMs = state.durationMs,
            onSeek = { newPos ->
                viewModel.setEvent(PlayerContract.Event.SeekTo(newPos))
            }
        )

        Spacer(modifier = Modifier.height(32.dp))

        // 控制按钮部分（低频变化）
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (state.isPlaying) {
                Button(onClick = { viewModel.setEvent(PlayerContract.Event.PauseClick) }) {
                    Text("暂停")
                }
            } else {
                Button(onClick = { viewModel.setEvent(PlayerContract.Event.PlayClick) }) {
                    Text("播放")
                }
            }
        }
    }
}

/**
 * 优化亮点小组件：独立的进度条组件
 * 通过 derivedStateOf 派生状态，实现高频刷新防抖与重组范围隔离
 */
@Composable
fun PlayerProgressBar(
    currentPositionMs: Long,
    durationMs: Long,
    onSeek: (Long) -> Unit
) {
    // 打印日志：观察小组件重组
    Log.d("ComposeOptimization", "====> PlayerProgressBar (仅进度条) 重组")

    // 使用 derivedStateOf 派生状态：将毫秒转为秒，只有秒数改变时才触发刷新
    val currentSecond by remember(currentPositionMs) {
        derivedStateOf { currentPositionMs / 1000 }
    }
    val totalSecond by remember(durationMs) {
        derivedStateOf { durationMs / 1000 }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Slider(
            value = currentPositionMs.toFloat(),
            onValueChange = { newPos -> onSeek(newPos.toLong()) },
            valueRange = 0f..(if (durationMs > 0) durationMs.toFloat() else 1f),
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "${currentSecond}s")
            Text(text = "${totalSecond}s")
        }
    }
}