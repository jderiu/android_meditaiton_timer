package com.example.meditationtimer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.meditationtimer.ui.theme.Brass
import com.example.meditationtimer.ui.theme.ControlIvory
import com.example.meditationtimer.ui.theme.Cormorant
import com.example.meditationtimer.ui.theme.Cream
import com.example.meditationtimer.ui.theme.CreamBright
import com.example.meditationtimer.ui.theme.Jost
import com.example.meditationtimer.timer.DivisionOptions
import com.example.meditationtimer.timer.divisionGlyph
import kotlin.math.roundToInt

private val Presets = listOf(5, 10, 15, 20, 30)

@Composable
fun PickerScreen(
    minutes: Int,
    onMinutesChange: (Int) -> Unit,
    divisions: Int,
    onDivisionsChange: (Int) -> Unit,
    koanEnabled: Boolean,
    onKoanToggle: () -> Unit,
    onBegin: () -> Unit,
    onPreviewGong: () -> Unit,
    onAbout: () -> Unit
) {
    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(48.dp))
        TinyLabel("meditation", fontSize = 13, tracking = 5f, alpha = 0.66f)
        Spacer(Modifier.weight(0.9f))
        Text(
            "$minutes",
            fontFamily = Cormorant,
            fontWeight = FontWeight.Medium,
            fontSize = 96.sp,
            color = CreamBright
        )
        TinyLabel("minutes", fontSize = 11, tracking = 4f, alpha = 0.6f)
        Spacer(Modifier.height(26.dp))
        Slider(
            value = minutes.toFloat(),
            onValueChange = { onMinutesChange(it.roundToInt().coerceIn(1, 60)) },
            valueRange = 1f..60f,
            colors = SliderDefaults.colors(
                thumbColor = ControlIvory,
                activeTrackColor = Brass.copy(alpha = 0.8f),
                inactiveTrackColor = Cream.copy(alpha = 0.15f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 56.dp)
        )
        Spacer(Modifier.height(18.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Presets.forEach { preset ->
                PresetPill(preset, selected = preset == minutes) { onMinutesChange(preset) }
            }
        }
        Spacer(Modifier.height(26.dp))
        TinyLabel("gong every", fontSize = 10, tracking = 3f, alpha = 0.5f)
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            DivisionOptions.forEach { option ->
                DivisionPill(option, selected = option == divisions) { onDivisionsChange(option) }
            }
        }
        Spacer(Modifier.weight(1.1f))
        RoundControl(84.dp, onClick = onBegin) {
            PlayIcon(Modifier.size(26.dp))
        }
        Spacer(Modifier.height(16.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            GongPreviewButton(onTap = onPreviewGong)
            KoanPill(enabled = koanEnabled, onClick = onKoanToggle)
        }
        Spacer(Modifier.height(2.dp))
        Box(
            Modifier
                .height(44.dp)
                .clip(RoundedCornerShape(22.dp))
                .clickable(onClick = onAbout)
                .padding(horizontal = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            TinyLabel("about", fontSize = 10, tracking = 3f, alpha = 0.42f)
        }
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun KoanPill(enabled: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(22.dp)
    Box(
        Modifier
            .height(44.dp)
            .clip(shape)
            .background(if (enabled) Brass.copy(alpha = 0.16f) else Color.Transparent, shape)
            .border(1.dp, Brass.copy(alpha = if (enabled) 0.7f else 0.3f), shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 17.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            "KOAN",
            fontFamily = Jost,
            fontSize = 12.sp,
            letterSpacing = 2.5.sp,
            color = Cream.copy(alpha = if (enabled) 0.9f else 0.6f)
        )
    }
}

@Composable
private fun DivisionPill(divisions: Int, selected: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(22.dp)
    Box(
        Modifier
            .size(44.dp)
            .clip(shape)
            .background(if (selected) Brass.copy(alpha = 0.16f) else Color.Transparent, shape)
            .border(1.dp, Brass.copy(alpha = if (selected) 0.7f else 0.3f), shape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            divisionGlyph(divisions),
            fontFamily = Cormorant,
            fontWeight = FontWeight.SemiBold,
            fontSize = 19.sp,
            color = Cream.copy(alpha = if (selected) 0.95f else 0.65f)
        )
    }
}

@Composable
private fun PresetPill(minutes: Int, selected: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(22.dp)
    Box(
        Modifier
            .height(44.dp)
            .clip(shape)
            .background(if (selected) Brass.copy(alpha = 0.16f) else Color.Transparent, shape)
            .border(1.dp, Brass.copy(alpha = if (selected) 0.7f else 0.3f), shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 17.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            "$minutes",
            fontFamily = Jost,
            fontSize = 14.sp,
            letterSpacing = 1.sp,
            color = Cream.copy(alpha = if (selected) 0.9f else 0.65f)
        )
    }
}
