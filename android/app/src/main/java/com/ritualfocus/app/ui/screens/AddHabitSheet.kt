package com.ritualfocus.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ritualfocus.app.data.Habit
import com.ritualfocus.app.ui.theme.LocalRfColors
import com.ritualfocus.app.ui.theme.habitColor
import java.time.LocalDate

val HabitIcons = listOf(
    "🧘", "📚", "🏃", "💧", "🌙", "✍️", "🎧", "🥗",
    "💪", "🧠", "🌱", "☀️", "🚴", "🎨", "🧹", "🛏️",
    "📝", "🎯", "💊", "☕", "🐕", "🎸", "🧴", "🪥",
    "📖", "💡", "🌊", "🍎", "🕯️", "🧘‍♂️", "🏊", "🎮"
)

val HabitColors = listOf(
    "#007AFF", "#5856D6", "#AF52DE", "#FF2D55", "#FF9500", "#FFCC00",
    "#34C759", "#00C7BE", "#30B0C7", "#A2845E", "#FF6482", "#8E8E93"
)

data class ScheduleDraft(
    val type: String = "daily",
    val weekdays: Set<Int> = emptySet(),
    val intervalDays: Int = 2
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddHabitSheet(
    onDismiss: () -> Unit,
    onAdd: (name: String, icon: String, color: String, schedule: ScheduleDraft) -> Unit
) {
    val palette = LocalRfColors.current
    var name by remember { mutableStateOf("") }
    var icon by remember { mutableStateOf(HabitIcons.first()) }
    var color by remember { mutableStateOf(HabitColors.first()) }
    var schedule by remember {
        mutableStateOf(
            ScheduleDraft(type = "daily", weekdays = setOf(LocalDate.now().dayOfWeek.value))
        )
    }
    val canAdd = name.trim().isNotEmpty() &&
        (schedule.type != "weekdays" || schedule.weekdays.isNotEmpty())

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = palette.glassBgStrong
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
        ) {
            SheetHeader(
                title = "添加习惯",
                confirmEnabled = canAdd,
                onCancel = onDismiss,
                onConfirm = {
                    if (canAdd) {
                        onAdd(name.trim(), icon, color, schedule)
                    }
                }
            )
            Spacer(Modifier.height(18.dp))

            Text("名称", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = palette.text2)
            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(palette.surfaceSoft)
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                BasicTextField(
                    value = name,
                    onValueChange = { name = it.take(8) },
                    singleLine = true,
                    textStyle = TextStyle(fontSize = 15.sp, color = palette.text),
                    cursorBrush = SolidColor(palette.accent),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Done
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                if (name.isEmpty()) {
                    Text("例如：冥想", fontSize = 15.sp, color = palette.text3)
                }
            }

            Spacer(Modifier.height(20.dp))
            SchedulePicker(schedule = schedule, onChange = { schedule = it })

            Spacer(Modifier.height(20.dp))
            Text("图标", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = palette.text2)
            Spacer(Modifier.height(8.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                maxItemsInEachRow = 8,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HabitIcons.forEach { option ->
                    val selected = option == icon
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selected) palette.accentSoft else palette.surfaceSoft)
                            .clickable { icon = option },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(option, fontSize = 19.sp)
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            Text("颜色", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = palette.text2)
            Spacer(Modifier.height(10.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                maxItemsInEachRow = 6,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                HabitColors.forEach { option ->
                    val selected = option == color
                    val tint = habitColor(option)
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(tint)
                            .clickable { color = option },
                        contentAlignment = Alignment.Center
                    ) {
                        if (selected) {
                            Text(
                                "✓",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ScheduleSheet(
    habit: Habit,
    onDismiss: () -> Unit,
    onSave: (schedule: ScheduleDraft) -> Unit
) {
    val palette = LocalRfColors.current
    var schedule by remember(habit.id) {
        mutableStateOf(
            ScheduleDraft(
                type = habit.scheduleType,
                weekdays = habit.weekdays,
                intervalDays = habit.intervalDays
            )
        )
    }
    val canSave = schedule.type != "weekdays" || schedule.weekdays.isNotEmpty()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = palette.glassBgStrong
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 26.dp)
        ) {
            SheetHeader(
                title = "修改打卡计划",
                confirmEnabled = canSave,
                onCancel = onDismiss,
                onConfirm = { if (canSave) onSave(schedule) }
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "${habit.icon} ${habit.name}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = palette.text2
            )
            Spacer(Modifier.height(18.dp))
            SchedulePicker(schedule = schedule, onChange = { schedule = it })
            Spacer(Modifier.height(8.dp))
            Text(
                text = "调整计划不会删除已有打卡记录",
                fontSize = 11.5.sp,
                color = palette.text3
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SchedulePicker(schedule: ScheduleDraft, onChange: (ScheduleDraft) -> Unit) {
    val palette = LocalRfColors.current

    Text("打卡计划", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = palette.text2)
    Spacer(Modifier.height(8.dp))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(palette.surfaceSoft)
            .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        ScheduleSegment("每天", "daily", schedule.type, Modifier.weight(1f)) {
            onChange(schedule.copy(type = "daily"))
        }
        ScheduleSegment("每周", "weekdays", schedule.type, Modifier.weight(1f)) {
            val days = schedule.weekdays.ifEmpty { setOf(LocalDate.now().dayOfWeek.value) }
            onChange(schedule.copy(type = "weekdays", weekdays = days))
        }
        ScheduleSegment("间隔", "interval", schedule.type, Modifier.weight(1f)) {
            onChange(schedule.copy(type = "interval"))
        }
    }

    Spacer(Modifier.height(12.dp))
    when (schedule.type) {
        "weekdays" -> {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val labels = listOf("一", "二", "三", "四", "五", "六", "日")
                labels.forEachIndexed { index, label ->
                    val day = index + 1
                    val selected = schedule.weekdays.contains(day)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selected) palette.accent else palette.surfaceSoft)
                            .clickable {
                                val next = if (selected) {
                                    schedule.weekdays - day
                                } else {
                                    schedule.weekdays + day
                                }
                                onChange(schedule.copy(weekdays = next))
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (selected) Color.White else palette.text2
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = "只在选中的星期计入统计，其余日子算休息",
                fontSize = 11.5.sp,
                color = palette.text3
            )
        }

        "interval" -> {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RoundStepButton("−", enabled = schedule.intervalDays > 2) {
                    onChange(schedule.copy(intervalDays = schedule.intervalDays - 1))
                }
                Text(
                    text = "每 ${schedule.intervalDays} 天一次",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = palette.text,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
                RoundStepButton("+", enabled = schedule.intervalDays < 30) {
                    onChange(schedule.copy(intervalDays = schedule.intervalDays + 1))
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = buildString {
                    append("从今天起，每 ${schedule.intervalDays} 天打卡一次")
                    append("（例如每 2 天 = 练一天休一天）")
                },
                fontSize = 11.5.sp,
                color = palette.text3
            )
        }

        else -> Text(
            text = "每天都需要打卡",
            fontSize = 11.5.sp,
            color = palette.text3
        )
    }
}

@Composable
private fun ScheduleSegment(
    label: String,
    value: String,
    current: String,
    modifier: Modifier = Modifier,
    onSelect: () -> Unit
) {
    val palette = LocalRfColors.current
    val active = value == current
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(9.dp))
            .background(if (active) palette.surfaceSolid else Color.Transparent)
            .clickable { onSelect() }
            .padding(vertical = 9.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (active) palette.text else palette.text2
        )
    }
}

@Composable
private fun RoundStepButton(label: String, enabled: Boolean, onClick: () -> Unit) {
    val palette = LocalRfColors.current
    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(palette.surfaceSoft)
            .clickable(enabled = enabled) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            color = if (enabled) palette.text else palette.text3
        )
    }
}

@Composable
fun SheetHeader(
    title: String,
    confirmEnabled: Boolean,
    onCancel: () -> Unit,
    onConfirm: () -> Unit
) {
    val palette = LocalRfColors.current
    Box(Modifier.fillMaxWidth()) {
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = palette.text,
            modifier = Modifier.align(Alignment.Center)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "取消",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = palette.text2,
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onCancel() }
                    .padding(horizontal = 6.dp, vertical = 6.dp)
            )
            Text(
                text = "确定",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (confirmEnabled) palette.accent else palette.text3,
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .clickable(enabled = confirmEnabled) { onConfirm() }
                    .padding(horizontal = 6.dp, vertical = 6.dp)
            )
        }
    }
}
