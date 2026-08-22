package com.example.meditationtimer.timer

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.PowerManager
import com.example.meditationtimer.MainActivity
import com.example.meditationtimer.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Foreground vehicle for a running sit: keeps the process alive and the CPU
 * awake (partial wakelock) so the clock and the gongs stay on time with the
 * screen off. All session logic lives in [SessionManager]; this service only
 * mirrors its state into a notification and its own lifetime.
 */
class SessionService : Service() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var wakeLock: PowerManager.WakeLock? = null

    override fun onCreate() {
        super.onCreate()
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Meditation session",
            NotificationManager.IMPORTANCE_LOW
        )
        channel.setShowBadge(false)
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, buildNotification("Meditation in progress"))
        scope.coroutineContext.cancelChildren()
        scope.launch {
            SessionManager.state.collect { s ->
                when {
                    s == null -> stopSelf()
                    s.finished -> {
                        notify("Session complete")
                        releaseWake()
                        delay(16_000) // let the closing gong ring out
                        stopSelf()
                    }
                    s.paused -> {
                        notify("Paused")
                        releaseWake()
                    }
                    else -> {
                        acquireWake(s.totalMs - SessionManager.elapsedNow() + 30_000L)
                        notify("Meditation in progress")
                    }
                }
            }
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        releaseWake()
        scope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun acquireWake(timeoutMs: Long) {
        val lock = wakeLock ?: getSystemService(PowerManager::class.java)
            .newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MeditationTimer:session")
            .also {
                it.setReferenceCounted(false)
                wakeLock = it
            }
        lock.acquire(timeoutMs)
    }

    private fun releaseWake() {
        if (wakeLock?.isHeld == true) wakeLock?.release()
    }

    private fun buildNotification(text: String): Notification =
        Notification.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_gong_notification)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(text)
            .setOngoing(true)
            .setContentIntent(
                PendingIntent.getActivity(
                    this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE
                )
            )
            .build()

    private fun notify(text: String) {
        getSystemService(NotificationManager::class.java).notify(NOTIFICATION_ID, buildNotification(text))
    }

    private companion object {
        const val CHANNEL_ID = "session"
        const val NOTIFICATION_ID = 1
    }
}
