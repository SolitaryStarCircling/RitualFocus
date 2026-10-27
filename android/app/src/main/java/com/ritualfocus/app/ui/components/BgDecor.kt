package com.ritualfocus.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.ritualfocus.app.ui.theme.LocalRfColors
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun BgDecor(modifier: Modifier = Modifier) {
    val palette = LocalRfColors.current
    val transition = rememberInfiniteTransition(label = "bg")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 26000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Canvas(modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val radius = width * 0.95f

        fun drawBlob(cx: Float, cy: Float, color: Color) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(color, Color.Transparent),
                    center = Offset(cx, cy),
                    radius = radius
                ),
                radius = radius,
                center = Offset(cx, cy)
            )
        }

        drawBlob(
            cx = width * 0.12f + sin(phase) * width * 0.12f,
            cy = height * 0.02f + sin(phase * 1.4f) * 40f,
            color = palette.blob1
        )
        drawBlob(
            cx = width * 0.92f + sin(phase * 0.8f) * width * 0.1f,
            cy = height * 0.08f,
            color = palette.blob2
        )
        drawBlob(
            cx = width * 0.1f + sin(phase * 1.2f) * 40f,
            cy = height * 0.92f,
            color = palette.blob3
        )
        drawBlob(
            cx = width * 0.95f,
            cy = height * 0.9f + sin(phase) * 50f,
            color = palette.blob4
        )
    }
}
