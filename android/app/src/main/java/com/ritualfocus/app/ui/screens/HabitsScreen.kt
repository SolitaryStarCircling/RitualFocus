package com.ritualfocus.app.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ritualfocus.app.data.AppData
import com.ritualfocus.app.data.Habit
import com.ritualfocus.app.ui.components.GlassCard
import com.ritualfocus.app.ui.components.RfButton
import com.ritualfocus.app.ui.components.RfDivider
import com.ritualfocus.app.ui.components.SectionHeader
import com.ritualfocus.app.ui.components.AddLink
import com.ritualfocus.app.ui.theme.LocalRfColors
import com.ritualfocus.app.ui.theme.habitColor
import com.ritualfocus.app.util.Stats

@Composable
fun HabitsScreen(
    data: AppData,
    onAdd: () -> Unit,
    onOpenHabit: (Habit) -> Unit,
    onCheckIn: (Habit, Color, androidx.compose.ui.geometry.Offset) -> Unit
) {
    val palette = LocalRfColors.current
    val scroll = rememberScrollState()
    val today = java.time.LocalDate.now()
    val done = Stats.todayDone(data)
    val total = Stats.todayScheduledTotal(data)
    val extra = Stats.todayExtraCheckIns(data)
    val streak = Stats.bestCurrentStreak(data)
    val target = if (total > 0) done.toFloat() / total else 0f
    val barFraction by animateFloatAsState(target, label = "todayBar")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scroll)
            .padding(horizontal = 16.dp, vertical = 16.dp)
            .padding(bottom = 112.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            corner = 26.dp,
            contentPadding = PaddingValues(18.dp)
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
                Column {
                    Text(
                        text = "今天",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp,
                        color = palette.text3
                    )
                    Spacer(Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "$done",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = palette.text
                        )
                        Text(
                            text = when {
                                total > 0 -> " / $total 已完成"
                                else -> " 已完成 · 今天休息"
                            },
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = palette.text2,
                            modifier = Modifier.padding(bottom = 3.dp)
                        )
                    }
                    if (extra > 0) {
                        Text(
                            text = "额外打卡 $extra 项",
                            fontSize = 10.5.sp,
                            color = palette.text3,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
                Spacer(Modifier.weight(1f))
                Text(
                    text = "连续 $streak 天",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = palette.accent,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(palette.accentSoft)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
            Spacer(Modifier.height(14.dp))
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(palette.surfaceSoft)
            ) {
                Box(
                    Modifier
                        .fillMaxWidth(barFraction)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Brush.linearGradient(listOf(palette.accent, palette.accent2)))
                )
            }
        }

        Spacer(Modifier.height(22.dp))
        SectionHeader(title = "我的习惯", action = {
            AddLink(text = "+ 添加", onClick = onAdd)
        })
        Spacer(Modifier.height(12.dp))

        if (data.habits.isEmpty()) {
            EmptyHabits(onAdd)
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                data.habits.forEach { habit ->
                    val color = habitColor(habit.color)
                    HabitCard(
                        habit = habit,
                        checked = Stats.isChecked(habit, com.ritualfocus.app.util.Dates.key(today)),
                        scheduledToday = Stats.isScheduled(habit, today),
                        scheduleLabel = Stats.scheduleLabel(habit),
                        streak = Stats.currentStreak(habit),
                        onOpen = { onOpenHabit(habit) },
                        onCheckIn = { center -> onCheckIn(habit, color, center) }
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyHabits(onAdd: () -> Unit) {
    val palette = LocalRfColors.current
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        corner = 26.dp,
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 42.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("🪴", fontSize = 52.sp)
        Spacer(Modifier.height(14.dp))
        Text(
            text = "还没有习惯",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = palette.text
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "从一个小目标开始，\n每天前进一步，习惯会自己长出来。",
            fontSize = 13.sp,
            color = palette.text2,
            textAlign = TextAlign.Center,
            lineHeight = 21.sp
        )
        Spacer(Modifier.height(20.dp))
        RfButton(
            text = "添加第一个习惯",
            onClick = onAdd,
            modifier = Modifier.widthIn(min = 172.dp)
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HabitCard(
    habit: Habit,
    checked: Boolean,
    scheduledToday: Boolean,
    scheduleLabel: String,
    streak: Int,
    onOpen: () -> Unit,
    onCheckIn: (androidx.compose.ui.geometry.Offset) -> Unit
) {
    val palette = LocalRfColors.current
    val color = habitColor(habit.color)
    var center by remember { mutableStateOf(androidx.compose.ui.geometry.Offset.Zero) }
    val extra = checked && !scheduledToday

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        corner = 24.dp,
        contentPadding = PaddingValues(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(onClick = onOpen, onLongClick = onOpen),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .size(50.dp)
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(15.dp),
                        clip = false,
                        ambientColor = color.copy(alpha = 0.45f),
                        spotColor = color.copy(alpha = 0.45f)
                    )
                    .clip(RoundedCornerShape(15.dp))
                    .background(color.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Text(habit.icon, fontSize = 24.sp)
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = habit.name,
                        fontSize = 15.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.text,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (streak >= 7) {
                        Spacer(Modifier.width(6.dp))
                        StreakBadge(if (streak >= 30) "30 天" else "7 天")
                    }
                }
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "$streak",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = color
                    )
                    Text(
                        text = " 天连续",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = palette.text2
                    )
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    text = buildString {
                        append(scheduleLabel)
                        if (!scheduledToday) append(" · 今日休息")
                        if (extra) append(" · 额外打卡")
                    },
                    fontSize = 10.5.sp,
                    color = palette.text3,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.width(10.dp))
            CheckInButton(
                checked = checked,
                color = color,
                modifier = Modifier.onGloballyPositioned { center = it.boundsInRoot().center }
            ) { onCheckIn(center) }
        }
    }
}

@Composable
private fun StreakBadge(label: String) {
    Box(
        Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Brush.linearGradient(listOf(Color(0xFFFFD700), Color(0xFFFF9500))))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = label,
            fontSize = 9.5.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.3.sp,
            color = Color.White
        )
    }
}

@Composable
private fun CheckInButton(
    checked: Boolean,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val palette = LocalRfColors.current
    val shape = CircleShape
    Box(
        modifier = modifier
            .size(44.dp)
            .clip(shape)
            .then(
                if (checked) {
                    Modifier.background(
                        Brush.linearGradient(listOf(color, lerp(color, Color.White, 0.2f)))
                    )
                } else {
                    Modifier
                        .background(palette.surfaceSoft)
                        .border(2.dp, color.copy(alpha = 0.35f), shape)
                }
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (checked) "✓" else "",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = if (checked) Color.White else color
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitMenuSheet(
    habit: Habit,
    onEditSchedule: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    val palette = LocalRfColors.current
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = palette.glassBgStrong
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 20.dp)
        ) {
            Text(
                text = "${habit.icon} ${habit.name}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = palette.text2,
                modifier = Modifier.padding(vertical = 6.dp)
            )
            RfDivider()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onEditSchedule() }
                    .padding(horizontal = 6.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🗓\uFE0E", fontSize = 18.sp)
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "修改打卡计划",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = palette.text
                )
            }
            RfDivider()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onDelete() }
                    .padding(horizontal = 6.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🗑\uFE0E", fontSize = 18.sp, color = palette.danger)
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "删除这个习惯",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = palette.danger
                )
            }
            RfDivider()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onDismiss() }
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "取消",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = palette.text2
                )
            }
        }
    }
}
