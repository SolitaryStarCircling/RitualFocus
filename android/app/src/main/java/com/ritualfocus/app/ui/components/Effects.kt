package com.ritualfocus.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun ConfettiOverlay(token: Int, modifier: Modifier = Modifier) {
    if (token == 0) return
    val progress = remember(token) { Animatable(0f) }
    LaunchedEffect(token) {
        progress.snapTo(0f)
        progress.animateTo(1f, animationSpec = tween(2600, easing = LinearEasing))
    }
    val colors = remember {
        listOf(
            Color(0xFFFF3B30), Color(0xFFFF9500), Color(0xFFFFCC00),
            Color(0xFF34C759), Color(0xFF007AFF), Color(0xFFAF52DE)
        )
    }
    Canvas(modifier.fillMaxSize()) {
        if (progress.value >= 1f) return@Canvas
        val random = Random(token)
        repeat(48) {
            val startX = random.nextFloat() * size.width
            val delayFactor = random.nextFloat() * 0.3f
            val speed = 0.8f + random.nextFloat() * 0.5f
            val t = (((progress.value - delayFactor) / (1f - 0.3f)) * speed).coerceIn(0f, 1f)
            if (t > 0f) {
                val y = -40.dp.toPx() + t * (size.height + 80.dp.toPx())
                val x = startX + sin(t * 6f + it) * 18.dp.toPx()
                rotate(degrees = t * 720f, pivot = Offset(x, y)) {
                    drawRect(
                        color = colors[it % colors.size].copy(alpha = 1f - t * 0.4f),
                        topLeft = Offset(x - 4.dp.toPx(), y - 7.dp.toPx()),
                        size = Size(8.dp.toPx(), 14.dp.toPx())
                    )
                }
            }
        }
    }
}

@Composable
fun ParticleBurstOverlay(
    center: Offset?,
    color: Color,
    token: Int,
    modifier: Modifier = Modifier
) {
    if (center == null || token == 0) return
    val progress = remember(token) { Animatable(0f) }
    LaunchedEffect(token) {
        progress.snapTo(0f)
        progress.animateTo(1f, animationSpec = tween(650, easing = LinearEasing))
    }
    Canvas(modifier.fillMaxSize()) {
        if (progress.value >= 1f) return@Canvas
        val random = Random(token)
        val t = progress.value
        repeat(14) {
            val angle = random.nextFloat() * (2f * PI.toFloat())
            val distance = (34 + random.nextFloat() * 52).dp.toPx()
            val radius = (3f + random.nextFloat() * 2f) * (1f - t * 0.5f)
            drawCircle(
                color = color.copy(alpha = (1f - t) * 0.9f),
                radius = radius.dp.toPx(),
                center = Offset(
                    center.x + cos(angle) * distance * t,
                    center.y + sin(angle) * distance * t
                )
            )
        }
    }
}

@Composable
fun BadgePopup(subtitle: String?, modifier: Modifier = Modifier, onDismiss: () -> Unit) {
    if (subtitle == null) return
    val scale = remember { Animatable(0.6f) }
    LaunchedEffect(subtitle) {
        scale.animateTo(1f, animationSpec = spring(dampingRatio = 0.55f, stiffness = 380f))
        delay(2200)
        scale.animateTo(0.6f, animationSpec = tween(200))
        onDismiss()
    }
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Box(
            Modifier
                .graphicsLayer {
                    scaleX = scale.value
                    scaleY = scale.value
                    alpha = ((scale.value - 0.6f) / 0.4f).coerceIn(0f, 1f)
                }
                .clip(RoundedCornerShape(24.dp))
                .background(Brush.linearGradient(listOf(Color(0xFFFFD700), Color(0xFFFF9500))))
                .padding(horizontal = 30.dp, vertical = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("🏆", fontSize = 48.sp)
                Spacer(Modifier.height(6.dp))
                Text("30 天成就", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                Spacer(Modifier.height(4.dp))
                Text(subtitle, color = Color.White.copy(alpha = 0.9f), fontSize = 12.sp)
            }
        }
    }
}
