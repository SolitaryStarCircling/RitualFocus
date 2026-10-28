package com.ritualfocus.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import com.ritualfocus.app.data.AppData
import com.ritualfocus.app.data.Habit
import com.ritualfocus.app.ui.theme.LocalRfColors
import com.ritualfocus.app.ui.theme.RfPalette
import com.ritualfocus.app.util.Dates
import com.ritualfocus.app.util.Stats
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.YearMonth

data class HeatCell(
    val date: LocalDate?,
    val level: Int,
    val isFuture: Boolean,
    val isToday: Boolean,
    val isRest: Boolean = false
)

private val HabitCell = 10.dp
private val FocusCell = 11.dp
private val HabitGap = 2.dp

@Composable
fun HeatCellView(cell: HeatCell, size: Dp, onClick: () -> Unit) {
    val palette = LocalRfColors.current
    val shape = RoundedCornerShape(3.dp)
    Box(
        Modifier
            .size(size)
            .clip(shape)
            .background(
                if (cell.isRest) {
                    palette.surfaceSoft.copy(alpha = if (palette.dark) 0.5f else 0.7f)
                } else {
                    palette.heat[cell.level.coerceIn(0, palette.heat.lastIndex)]
                }
            )
            .then(if (cell.isFuture) Modifier.alpha(0.3f) else Modifier)
            .then(if (cell.isToday) Modifier.border(1.5.dp, palette.accent, shape) else Modifier)
            .clickable(enabled = cell.date != null && !cell.isFuture) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (cell.isRest) {
            Box(
                Modifier
                    .size(3.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(palette.text3.copy(alpha = 0.45f))
            )
        }
    }
}

@Composable
fun HabitMonthHeatmap(
    data: AppData,
    onCellClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalRfColors.current
    val scroll = rememberScrollState()
    val today = LocalDate.now()
    LaunchedEffect(Unit) {
        delay(90)
        scroll.scrollTo(scroll.maxValue)
    }
    Row(
        modifier = modifier.horizontalScroll(scroll),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        val current = YearMonth.from(today)
        for (i in 11 downTo 0) {
            val month = current.minusMonths(i.toLong())
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${month.monthValue}月",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = palette.text3
                )
                Spacer(Modifier.height(6.dp))
                Column(verticalArrangement = Arrangement.spacedBy(HabitGap)) {
                    monthRows(month, data, today).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(HabitGap)) {
                            row.forEach { cell ->
                                val date = cell?.date
                                if (cell == null || date == null) {
                                    Spacer(Modifier.size(HabitCell))
                                } else {
                                    HeatCellView(cell, HabitCell) { onCellClick(date) }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HabitWeekHeatmap(
    data: AppData,
    onCellClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalRfColors.current
    val scroll = rememberScrollState()
    val today = LocalDate.now()
    val thisMonday = remember { Dates.weekStart(today) }
    LaunchedEffect(Unit) {
        delay(90)
        scroll.scrollTo(scroll.maxValue)
    }
    Row(
        modifier = modifier.horizontalScroll(scroll),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        for (w in 13 downTo 0) {
            val start = thisMonday.minusWeeks(w.toLong())
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Column(verticalArrangement = Arrangement.spacedBy(HabitGap)) {
                    for (d in 0..6L) {
                        val date = start.plusDays(d)
                        HeatCellView(
                            cell = HeatCell(
                                date = date,
                                level = Stats.habitLevel(data, date),
                                isFuture = date.isAfter(today),
                                isToday = date == today,
                                isRest = Stats.isRestDay(data, date) && Stats.hasHabitHistory(data, date)
                            ),
                            size = HabitCell
                        ) { onCellClick(date) }
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = Dates.short(start),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Medium,
                    color = palette.text3
                )
            }
        }
    }
}

@Composable
fun FocusYearHeatmap(
    data: AppData,
    onCellClick: (LocalDate, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val scroll = rememberScrollState()
    val today = LocalDate.now()
    val start = remember(today) {
        val raw = today.minusDays(364)
        raw.minusDays(((raw.dayOfWeek.value + 6) % 7).toLong())
    }
    val weeks = remember(start, today) {
        val days = mutableListOf<LocalDate>()
        var cursor = start
        while (!cursor.isAfter(today)) {
            days.add(cursor)
            cursor = cursor.plusDays(1)
        }
        days.chunked(7)
    }
    LaunchedEffect(Unit) {
        delay(90)
        scroll.scrollTo(scroll.maxValue)
    }
    Row(
        modifier = modifier.horizontalScroll(scroll),
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        weeks.forEach { week ->
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                week.forEach { date ->
                    val minutes = data.focusRecords[Dates.key(date)] ?: 0
                    HeatCellView(
                        cell = HeatCell(
                            date = date,
                            level = Stats.focusLevel(minutes),
                            isFuture = date.isAfter(today),
                            isToday = date == today
                        ),
                        size = FocusCell
                    ) { onCellClick(date, minutes) }
                }
            }
        }
    }
}

@Composable
fun HeatLegend(modifier: Modifier = Modifier, cellSize: Dp = HabitCell) {
    val palette = LocalRfColors.current
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("少", fontSize = 10.5.sp, color = palette.text3)
        Spacer(Modifier.width(5.dp))
        palette.heat.forEach { color ->
            Box(
                Modifier
                    .size(cellSize)
                    .clip(RoundedCornerShape(3.dp))
                    .background(color)
            )
            Spacer(Modifier.width(5.dp))
        }
        Text("多", fontSize = 10.5.sp, color = palette.text3)
    }
}

@Composable
fun HeatDetailPopup(
    date: LocalDate,
    kind: String,
    data: AppData,
    minutes: Int,
    onDismiss: () -> Unit
) {
    val palette = LocalRfColors.current
    LaunchedEffect(date, kind) {
        delay(3600)
        onDismiss()
    }
    Popup(alignment = Alignment.Center, onDismissRequest = onDismiss) {
        GlassCard(
            modifier = Modifier.width(248.dp),
            corner = 16.dp,
            contentPadding = PaddingValues(14.dp)
        ) {
            Text(
                text = "${date.monthValue}月${date.dayOfMonth}日 · ${Dates.weekdayCn(date)}",
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
                color = palette.text
            )
            Spacer(Modifier.height(8.dp))
            if (kind == "focus") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("⏱", fontSize = 13.sp)
                    Spacer(Modifier.width(6.dp))
                    Text("专注时长", fontSize = 12.sp, color = palette.text2)
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = if (minutes > 0) "$minutes 分钟" else "无记录",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (minutes > 0) palette.text else palette.text3
                    )
                }
            } else {
                val scheduled = Stats.scheduledHabits(data, date)
                val extras = data.habits.filter {
                    !Stats.isScheduled(it, date) && it.checkInDates.contains(Dates.key(date))
                }
                if (scheduled.isEmpty() && extras.isEmpty()) {
                    Text(
                        text = if (Stats.hasHabitHistory(data, date)) {
                            "休息日 · 今天没有安排"
                        } else {
                            "无安排"
                        },
                        fontSize = 12.sp,
                        color = palette.text3
                    )
                } else {
                    scheduled.forEach { habit ->
                        val checked = habit.checkInDates.contains(Dates.key(date))
                        HabitDetailRow(habit, if (checked) "✓" else "—", checked, palette)
                        Spacer(Modifier.height(2.dp))
                    }
                    extras.forEach { habit ->
                        HabitDetailRow(habit, "额外", true, palette)
                        Spacer(Modifier.height(2.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun HabitDetailRow(
    habit: Habit,
    trailing: String,
    checked: Boolean,
    palette: RfPalette
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(habit.icon, fontSize = 13.sp)
        Spacer(Modifier.width(6.dp))
        Text(
            text = habit.name,
            fontSize = 12.sp,
            color = if (checked) palette.text else palette.text3,
            modifier = Modifier.weight(1f),
            maxLines = 1
        )
        Text(
            text = trailing,
            fontSize = if (trailing == "额外") 10.sp else 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (checked) palette.success else palette.text3
        )
    }
}

private fun monthRows(month: YearMonth, data: AppData, today: LocalDate): List<List<HeatCell?>> {
    val first = month.atDay(1)
    val leading = (first.dayOfWeek.value + 6) % 7
    val cells = mutableListOf<HeatCell?>()
    repeat(leading) { cells.add(null) }
    for (day in 1..month.lengthOfMonth()) {
        val date = month.atDay(day)
        cells.add(
            HeatCell(
                date = date,
                level = Stats.habitLevel(data, date),
                isFuture = date.isAfter(today),
                isToday = date == today,
                isRest = Stats.isRestDay(data, date) && Stats.hasHabitHistory(data, date)
            )
        )
    }
    return cells.chunked(7).map { chunk -> chunk + List(7 - chunk.size) { null } }
}
