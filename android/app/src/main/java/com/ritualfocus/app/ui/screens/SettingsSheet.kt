package com.ritualfocus.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ritualfocus.app.data.Settings
import com.ritualfocus.app.ui.components.RfButton
import com.ritualfocus.app.ui.theme.LocalRfColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSheet(
    settings: Settings,
    onDismiss: () -> Unit,
    onDurations: (focus: Int, rest: Int) -> Unit,
    onTheme: (String) -> Unit,
    onExportJson: () -> Unit,
    onExportCsv: () -> Unit
) {
    val palette = LocalRfColors.current
    var focusText by remember { mutableStateOf(settings.focusDuration.toString()) }
    var restText by remember { mutableStateOf(settings.breakDuration.toString()) }

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
                .padding(bottom = 26.dp)
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "设置",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = palette.text
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = "完成",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = palette.accent,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onDismiss() }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }

            Spacer(Modifier.height(20.dp))
            Text(
                text = "专注时长",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = palette.text2
            )
            Spacer(Modifier.height(10.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(palette.surfaceSoft)
                    .padding(horizontal = 14.dp, vertical = 4.dp)
            ) {
                DurationRow("专注", focusText) { input ->
                    focusText = input
                    val value = input.toIntOrNull()
                    val rest = restText.toIntOrNull()?.takeIf { it in 1..90 } ?: settings.breakDuration
                    if (value != null && value in 10..90) onDurations(value, rest)
                }
                DurationRow("休息", restText) { input ->
                    restText = input
                    val value = input.toIntOrNull()
                    val focus = focusText.toIntOrNull()?.takeIf { it in 10..90 } ?: settings.focusDuration
                    if (value != null && value in 1..90) onDurations(focus, value)
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = "专注 10 - 90 分钟，休息 1 - 90 分钟，未开始计时时立即生效",
                fontSize = 11.5.sp,
                color = palette.text3
            )

            Spacer(Modifier.height(22.dp))
            Text(
                text = "主题",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = palette.text2
            )
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(palette.surfaceSoft)
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                ThemeOption("跟随系统", "system", settings.theme, onTheme, Modifier.weight(1f))
                ThemeOption("浅色", "light", settings.theme, onTheme, Modifier.weight(1f))
                ThemeOption("深色", "dark", settings.theme, onTheme, Modifier.weight(1f))
            }

            Spacer(Modifier.height(22.dp))
            Text(
                text = "导出数据",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = palette.text2
            )
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                RfButton(
                    text = "导出 JSON",
                    onClick = onExportJson,
                    modifier = Modifier.weight(1f),
                    primary = false
                )
                RfButton(
                    text = "导出 CSV",
                    onClick = onExportCsv,
                    modifier = Modifier.weight(1f),
                    primary = false
                )
            }
        }
    }
}

@Composable
private fun DurationRow(label: String, text: String, onTextChange: (String) -> Unit) {
    val palette = LocalRfColors.current
    Row(
        modifier = Modifier.fillMaxWidth().height(52.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 14.sp, color = palette.text)
        Spacer(Modifier.weight(1f))
        Box(
            modifier = Modifier
                .width(72.dp)
                .height(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(palette.glassBgStrong)
                .border(1.dp, palette.separator, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            BasicTextField(
                value = text,
                onValueChange = { input ->
                    onTextChange(input.filter { it.isDigit() }.take(2))
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                textStyle = TextStyle(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = palette.text,
                    textAlign = TextAlign.Center
                ),
                cursorBrush = SolidColor(palette.accent),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun ThemeOption(
    label: String,
    value: String,
    current: String,
    onTheme: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalRfColors.current
    val active = value == current
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(9.dp))
            .background(if (active) palette.surfaceSolid else androidx.compose.ui.graphics.Color.Transparent)
            .clickable { onTheme(value) }
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
