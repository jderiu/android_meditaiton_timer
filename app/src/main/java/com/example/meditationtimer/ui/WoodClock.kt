package com.example.meditationtimer.ui

import android.graphics.BlurMaskFilter
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import com.example.meditationtimer.ui.theme.Cormorant
import com.example.meditationtimer.ui.theme.MottleDark
import com.example.meditationtimer.ui.theme.StreakMineral
import com.example.meditationtimer.ui.theme.StreakPale
import com.example.meditationtimer.ui.theme.StreakUmber
import com.example.meditationtimer.ui.theme.Wood0
import com.example.meditationtimer.ui.theme.Wood1
import com.example.meditationtimer.ui.theme.Wood2
import com.example.meditationtimer.ui.theme.Wood3
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random

/**
 * The mango-wood dial. One full turn of the hand is the whole session;
 * a strong tick marks each division (halves, thirds, quarters, fifths,
 * eighths) — no numerals, the marks suffice. An optional koan is carved
 * into the lower face.
 *
 * Geometry is authored in a 340-unit space (the design mockup's viewBox)
 * and scaled to the composable's size. The wood itself never changes, so
 * it is rendered once into a bitmap; only the hand is drawn per frame.
 */
@Composable
fun WoodClock(
    divisions: Int,
    koan: String?,
    progress: () -> Float,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier.aspectRatio(1f), contentAlignment = Alignment.Center) {
        val side = min(maxWidth, maxHeight)
        val u = side.value / 340f // dp per dial unit

        // Static layer: cast shadow + the wooden disc with ticks.
        Box(
            Modifier
                .size(side)
                .drawWithCache {
                    val s = size.minDimension / 340f
                    val dial = renderDialBitmap(size, divisions)
                    val shadowCenter = Offset(size.width / 2f, size.height / 2f + 18f * s)
                    val shadowBrush = Brush.radialGradient(
                        0f to Color.Black.copy(alpha = 0.45f),
                        0.75f to Color.Black.copy(alpha = 0.12f),
                        1f to Color.Transparent,
                        center = shadowCenter,
                        radius = 205f * s
                    )
                    onDrawBehind {
                        drawCircle(shadowBrush, radius = 205f * s, center = shadowCenter)
                        drawImage(dial)
                    }
                }
        )

        // The koan, carved into the lower face: a light chisel-edge copy
        // under an ink copy, both in the wood's own tones.
        if (koan != null) {
            val density = LocalDensity.current
            val koanStyle = TextStyle(
                fontFamily = Cormorant,
                fontWeight = FontWeight.SemiBold,
                fontStyle = FontStyle.Italic,
                fontSize = with(density) { (14.5f * u).dp.toSp() },
                lineHeight = with(density) { (17f * u).dp.toSp() },
                letterSpacing = with(density) { (0.6f * u).dp.toSp() },
                textAlign = TextAlign.Center
            )
            Text(
                koan,
                style = koanStyle,
                color = Color(0x8CF7DDAB),
                modifier = Modifier
                    .width((205 * u).dp)
                    .offset(y = (63.8f * u).dp)
            )
            Text(
                koan,
                style = koanStyle,
                color = Color(0xE046290F),
                modifier = Modifier
                    .width((205 * u).dp)
                    .offset(y = (63f * u).dp)
            )
        }

        // Dynamic layer: the black steel hand and its shadow.
        Box(
            Modifier
                .size(side)
                .drawWithCache {
                    val s = size.minDimension / 340f
                    val c = Offset(size.width / 2f, size.height / 2f)
                    val hand = Path().apply {
                        moveTo(c.x, c.y - 120f * s)
                        lineTo(c.x + 2.8f * s, c.y - 2f * s)
                        lineTo(c.x, c.y + 22f * s)
                        lineTo(c.x - 2.8f * s, c.y - 2f * s)
                        close()
                        addOval(Rect(center = Offset(c.x, c.y + 27f * s), radius = 4.2f * s))
                    }
                    val handAndroid = hand.asAndroidPath()
                    val shadowPaint = android.graphics.Paint().apply {
                        isAntiAlias = true
                        color = android.graphics.Color.argb(82, 26, 15, 5)
                        maskFilter = BlurMaskFilter(3.5f * s, BlurMaskFilter.Blur.NORMAL)
                    }
                    val handBrush = Brush.linearGradient(
                        0f to Color(0xFF38342E),
                        0.55f to Color(0xFF171411),
                        1f to Color(0xFF060504),
                        start = Offset(c.x, c.y - 120f * s),
                        end = Offset(c.x, c.y + 31f * s)
                    )
                    onDrawBehind {
                        val deg = progress().coerceIn(0f, 1f) * 360f
                        translate(2.4f * s, 4.2f * s) {
                            rotate(deg, pivot = c) {
                                drawContext.canvas.nativeCanvas.drawPath(handAndroid, shadowPaint)
                            }
                        }
                        rotate(deg, pivot = c) {
                            drawPath(hand, brush = handBrush)
                            drawPath(hand, color = Color.White.copy(alpha = 0.08f), style = Stroke(0.4f * s))
                        }
                        drawCircle(Color(0xFF15100A), radius = 6.6f * s, center = c)
                        drawCircle(Color(0x33FFE0AA), radius = 6.6f * s, center = c, style = Stroke(0.8f * s))
                        drawCircle(Color.White.copy(alpha = 0.28f), radius = 1f * s, center = c + Offset(-1.2f * s, -1.4f * s))
                    }
                }
        )
    }
}

private fun renderDialBitmap(size: Size, divisions: Int): ImageBitmap {
    val w = ceil(size.width).toInt().coerceAtLeast(1)
    val h = ceil(size.height).toInt().coerceAtLeast(1)
    val bitmap = ImageBitmap(w, h)
    CanvasDrawScope().draw(
        Density(1f),
        LayoutDirection.Ltr,
        Canvas(bitmap),
        Size(w.toFloat(), h.toFloat())
    ) {
        drawDial(divisions)
    }
    return bitmap
}

private fun DrawScope.drawDial(divisions: Int) {
    val s = size.minDimension / 340f
    val c = center

    // One solid disc of mango wood.
    drawCircle(
        Brush.radialGradient(
            0f to Wood0, 0.45f to Wood1, 0.8f to Wood2, 1f to Wood3,
            center = Offset(142.8f * s, 122.4f * s),
            radius = 255f * s
        ),
        radius = 165f * s,
        center = c
    )

    // Grain: long streaks with mineral lines and pale bands, plus fine mottle.
    val disc = Path().apply { addOval(Rect(center = c, radius = 165f * s)) }
    clipPath(disc) {
        val rnd = Random(7)
        fun wavy(yUnits: Float, widthUnits: Float, color: Color, alpha: Float) {
            val y = yUnits * s
            fun j() = (rnd.nextFloat() * 10f - 5f) * s
            val p = Path().apply {
                moveTo(-10f * s, y + j())
                cubicTo(60f * s, y + j(), 120f * s, y + j(), 180f * s, y + j())
                cubicTo(240f * s, y + j(), 300f * s, y + j(), 350f * s, y + j())
            }
            drawPath(p, color.copy(alpha = alpha), style = Stroke(widthUnits * s, cap = StrokeCap.Round))
        }
        repeat(13) { i ->
            wavy(26f + i * 22.5f + rnd.nextFloat() * 8f - 4f, 2f + rnd.nextFloat() * 5f, StreakUmber, 0.08f + rnd.nextFloat() * 0.06f)
        }
        repeat(3) { i ->
            wavy(70f + i * 85f + rnd.nextFloat() * 20f, 1f + rnd.nextFloat() * 0.4f, StreakMineral, 0.24f + rnd.nextFloat() * 0.08f)
        }
        repeat(4) { i ->
            wavy(45f + i * 75f + rnd.nextFloat() * 16f, 5f + rnd.nextFloat() * 4f, StreakPale, 0.07f + rnd.nextFloat() * 0.04f)
        }
        repeat(560) {
            val x = rnd.nextFloat() * 340f
            val y = rnd.nextFloat() * 340f
            val len = 3f + rnd.nextFloat() * 6f
            drawLine(
                MottleDark.copy(alpha = 0.04f + rnd.nextFloat() * 0.05f),
                Offset(x * s, y * s),
                Offset((x + len) * s, (y + rnd.nextFloat() * 1.5f - 0.75f) * s),
                strokeWidth = (0.7f + rnd.nextFloat() * 0.9f) * s
            )
        }
        repeat(360) {
            val x = rnd.nextFloat() * 340f
            val y = rnd.nextFloat() * 340f
            val len = 3f + rnd.nextFloat() * 5f
            drawLine(
                StreakPale.copy(alpha = 0.03f + rnd.nextFloat() * 0.04f),
                Offset(x * s, y * s),
                Offset((x + len) * s, (y + rnd.nextFloat() * 1.2f - 0.6f) * s),
                strokeWidth = (0.6f + rnd.nextFloat() * 0.8f) * s
            )
        }
    }

    // Turned bezel: darker stain, groove, bevel edges, soft sheen.
    drawCircle(Color(0x615E3A1A), radius = 152.5f * s, center = c, style = Stroke(26f * s))
    drawCircle(Color(0xB33A230E), radius = 138.6f * s, center = c, style = Stroke(1.6f * s))
    drawCircle(Color(0x40FFE1AA), radius = 140.4f * s, center = c, style = Stroke(1f * s))
    drawCircle(
        Brush.radialGradient(
            0f to Color(0x52FFE9BC), 0.7f to Color(0x1AFFE9BC), 1f to Color(0x00FFE9BC),
            center = Offset(156.4f * s, 136f * s),
            radius = 204f * s
        ),
        radius = 136f * s,
        center = c
    )
    drawCircle(Color(0x33462C12), radius = 133f * s, center = c, style = Stroke(8f * s))
    drawCircle(Color(0x1A462C12), radius = 128f * s, center = c, style = Stroke(6f * s))
    drawCircle(Color(0x8C28180A), radius = 164.7f * s, center = c, style = Stroke(1.8f * s))
    drawCircle(Color(0x47FFE4AF), radius = 162.8f * s, center = c, style = Stroke(1.1f * s))
    drawArc(
        color = Color(0x1AFFF2D2),
        startAngle = -140f,
        sweepAngle = 99.3f,
        useCenter = false,
        topLeft = Offset(c.x - 150f * s, c.y - 150f * s),
        size = Size(300f * s, 300f * s),
        style = Stroke(26f * s, cap = StrokeCap.Round)
    )

    // Fine tick ring with a strong mark at each division.
    val perDivision = (60f / divisions).roundToInt().coerceAtLeast(1)
    val tickCount = divisions * perDivision
    val minor = Path()
    val major = Path()
    for (i in 0 until tickCount) {
        val a = Math.toRadians(i * 360.0 / tickCount - 90.0)
        val dx = cos(a).toFloat()
        val dy = sin(a).toFloat()
        val isMajor = i % perDivision == 0
        val p = if (isMajor) major else minor
        val inner = if (isMajor) 112f else 118f
        p.moveTo(c.x + dx * 126f * s, c.y + dy * 126f * s)
        p.lineTo(c.x + dx * inner * s, c.y + dy * inner * s)
    }
    drawPath(minor, Color(0x8C38210C), style = Stroke(1f * s, cap = StrokeCap.Round))
    drawPath(major, Color(0xF2241505), style = Stroke(2.4f * s, cap = StrokeCap.Round))
}
