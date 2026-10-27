package com.ritualfocus.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ritualfocus.app.data.AppData
import com.ritualfocus.app.ui.components.GlassCard
import com.ritualfocus.app.ui.components.ProgressRing
import com.ritualfocus.app.ui.components.RfButton
import com.ritualfocus.app.ui.theme.LocalRfColors
import com.ritualfocus.app.util.Dates

@Composable
fun FocusScreen(
    data: AppData,
    remaining: Long,
    onToggle: () -> Unit,
    onReset: () -> Unit
) {
    val palette = LocalRfColors.current
    val scroll = rememberScrollState()
    val runtime = data.focusRuntime
    val isBreak = runtime.mode == "break"
    val total = if (runtime.total > 0L) runtime.total else data.modeSeconds(runtime.mode)
    val progress = if (total > 0L) remaining.toFloat() / total else 0f
    val ringColor = if (isBreak) palette.breakAccent else palette.accent
    val todayMinutes = data.focusRecords[Dates.key()] ?: 0

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scroll)
            .padding(horizontal = 16.dp, vertical = 18.dp)
    ) {
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            corner = 28.dp,
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 26.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isBreak) "休息模式" else "专注模式",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.2.sp,
                color = ringColor,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        if (isBreak) palette.breakAccent.copy(alpha = 0.15f) else palette.accentSoft
                    )
                    .border(0.5.dp, palette.separator, RoundedCornerShape(20.dp))
                    .padding(horizontal = 16.dp, vertical = 7.dp)
            )

            Spacer(Modifier.height(26.dp))
            ProgressRing(
                progress = progress,
                modifier = Modifier.size(248.dp),
                strokeWidth = 18.dp,
                progressColor = ringColor
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = Dates.clock(remaining),
                        style = TextStyle(
                            brush = Brush.verticalGradient(listOf(palette.text, palette.text2)),
                            fontSize = 56.sp,
                            fontWeight = FontWeight.Light,
                            letterSpacing = (-2).sp
                        ),
                        maxLines = 1
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = when {
                            runtime.isRunning && isBreak -> "休息中"
                            runtime.isRunning -> "专注中"
                            remaining >= total -> "准备开始"
                            else -> "已暂停"
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 1.2.sp,
                        color = palette.text3
                    )
                }
            }

            Spacer(Modifier.height(26.dp))
            Text(
                text = "今日已专注 $todayMinutes 分钟",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = palette.text2
            )
            Spacer(Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val label = when {
                    runtime.isRunning -> "暂停"
                    remaining < total && remaining > 0 -> "继续"
                    else -> "开始"
                }
                RfButton(
                    text = label,
                    onClick = onToggle,
                    modifier = Modifier.weight(1f)
                )
                RfButton(
                    text = "重置",
                    onClick = onReset,
                    modifier = Modifier.weight(1f),
                    primary = false
                )
            }
        }

        Spacer(Modifier.height(18.dp))
        Text(
            text = "专注结束后自动进入休息，休息结束回到专注",
            fontSize = 11.5.sp,
            lineHeight = 18.sp,
            color = palette.text3,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)
        )
    }
}
