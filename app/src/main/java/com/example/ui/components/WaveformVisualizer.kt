package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.PrimaryDark
import com.example.ui.theme.SecondaryDark
import kotlin.math.sin

@Composable
fun WaveformVisualizer(
    amplitudes: List<Float>,
    isRecording: Boolean,
    modifier: Modifier = Modifier,
    barCount: Int = 32,
    barWidth: Dp = 4.dp,
    barSpacing: Dp = 3.dp,
    height: Dp = 64.dp,
    primaryColor: Color = PrimaryDark,
    secondaryColor: Color = SecondaryDark
) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveform_anim")
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_offset"
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .testTag("waveform_visualizer")
    ) {
        val totalWidth = size.width
        val canvasHeight = size.height
        val barWidthPx = barWidth.toPx()
        val barSpacingPx = barSpacing.toPx()
        val totalBarSlot = barWidthPx + barSpacingPx
        val actualBars = (totalWidth / totalBarSlot).toInt().coerceAtMost(barCount).coerceAtLeast(10)

        val gradient = Brush.verticalGradient(
            colors = listOf(primaryColor, secondaryColor, primaryColor.copy(alpha = 0.6f)),
            startY = 0f,
            endY = canvasHeight
        )

        for (i in 0 until actualBars) {
            val rawAmp = amplitudes.getOrNull(amplitudes.size - actualBars + i) ?: 0.1f
            
            // Apply slight organic modulation if recording
            val animatedHeightFraction = if (isRecording) {
                val wave = ((sin(waveOffset + (i * 0.4f)) + 1f) / 2f) * 0.25f
                (rawAmp + wave).coerceIn(0.12f, 1.0f)
            } else {
                rawAmp.coerceIn(0.08f, 0.95f)
            }

            val barH = (canvasHeight * animatedHeightFraction).coerceAtLeast(6.dp.toPx())
            val left = i * totalBarSlot + (totalWidth - (actualBars * totalBarSlot)) / 2f
            val top = (canvasHeight - barH) / 2f

            drawRoundRect(
                brush = gradient,
                topLeft = Offset(left, top),
                size = Size(barWidthPx, barH),
                cornerRadius = CornerRadius(barWidthPx / 2f, barWidthPx / 2f)
            )
        }
    }
}
