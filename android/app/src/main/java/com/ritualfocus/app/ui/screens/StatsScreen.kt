package com.ritualfocus.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ritualfocus.app.data.AppData
import com.ritualfocus.app.ui.components.FocusYearHeatmap
import com.ritualfocus.app.ui.components.GlassCard
import com.ritualfocus.app.ui.components.HeatDetailPopup
import com.ritualfocus.app.ui.components.HeatLegend
import com.ritualfocus.app.ui.components.HabitMonthHeatmap
import com.ritualfocus.app.ui.components.HabitWeekHeatmap
import com.ritualfocus.app.ui.components.SectionHeader
import com.ritualfocus.app.ui.components.StatCard
import com.ritualfocus.app.ui.theme.LocalRfColors
import com.ritualfocus.app.util.Dates
import com.ritualfocus.app.util.Stats
import java.time.LocalDate

private data class HeatDetail(val date: LocalDate, val kind: String, val minutes: Int)

@Composable
fun StatsScreen(data: AppData) {
    val palette = LocalRfColors.current
    val scroll = rememberScrollState()
    var monthView by remember { mutableStateOf(true) }
    var detail by remember { mutableStateOf<HeatDetail?>(null) }

    val todayMinutes = data.focusRecords[Dates.key()] ?: 0
    val done = Stats.todayDone(data)
    val total = Stats.todayScheduledTotal(data)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scroll)
            .padding(horizontal = 16.dp, vertical = 16.dp)
            .padding(bottom = 112.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatCard(
                value = "$todayMinutes",
                label = "今日专注(分)",
                modifier = Modifier.weight(1f)
            )
            StatCard(
                value = "$done/$total",
                label = "今日打卡",
                modifier = Modifier.weight(1f)
            )
            StatCard(
                value = "${Stats.longestOverallStreak(data)}",
                label = "最长连续",
                modifier = Modifier.weight(1f)
            )
            StatCard(
                value = "${Stats.monthCompletionRate(data)}%",
                label = "本月完成率",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(22.dp))
        SectionHeader(title = "打卡记录", action = {
            ViewToggle(monthView) { monthView = it }
        })
        Spacer(Modifier.height(12.dp))
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            corner = 22.dp,
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 18.dp, bottom = 14.dp)
        ) {
            if (monthView) {
                HabitMonthHeatmap(
                    data = data,
                    onCellClick = { date -> detail = HeatDetail(date, "habit", 0) },
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                HabitWeekHeatmap(
                    data = data,
                    onCellClick = { date -> detail = HeatDetail(date, "habit", 0) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(Modifier.height(12.dp))
            HeatLegend()
        }

        Spacer(Modifier.height(22.dp))
        SectionHeader(title = "专注记录", action = {
            Text(
                text = "共 ${Stats.totalFocusMinutes(data)} 分钟",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = palette.accent
            )
        })
        Spacer(Modifier.height(12.dp))
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            corner = 22.dp,
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 18.dp, bottom = 14.dp)
        ) {
            FocusYearHeatmap(
                data = data,
                onCellClick = { date, minutes -> detail = HeatDetail(date, "focus", minutes) },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            HeatLegend(cellSize = 11.dp)
        }
    }

    detail?.let { selected ->
        HeatDetailPopup(
            date = selected.date,
            kind = selected.kind,
            data = data,
            minutes = selected.minutes,
            onDismiss = { detail = null }
        )
    }
}

@Composable
private fun ViewToggle(month: Boolean, onChange: (Boolean) -> Unit) {
    val palette = LocalRfColors.current
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(palette.surfaceSoft)
            .padding(2.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        ToggleItem("月", month, palette.surfaceSolid, palette.text, palette.text2) { onChange(true) }
        ToggleItem("周", !month, palette.surfaceSolid, palette.text, palette.text2) { onChange(false) }
    }
}

@Composable
private fun ToggleItem(
    label: String,
    active: Boolean,
    activeBg: Color,
    activeText: Color,
    idleText: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (active) activeBg else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 5.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (active) activeText else idleText
        )
    }
}
