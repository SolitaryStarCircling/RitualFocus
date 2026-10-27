package com.ritualfocus.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class RfPalette(
    val dark: Boolean,
    val bg: Color,
    val text: Color,
    val text2: Color,
    val text3: Color,
    val accent: Color,
    val accent2: Color,
    val accentSoft: Color,
    val accentGlow: Color,
    val glassBg: Color,
    val glassBgStrong: Color,
    val glassBorder: Color,
    val surfaceSoft: Color,
    val surfaceSolid: Color,
    val separator: Color,
    val danger: Color,
    val success: Color,
    val heat: List<Color>,
    val blob1: Color,
    val blob2: Color,
    val blob3: Color,
    val blob4: Color,
    val breakAccent: Color,
    val breakAccent2: Color
)

val LightPalette = RfPalette(
    dark = false,
    bg = Color(0xFFEEF2F8),
    text = Color(0xFF1C1C1E),
    text2 = Color(0xFF6E6E78),
    text3 = Color(0xFFA0A0AA),
    accent = Color(0xFF007AFF),
    accent2 = Color(0xFF4A9EFF),
    accentSoft = Color(0x1F007AFF),
    accentGlow = Color(0x59007AFF),
    glassBg = Color(0x94FFFFFF),
    glassBgStrong = Color(0xE0FFFFFF),
    glassBorder = Color(0xD9FFFFFF),
    surfaceSoft = Color(0x1A787880),
    surfaceSolid = Color(0xF2FFFFFF),
    separator = Color(0x1F3C3C43),
    danger = Color(0xFFFF3B30),
    success = Color(0xFF34C759),
    heat = listOf(
        Color(0xFFE6E9EF),
        Color(0xFF9BE9A8),
        Color(0xFF40C463),
        Color(0xFF2EA14C),
        Color(0xFF166B39)
    ),
    blob1 = Color(0x6B589CFF),
    blob2 = Color(0x59966CFF),
    blob3 = Color(0x47FF9866),
    blob4 = Color(0x5266D6FF),
    breakAccent = Color(0xFFFF9500),
    breakAccent2 = Color(0xFFFFB143)
)

val DarkPalette = RfPalette(
    dark = true,
    bg = Color(0xFF0A0A12),
    text = Color(0xFFF5F5F7),
    text2 = Color(0xFF98989D),
    text3 = Color(0xFF6A6A70),
    accent = Color(0xFF0A84FF),
    accent2 = Color(0xFF5AA6FF),
    accentSoft = Color(0x260A84FF),
    accentGlow = Color(0x730A84FF),
    glassBg = Color(0x9E1C1C24),
    glassBgStrong = Color(0xEF1C1C24),
    glassBorder = Color(0x1AFFFFFF),
    surfaceSoft = Color(0x33787880),
    surfaceSolid = Color(0x1FFFFFFF),
    separator = Color(0x1FFFFFFF),
    danger = Color(0xFFFF453A),
    success = Color(0xFF30D158),
    heat = listOf(
        Color(0x1FFFFFFF),
        Color(0xFF0D5233),
        Color(0xFF0F7A44),
        Color(0xFF16A34A),
        Color(0xFF34D399)
    ),
    blob1 = Color(0x474080FF),
    blob2 = Color(0x3D8C5CFF),
    blob3 = Color(0x2EFF8250),
    blob4 = Color(0x3350C8FF),
    breakAccent = Color(0xFFFFA726),
    breakAccent2 = Color(0xFFFFC06A)
)

val LocalRfColors = staticCompositionLocalOf { LightPalette }

fun habitColor(hex: String): Color = runCatching { Color(android.graphics.Color.parseColor(hex)) }
    .getOrElse { LightPalette.accent }

@Composable
fun RitualFocusTheme(darkTheme: Boolean, content: @Composable () -> Unit) {
    val palette = if (darkTheme) DarkPalette else LightPalette
    CompositionLocalProvider(LocalRfColors provides palette) {
        MaterialTheme(
            colorScheme = if (darkTheme) {
                darkColorScheme(
                    primary = palette.accent,
                    background = palette.bg,
                    surface = palette.bg,
                    onBackground = palette.text,
                    onSurface = palette.text
                )
            } else {
                lightColorScheme(
                    primary = palette.accent,
                    background = palette.bg,
                    surface = palette.bg,
                    onBackground = palette.text,
                    onSurface = palette.text
                )
            },
            typography = Typography(),
            content = content
        )
    }
}
