package com.example.meditationtimer.ui

import android.view.WindowManager
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.meditationtimer.timer.SessionManager
import com.example.meditationtimer.timer.SessionState

/**
 * The running sit. All timing and gongs live in [SessionManager]; this
 * screen only renders its state and forwards taps.
 */
@Composable
fun SessionScreen(state: SessionState) {

    // Smooth hand: sample the manager's wall-clock elapsed once per frame.
    var displayedElapsed by remember { mutableLongStateOf(SessionManager.elapsedNow()) }
    LaunchedEffect(state.paused, state.finished) {
        displayedElapsed = SessionManager.elapsedNow()
        if (state.paused || state.finished) return@LaunchedEffect
        while (true) {
            withFrameMillis { }
            displayedElapsed = SessionManager.elapsedNow()
        }
    }

    // A sit keeps the screen awake and goes fullscreen; a swipe shows the bars briefly.
    val activity = LocalActivity.current
    DisposableEffect(activity) {
        val window = activity?.window
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        val controller = window?.let { WindowCompat.getInsetsController(it, it.decorView) }
        controller?.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        controller?.hide(WindowInsetsCompat.Type.systemBars())
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            controller?.show(WindowInsetsCompat.Type.systemBars())
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(48.dp))
        TinyLabel(
            if (state.finished) "complete" else durationLabel((state.totalMs / 60_000L).toInt()),
            fontSize = 13,
            tracking = 5f,
            alpha = 0.66f
        )
        Box(
            Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            BreathingGlow(Modifier.matchParentSize())
            WoodClock(
                divisions = state.divisions,
                koan = state.koan,
                progress = {
                    if (state.totalMs == 0L) 0f else displayedElapsed.toFloat() / state.totalMs
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 36.dp)
            )
        }
        if (!state.finished) {
            RoundControl(66.dp, onClick = { SessionManager.togglePause() }) {
                if (state.paused) PlayIcon(Modifier.size(22.dp)) else PauseIcon(Modifier.size(20.dp))
            }
            Spacer(Modifier.height(10.dp))
            TextControl("end session", onClick = { SessionManager.clear() })
        } else {
            RoundControl(66.dp, onClick = { SessionManager.clear() }) {
                CheckIcon(Modifier.size(24.dp))
            }
            Spacer(Modifier.height(10.dp))
            TextControl("done", onClick = { SessionManager.clear() })
        }
        Spacer(Modifier.height(28.dp))
    }
}
