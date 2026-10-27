package com.ritualfocus.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ritualfocus.app.ui.theme.LocalRfColors

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

@Composable
fun AppTabBar(current: String, onSelect: (String) -> Unit) {
    val palette = LocalRfColors.current
    Column(
        Modifier
            .fillMaxWidth()
            .background(palette.glassBgStrong)
            .navigationBarsPadding()
    ) {
        RfDivider()
        Row(Modifier.fillMaxWidth().height(60.dp)) {
            TabItem(current, "habits", "✅", "习惯", onSelect)
            TabItem(current, "focus", "⏱", "专注", onSelect)
            TabItem(current, "stats", "📊", "统计", onSelect)
        }
    }
}

@Composable
private fun RowScope.TabItem(
    current: String,
    id: String,
    icon: String,
    label: String,
    onSelect: (String) -> Unit
) {
    val palette = LocalRfColors.current
    val active = current == id
    Column(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .clickable { onSelect(id) },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(icon, fontSize = 20.sp)
        Spacer(Modifier.height(3.dp))
        Text(
            text = label,
            fontSize = 10.5.sp,
            fontWeight = if (active) FontWeight.SemiBold else FontWeight.Medium,
            color = if (active) palette.accent else palette.text3
        )
    }
}
