package com.example.meditationtimer.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.sin

/**
 * Synthesizes and plays the gongs; no audio assets involved.
 *
 * Sound design, as settled on the design canvas: detuned sine partials of a
 * bowl strike around G3, a slow attack, an exponential decay. The closing
 * gong adds a 98 Hz layer and rings out longer.
 */
class GongPlayer {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val quarterPcm: Deferred<ShortArray> = scope.async {
        render(
            parts = listOf(
                Partial(196.0, 0.50, 8.0),
                Partial(392.5, 0.18, 6.0),
                Partial(584.0, 0.09, 4.5),
                Partial(823.0, 0.05, 3.0)
            ),
            attack = 0.05,
            master = 0.40
        )
    }

    private val endPcm: Deferred<ShortArray> = scope.async {
        render(
            parts = listOf(
                Partial(98.0, 0.32, 14.0),
                Partial(196.0, 0.50, 13.0),
                Partial(392.5, 0.16, 9.0),
                Partial(584.0, 0.08, 6.0),
                Partial(823.0, 0.04, 4.0)
            ),
            attack = 0.07,
            master = 0.45
        )
    }

    /** The gong used at the start of the sit and at each quarter. */
    fun playQuarter() = play(quarterPcm)

    /** The longer, deeper gong that closes the sit. */
    fun playEnd() = play(endPcm)

    fun release() = scope.cancel()

    private fun play(pcm: Deferred<ShortArray>) {
        scope.launch {
            val samples = pcm.await()
            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(SAMPLE_RATE)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setTransferMode(AudioTrack.MODE_STATIC)
                .setBufferSizeInBytes(samples.size * 2)
                .build()
            try {
                track.write(samples, 0, samples.size)
                track.play()
                delay(samples.size * 1000L / SAMPLE_RATE + 200L)
            } finally {
                track.release()
            }
        }
    }

    private data class Partial(val freq: Double, val amp: Double, val decay: Double)

    private fun render(parts: List<Partial>, attack: Double, master: Double): ShortArray {
        val total = parts.maxOf { it.decay } + 0.25
        val n = (total * SAMPLE_RATE).toInt()
        val mix = DoubleArray(n)
        for (part in parts) {
            val peak = part.amp * 0.5
            val rate = ln(1e-4 / peak) / (part.decay - attack)
            val attackSamples = (attack * SAMPLE_RATE).toInt()
            val end = (part.decay * SAMPLE_RATE).toInt().coerceAtMost(n)
            for (detune in DETUNE) {
                val step = 2.0 * Math.PI * part.freq * detune / SAMPLE_RATE
                var phase = 0.0
                for (i in 0 until end) {
                    val t = i.toDouble() / SAMPLE_RATE
                    val env = if (i < attackSamples) peak * i / attackSamples else peak * exp(rate * (t - attack))
                    mix[i] += env * sin(phase)
                    phase += step
                }
            }
        }
        return ShortArray(n) { i ->
            val v = (mix[i] * master).coerceIn(-1.0, 1.0)
            (v * Short.MAX_VALUE).toInt().toShort()
        }
    }

    private companion object {
        const val SAMPLE_RATE = 44100
        val DETUNE = doubleArrayOf(1.0, 1.0021)
    }
}
