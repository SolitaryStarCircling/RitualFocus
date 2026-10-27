package com.ritualfocus.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ritualfocus.app.ui.theme.LocalRfColors

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    corner: Dp = 26.dp,
    contentPadding: PaddingValues = PaddingValues(18.dp),
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    content: @Composable ColumnScope.() -> Unit
) {
    val palette = LocalRfColors.current
    val shape = RoundedCornerShape(corner)
    Column(
        modifier = modifier
            .shadow(
                elevation = 10.dp,
                shape = shape,
                clip = false,
                ambientColor = Color.Black.copy(alpha = if (palette.dark) 0.35f else 0.10f),
                spotColor = Color.Black.copy(alpha = if (palette.dark) 0.45f else 0.14f)
            )
            .clip(shape)
            .background(palette.glassBg)
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    listOf(palette.glassBorder, palette.glassBorder.copy(alpha = 0.18f))
                ),
                shape = shape
            )
            .padding(contentPadding),
        horizontalAlignment = horizontalAlignment,
        content = content
    )
}

@Composable
fun RfButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    primary: Boolean = true,
    enabled: Boolean = true
) {
    val palette = LocalRfColors.current
    val shape = RoundedCornerShape(16.dp)
    val base = modifier.height(50.dp)
    Box(
        modifier = if (primary) {
            base
                .shadow(
                    elevation = 8.dp,
                    shape = shape,
                    clip = false,
                    ambientColor = palette.accentGlow,
                    spotColor = palette.accentGlow
                )
                .clip(shape)
                .background(Brush.linearGradient(listOf(palette.accent, palette.accent2)))
                .clickable(enabled = enabled) { onClick() }
        } else {
            base
                .clip(shape)
                .background(if (palette.dark) palette.surfaceSoft else palette.glassBgStrong)
                .border(1.dp, palette.glassBorder, shape)
                .clickable(enabled = enabled) { onClick() }
        },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (primary) Color.White else palette.text,
            fontSize = 15.5.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    action: @Composable (() -> Unit)? = null
) {
    val palette = LocalRfColors.current
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(width = 3.dp, height = 15.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Brush.verticalGradient(listOf(palette.accent, palette.accent2)))
        )
        Spacer(Modifier.width(8.dp))
        Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = palette.text)
        Spacer(Modifier.weight(1f))
        action?.invoke()
    }
}

@Composable
fun AddLink(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val palette = LocalRfColors.current
    Text(
        text = text,
        color = palette.accent,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp)
    )
}

@Composable
fun RfDivider(modifier: Modifier = Modifier) {
    val palette = LocalRfColors.current
    Box(modifier.fillMaxWidth().height(0.5.dp).background(palette.separator))
}
