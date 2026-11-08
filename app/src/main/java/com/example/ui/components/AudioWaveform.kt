package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.VioletNeon
import kotlin.math.sin

@Composable
fun AudioWaveform(
    isListening: Boolean,
    soundLevel: Float, // 0.0f .. 1.0f
    modifier: Modifier = Modifier,
    barCount: Int = 28
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave_anim")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28318f, // 2*PI
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    val idlePulse by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "idle_pulse"
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
    ) {
        val width = size.width
        val height = size.height
        val barWidth = (width / (barCount * 1.6f)).coerceIn(4f, 16f)
        val spacing = (width - (barCount * barWidth)) / (barCount + 1)
        val centerY = height / 2f

        val gradient = Brush.verticalGradient(
            colors = listOf(CyanNeon, VioletNeon)
        )

        for (i in 0 until barCount) {
            val progress = i.toFloat() / barCount.toFloat()
            val wave = (sin(phase + progress * 8f) + 1f) / 2f // 0..1

            val amplitude = if (isListening) {
                // Combine audio level with wave pattern
                val dynamicLevel = (soundLevel * 0.7f) + (wave * 0.3f)
                dynamicLevel.coerceIn(0.1f, 1.0f)
            } else {
                idlePulse * (0.5f + wave * 0.5f)
            }

            val barHeight = (height * amplitude * 0.85f).coerceAtLeast(6f)
            val left = spacing + i * (barWidth + spacing)
            val top = centerY - (barHeight / 2f)

            drawRoundRect(
                brush = gradient,
                topLeft = Offset(left, top),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
            )
        }
    }
}
