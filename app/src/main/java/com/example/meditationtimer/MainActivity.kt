package com.example.meditationtimer

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import com.example.meditationtimer.billing.TipJar
import com.example.meditationtimer.timer.KoanDeck
import com.example.meditationtimer.timer.SessionManager
import com.example.meditationtimer.timer.SessionService
import com.example.meditationtimer.ui.AboutSheet
import com.example.meditationtimer.ui.PickerScreen
import com.example.meditationtimer.ui.SessionScreen
import com.example.meditationtimer.ui.theme.Espresso
import com.example.meditationtimer.ui.theme.EspressoGlow
import com.example.meditationtimer.ui.theme.EspressoMid
import com.example.meditationtimer.ui.theme.MeditationTimerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MeditationTimerTheme {
                MeditationApp()
            }
        }
    }
}

@Composable
private fun MeditationApp() {
    val context = LocalContext.current
    val session by SessionManager.state.collectAsState()

    var minutes by rememberSaveable { mutableIntStateOf(10) }
    var divisions by rememberSaveable { mutableIntStateOf(4) }
    val prefs = remember { context.getSharedPreferences("settings", Context.MODE_PRIVATE) }
    var koanEnabled by remember { mutableStateOf(prefs.getBoolean("koan_enabled", false)) }
    val tipJar = remember { TipJar(context) }
    var showAbout by remember { mutableStateOf(false) }
    DisposableEffect(Unit) {
        onDispose { tipJar.close() }
    }

    val notificationPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    Box(
        Modifier
            .fillMaxSize()
            .drawBehind {
                drawRect(
                    Brush.radialGradient(
                        0f to EspressoGlow,
                        0.38f to EspressoMid,
                        0.78f to Espresso,
                        1f to Espresso,
                        center = Offset(size.width / 2f, size.height * 0.34f),
                        radius = size.height * 0.8f
                    )
                )
            }
    ) {
        val current = session
        if (current != null) {
            SessionScreen(current)
        } else {
            PickerScreen(
                minutes = minutes,
                onMinutesChange = { minutes = it },
                divisions = divisions,
                onDivisionsChange = { divisions = it },
                koanEnabled = koanEnabled,
                onKoanToggle = {
                    koanEnabled = !koanEnabled
                    prefs.edit().putBoolean("koan_enabled", koanEnabled).apply()
                },
                onBegin = {
                    if (Build.VERSION.SDK_INT >= 33) {
                        notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                    val koan = if (koanEnabled) KoanDeck.next(context) else null
                    SessionManager.start(minutes, divisions, koan)
                    context.startForegroundService(Intent(context, SessionService::class.java))
                },
                onPreviewGong = { SessionManager.previewGong() },
                onAbout = {
                    showAbout = true
                    tipJar.open()
                }
            )
        }
        if (showAbout) {
            AboutSheet(tipJar = tipJar, onDismiss = { showAbout = false })
        }
    }
}
