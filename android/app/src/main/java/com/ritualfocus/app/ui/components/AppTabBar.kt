package com.ritualfocus.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ritualfocus.app.ui.theme.LocalRfColors
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect

private data class TabSpec(val id: String, val icon: String, val label: String)

private val Tabs = listOf(
    TabSpec("habits", "✅", "习惯"),
    TabSpec("focus", "⏱", "专注"),
    TabSpec("stats", "📊", "统计")
)

@Composable
fun AppNavBar(
    title: String,
    dark: Boolean,
    onToggleTheme: () -> Unit,
    onSettings: () -> Unit
) {
    val palette = LocalRfColors.current
    Column(Modifier.fillMaxWidth().background(palette.glassBgStrong)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .height(54.dp)
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavIconButton(if (dark) "☀\uFE0E" else "☾", onToggleTheme)
            Spacer(Modifier.weight(1f))
            Text(title, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = palette.text)
            Spacer(Modifier.weight(1f))
            NavIconButton("⚙\uFE0E", onSettings)
        }
        RfDivider()
    }
}

@Composable
private fun NavIconButton(glyph: String, onClick: () -> Unit) {
    val palette = LocalRfColors.current
    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(RoundedCornerShape(11.dp))
            .background(palette.surfaceSoft)
            .border(0.5.dp, palette.separator, RoundedCornerShape(11.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(glyph, fontSize = 17.sp, color = palette.text)
    }
}

/**
 * 悬浮胶囊底部导航：真实背景模糊（Haze），无描边。
 * Android 12 以下不支持实时模糊，会自动降级为无边框半透明底。
 */
@Composable
fun AppTabBar(
    current: String,
    onSelect: (String) -> Unit,
    hazeState: HazeState,
    modifier: Modifier = Modifier
) {
    val palette = LocalRfColors.current
    val hazeStyle = remember(palette.dark) {
        HazeStyle(
            backgroundColor = Color.Transparent,
            tint = HazeTint(
                if (palette.dark) Color.Black.copy(alpha = 0.24f) else Color.White.copy(alpha = 0.30f)
            ),
            blurRadius = 24.dp,
            noiseFactor = 0.06f,
            fallbackTint = HazeTint(
                if (palette.dark) Color(0xF21C1C24) else Color(0xF2FFFFFF)
            )
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(start = 24.dp, end = 24.dp, bottom = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .widthIn(max = 420.dp)
                .fillMaxWidth()
                .height(62.dp)
                .shadow(
                    elevation = 18.dp,
                    shape = RoundedCornerShape(31.dp),
                    clip = false,
                    ambientColor = Color.Black.copy(alpha = if (palette.dark) 0.45f else 0.10f),
                    spotColor = Color.Black.copy(alpha = if (palette.dark) 0.55f else 0.16f)
                )
                .clip(RoundedCornerShape(31.dp))
                .hazeEffect(state = hazeState, style = hazeStyle)
        ) {
            val tabWidth = maxWidth / Tabs.size
            val activeIndex = Tabs.indexOfFirst { it.id == current }.coerceAtLeast(0)
            val indicatorOffset by animateDpAsState(
                targetValue = tabWidth * activeIndex.toFloat(),
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMediumLow
                ),
                label = "indicator"
            )

            Box(
                Modifier
                    .offset(x = indicatorOffset + 5.dp, y = 6.dp)
                    .width(tabWidth - 10.dp)
                    .height(50.dp)
                    .clip(RoundedCornerShape(25.dp))
                    .background(palette.accentSoft)
            )

            Row(Modifier.fillMaxSize()) {
                Tabs.forEach { tab ->
                    TabItem(
                        tab = tab,
                        active = tab.id == current,
                        onClick = { onSelect(tab.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.TabItem(
    tab: TabSpec,
    active: Boolean,
    onClick: () -> Unit
) {
    val palette = LocalRfColors.current
    val haptic = LocalHapticFeedback.current
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = when {
            pressed -> 0.90f
            active -> 1.06f
            else -> 1f
        },
        animationSpec = spring(
            dampingRatio = 0.45f,
            stiffness = Spring.StiffnessMedium
        ),
        label = "tabScale"
    )
    val contentColor by animateColorAsState(
        targetValue = if (active) palette.accent else palette.text3,
        label = "tabColor"
    )

    Column(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .semantics {
                selected = active
                role = Role.Tab
            }
            .clickable(
                interactionSource = interaction,
                indication = null
            ) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            }
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val density = LocalDensity.current
        // emoji 字形高度与字号不一致，且 sp 会随系统字体缩放，这里按 dp 固定槽位，避免顶出胶囊
        val iconSize = with(density) { 19.dp.toSp() }
        val iconLine = with(density) { 21.dp.toSp() }
        val labelSize = with(density) { 10.5.dp.toSp() }
        val labelLine = with(density) { 12.dp.toSp() }
        Text(
            text = tab.icon,
            fontSize = iconSize,
            lineHeight = iconLine,
            style = NoFontPadding,
            maxLines = 1
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = tab.label,
            fontSize = labelSize,
            lineHeight = labelLine,
            fontWeight = if (active) FontWeight.SemiBold else FontWeight.Medium,
            color = contentColor,
            style = NoFontPadding,
            maxLines = 1
        )
    }
}

private val NoFontPadding = TextStyle(
    platformStyle = PlatformTextStyle(includeFontPadding = false)
)
