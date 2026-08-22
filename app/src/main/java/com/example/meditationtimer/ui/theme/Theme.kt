package com.example.meditationtimer.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

// The app is dark by design: a dim room around warm wood.
private val ColorScheme = darkColorScheme(
    primary = Brass,
    onPrimary = Espresso,
    secondary = Wood1,
    onSecondary = Espresso,
    background = Espresso,
    onBackground = Cream,
    surface = EspressoMid,
    onSurface = Cream
)

@Composable
fun MeditationTimerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ColorScheme,
        typography = Typography,
        content = content
    )
}
