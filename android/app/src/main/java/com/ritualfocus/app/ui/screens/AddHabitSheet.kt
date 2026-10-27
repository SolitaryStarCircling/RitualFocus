package com.ritualfocus.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.ritualfocus.app.ui.components.RfButton
import com.ritualfocus.app.ui.theme.LocalRfColors
import com.ritualfocus.app.ui.theme.habitColor

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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddHabitSheet(
    onDismiss: () -> Unit,
    onAdd: (name: String, icon: String, color: String) -> Unit
) {
    val palette = LocalRfColors.current
    var name by remember { mutableStateOf("") }
    var icon by remember { mutableStateOf(HabitIcons.first()) }
    var color by remember { mutableStateOf(HabitColors.first()) }
    val canAdd = name.trim().isNotEmpty()

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
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "添加习惯",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = palette.text
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = "取消",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = palette.accent,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onDismiss() }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
            Spacer(Modifier.height(18.dp))

            Text("名称", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = palette.text2)
            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(palette.surfaceSoft)
                    .border(1.dp, palette.separator, RoundedCornerShape(14.dp))
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
                            .then(
                                if (selected) {
                                    Modifier.border(1.5.dp, palette.accent, RoundedCornerShape(10.dp))
                                } else {
                                    Modifier
                                }
                            )
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
                            .then(
                                if (selected) {
                                    Modifier.border(2.5.dp, palette.text, CircleShape)
                                } else {
                                    Modifier
                                }
                            )
                            .clickable { color = option },
                        contentAlignment = Alignment.Center
                    ) {
                        if (selected) {
                            Text("✓", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }

            Spacer(Modifier.height(26.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                RfButton(
                    text = "取消",
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    primary = false
                )
                RfButton(
                    text = "添加",
                    onClick = { onAdd(name.trim(), icon, color) },
                    modifier = Modifier.weight(1f),
                    enabled = canAdd
                )
            }
        }
    }
}
