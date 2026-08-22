package com.example.meditationtimer.timer

import android.os.SystemClock
import com.example.meditationtimer.audio.GongPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class SessionState(
    val totalMs: Long,
    val divisions: Int,
    val baseRealtime: Long,   // elapsedRealtime anchor while running
    val accumulatedMs: Long,  // frozen elapsed while paused or finished
    val paused: Boolean,
    val finished: Boolean,
    val koan: String? = null  // carved into the dial for this sit, when enabled
)

/**
 * Owns the running sit: wall-clock anchored elapsed time and the gong
 * schedule. Lives outside any activity, so the UI can die and come back;
 * [SessionService] keeps the process and CPU alive while a sit runs.
 */
object SessionManager {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val gong by lazy { GongPlayer() }

    private val _state = MutableStateFlow<SessionState?>(null)
    val state: StateFlow<SessionState?> = _state

    private var runJob: Job? = null
    private var firedGongs = 0

    fun previewGong() = gong.playQuarter()

    fun start(minutes: Int, divisions: Int, koan: String? = null) {
        runJob?.cancel()
        firedGongs = 0
        _state.value = SessionState(
            totalMs = minutes * 60_000L,
            divisions = divisions,
            baseRealtime = SystemClock.elapsedRealtime(),
            accumulatedMs = 0L,
            paused = false,
            finished = false,
            koan = koan
        )
        gong.playQuarter() // opening gong
        startRunJob()
    }

    fun togglePause() {
        val s = _state.value ?: return
        if (s.finished) return
        if (s.paused) {
            _state.value = s.copy(
                paused = false,
                baseRealtime = SystemClock.elapsedRealtime() - s.accumulatedMs
            )
            startRunJob()
        } else {
            runJob?.cancel()
            _state.value = s.copy(paused = true, accumulatedMs = elapsedNow())
        }
    }

    /** Ends the sit (user abort or the done button); the service stops itself. */
    fun clear() {
        runJob?.cancel()
        _state.value = null
    }

    fun elapsedNow(): Long {
        val s = _state.value ?: return 0L
        val elapsed =
            if (s.paused || s.finished) s.accumulatedMs
            else SystemClock.elapsedRealtime() - s.baseRealtime
        return elapsed.coerceIn(0L, s.totalMs)
    }

    private fun startRunJob() {
        runJob = scope.launch {
            while (isActive) {
                val s = _state.value ?: return@launch
                if (s.paused || s.finished) return@launch
                val elapsed = SystemClock.elapsedRealtime() - s.baseRealtime
                // Fire everything that is due; catches up in a burst after any stall.
                while (firedGongs < s.divisions - 1 && elapsed * s.divisions >= s.totalMs * (firedGongs + 1)) {
                    firedGongs++
                    gong.playQuarter()
                }
                if (elapsed >= s.totalMs) {
                    _state.value = s.copy(finished = true, paused = false, accumulatedMs = s.totalMs)
                    gong.playEnd()
                    return@launch
                }
                val nextK = if (firedGongs < s.divisions - 1) firedGongs + 1 else s.divisions
                val nextAt = s.totalMs * nextK / s.divisions
                delay((nextAt - elapsed).coerceAtLeast(50L))
            }
        }
    }
}
