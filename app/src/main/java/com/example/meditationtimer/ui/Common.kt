package com.example.meditationtimer.ui

import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.meditationtimer.ui.theme.Brass
import com.example.meditationtimer.ui.theme.ControlIvory
import com.example.meditationtimer.ui.theme.Cream
import com.example.meditationtimer.ui.theme.Jost

fun durationLabel(minutes: Int): String =
    if (minutes == 1) "1 minute" else "$minutes minutes"

/** Small caps, letterspaced label in Jost, the app's secondary voice. */
@Composable
fun TinyLabel(
    text: String,
    fontSize: Int,
    tracking: Float,
    alpha: Float,
    modifier: Modifier = Modifier
) {
    Text(
        text.uppercase(),
        modifier = modifier,
        fontFamily = Jost,
        fontWeight = FontWeight.Normal,
        fontSize = fontSize.sp,
        letterSpacing = tracking.sp,
        color = Cream.copy(alpha = alpha)
    )
}

/** Round control button: thin brass ring on a faint warm fill. */
@Composable
fun RoundControl(
    diameter: Dp,
    onClick: () -> Unit,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        Modifier
            .size(diameter)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    0f to Brass.copy(alpha = 0.16f),
                    1f to Brass.copy(alpha = 0.04f)
                ),
                CircleShape
            )
            .border(1.dp, Brass.copy(alpha = 0.55f), CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
        content = content
    )
}

@Composable
fun TextControl(text: String, onClick: () -> Unit) {
    Box(
        Modifier
            .height(44.dp)
            .clip(RoundedCornerShape(22.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        TinyLabel(text, fontSize = 12, tracking = 3.5f, alpha = 0.68f)
    }
}

/** Icon-only gong button; tapping it previews the gong sound. */
@Composable
fun GongPreviewButton(onTap: () -> Unit) {
    Box(
        Modifier
            .size(44.dp)
            .clip(CircleShape)
            .clickable(onClick = onTap),
        contentAlignment = Alignment.Center
    ) {
        GongIcon(Modifier.size(18.dp))
    }
}

@Composable
fun GongIcon(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val s = size.minDimension / 17f
        val col = ControlIvory.copy(alpha = 0.8f)
        drawLine(col, Offset(2.5f * s, 3f * s), Offset(14.5f * s, 3f * s), 1.2f * s, StrokeCap.Round)
        drawLine(col, Offset(4.2f * s, 3f * s), Offset(5.6f * s, 5.4f * s), 1f * s, StrokeCap.Round)
        drawLine(col, Offset(12.8f * s, 3f * s), Offset(11.4f * s, 5.4f * s), 1f * s, StrokeCap.Round)
        drawCircle(col, 5.4f * s, Offset(8.5f * s, 9.6f * s), style = Stroke(1.2f * s))
        drawCircle(col, 1.8f * s, Offset(8.5f * s, 9.6f * s), style = Stroke(1f * s))
    }
}

@Composable
fun PauseIcon(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val sx = size.width / 18f
        val sy = size.height / 22f
        val corner = CornerRadius(2.3f * sx)
        drawRoundRect(ControlIvory, Offset(2f * sx, 1f * sy), Size(4.6f * sx, 20f * sy), corner)
        drawRoundRect(ControlIvory, Offset(11.4f * sx, 1f * sy), Size(4.6f * sx, 20f * sy), corner)
    }
}

@Composable
fun PlayIcon(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val p = Path().apply {
            moveTo(w * 0.22f, h * 0.06f)
            lineTo(w * 0.22f, h * 0.94f)
            lineTo(w * 0.92f, h * 0.5f)
            close()
        }
        drawPath(p, ControlIvory)
        drawPath(p, ControlIvory, style = Stroke(w * 0.12f, join = StrokeJoin.Round))
    }
}

@Composable
fun CheckIcon(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val p = Path().apply {
            moveTo(w * 0.14f, h * 0.54f)
            lineTo(w * 0.4f, h * 0.8f)
            lineTo(w * 0.86f, h * 0.24f)
        }
        drawPath(p, ControlIvory, style = Stroke(w * 0.11f, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

/** Soft candlelight behind the clock, breathing on a slow cycle. */
@Composable
fun BreathingGlow(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "breath")
    val glowAlpha by transition.animateFloat(
        initialValue = 0.55f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(4500, easing = EaseInOut), RepeatMode.Reverse),
        label = "breathAlpha"
    )
    Box(
        modifier
            .graphicsLayer { alpha = glowAlpha }
            .drawBehind {
                val r = size.minDimension * 0.68f
                drawCircle(
                    Brush.radialGradient(
                        0f to Color(0x21FFC478),
                        0.62f to Color(0x00FFC478),
                        1f to Color.Transparent,
                        center = center,
                        radius = r
                    ),
                    radius = r,
                    center = center
                )
            }
    )
}
