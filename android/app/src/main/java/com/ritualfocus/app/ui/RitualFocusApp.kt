package com.ritualfocus.app.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ritualfocus.app.data.Habit
import com.ritualfocus.app.ui.components.AppNavBar
import com.ritualfocus.app.ui.components.AppTabBar
import com.ritualfocus.app.ui.components.BadgePopup
import com.ritualfocus.app.ui.components.BgDecor
import com.ritualfocus.app.ui.components.ConfettiOverlay
import com.ritualfocus.app.ui.components.ParticleBurstOverlay
import com.ritualfocus.app.ui.components.ToastHost
import com.ritualfocus.app.ui.screens.AddHabitSheet
import com.ritualfocus.app.ui.screens.FocusScreen
import com.ritualfocus.app.ui.screens.HabitMenuSheet
import com.ritualfocus.app.ui.screens.HabitsScreen
import com.ritualfocus.app.ui.screens.SettingsSheet
import com.ritualfocus.app.ui.screens.StatsScreen
import com.ritualfocus.app.ui.theme.LocalRfColors
import com.ritualfocus.app.util.Dates
import com.ritualfocus.app.util.Export
import com.ritualfocus.app.util.Notifications
import com.ritualfocus.app.util.Stats
import kotlinx.coroutines.delay

@Composable
fun resolveDark(theme: String): Boolean = when (theme) {
    "dark" -> true
    "light" -> false
    else -> isSystemInDarkTheme()
}

@Composable
fun RitualFocusApp(viewModel: AppViewModel) {
    val palette = LocalRfColors.current
    val context = LocalContext.current
    val data by viewModel.data.collectAsStateWithLifecycle()
    val remaining by viewModel.remaining.collectAsStateWithLifecycle()
    val dark = resolveDark(data.settings.theme)

    var tab by remember { mutableStateOf("habits") }
    var showAdd by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var menuHabit by remember { mutableStateOf<Habit?>(null) }
    var toast by remember { mutableStateOf<String?>(null) }

    var confettiToken by remember { mutableIntStateOf(0) }
    var particleToken by remember { mutableIntStateOf(0) }
    var particleCenter by remember { mutableStateOf<Offset?>(null) }
    var particleColor by remember { mutableStateOf(Color.Transparent) }
    var badgeSubtitle by remember { mutableStateOf<String?>(null) }

    val createJson = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            runCatching { Export.write(context, uri, Export.json(viewModel.data.value)) }
                .onSuccess { toast = "已导出 JSON" }
                .onFailure { toast = "导出失败" }
        }
    }
    val createCsv = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri != null) {
            runCatching { Export.write(context, uri, Export.csv(viewModel.data.value)) }
                .onSuccess { toast = "已导出 CSV" }
                .onFailure { toast = "导出失败" }
        }
    }
    val notificationPermission = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { }

    fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    fun handleCheckIn(habit: Habit, color: Color, center: Offset) {
        val checked = viewModel.toggleCheckIn(habit.id)
        if (!checked) {
            toast = "已取消打卡"
            return
        }
        val updated = viewModel.data.value.habits.firstOrNull { it.id == habit.id }
        val streak = updated?.let { Stats.currentStreak(it) } ?: 0
        particleCenter = center
        particleColor = color
        particleToken++
        when {
            streak == 7 -> {
                confettiToken++
                toast = "坚持 7 天，太棒了！"
            }
            streak >= 30 && updated != null && updated.checkInDates.size == streak -> {
                badgeSubtitle = "连续 $streak 天 · ${updated.name}"
                toast = "解锁 30 天成就！"
            }
            else -> toast = "已打卡 · 连续 $streak 天"
        }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is CycleEvent.FocusFinished -> {
                    toast = "专注完成！休息一下 ☕"
                    Notifications.notify(
                        context,
                        "专注完成",
                        "已完成 ${event.minutes} 分钟专注，休息一下"
                    )
                }
                is CycleEvent.BreakFinished -> {
                    toast = "休息结束，开始专注 🎯"
                    Notifications.notify(context, "休息结束", "开始下一个专注吧")
                }
            }
        }
    }

    LaunchedEffect(toast) {
        if (toast != null) {
            delay(1900)
            toast = null
        }
    }

    BackHandler(
        enabled = showAdd || showSettings || menuHabit != null || tab != "habits"
    ) {
        when {
            showAdd -> showAdd = false
            showSettings -> showSettings = false
            menuHabit != null -> menuHabit = null
            tab != "habits" -> tab = "habits"
        }
    }

    Box(Modifier.fillMaxSize().background(palette.bg)) {
        BgDecor()

        Column(Modifier.fillMaxSize()) {
            AppNavBar(
                title = when (tab) {
                    "focus" -> "专注"
                    "stats" -> "统计"
                    else -> "习惯"
                },
                dark = dark,
                onToggleTheme = { viewModel.setTheme(if (dark) "light" else "dark") },
                onSettings = { showSettings = true }
            )

            Crossfade(
                targetState = tab,
                modifier = Modifier.weight(1f),
                label = "page"
            ) { current ->
                when (current) {
                    "focus" -> FocusScreen(
                        data = data,
                        remaining = remaining,
                        onToggle = {
                            val wasRunning = viewModel.data.value.focusRuntime.isRunning
                            viewModel.toggleTimer()
                            if (!wasRunning) requestNotificationPermission()
                        },
                        onReset = { viewModel.resetTimer() }
                    )
                    "stats" -> StatsScreen(data = data)
                    else -> HabitsScreen(
                        data = data,
                        onAdd = { showAdd = true },
                        onOpenHabit = { menuHabit = it },
                        onCheckIn = { habit, color, center -> handleCheckIn(habit, color, center) }
                    )
                }
            }

            AppTabBar(current = tab, onSelect = { tab = it })
        }

        ToastHost(message = toast, modifier = Modifier.fillMaxSize())
        ConfettiOverlay(token = confettiToken, modifier = Modifier.fillMaxSize())
        ParticleBurstOverlay(
            center = particleCenter,
            color = particleColor,
            token = particleToken,
            modifier = Modifier.fillMaxSize()
        )
        BadgePopup(
            subtitle = badgeSubtitle,
            modifier = Modifier.fillMaxSize(),
            onDismiss = { badgeSubtitle = null }
        )

        if (showAdd) {
            AddHabitSheet(
                onDismiss = { showAdd = false },
                onAdd = { name, icon, color ->
                    viewModel.addHabit(name, icon, color)
                    showAdd = false
                    toast = "已添加习惯"
                }
            )
        }

        if (showSettings) {
            SettingsSheet(
                settings = data.settings,
                onDismiss = { showSettings = false },
                onDurations = { focus, rest -> viewModel.setDurations(focus, rest) },
                onTheme = { viewModel.setTheme(it) },
                onExportJson = {
                    createJson.launch("ritualfocus-${Dates.key()}.json")
                },
                onExportCsv = {
                    createCsv.launch("ritualfocus-${Dates.key()}.csv")
                }
            )
        }

        menuHabit?.let { habit ->
            HabitMenuSheet(
                habit = habit,
                onDelete = {
                    viewModel.deleteHabit(habit.id)
                    menuHabit = null
                    toast = "已删除习惯"
                },
                onDismiss = { menuHabit = null }
            )
        }
    }
}
