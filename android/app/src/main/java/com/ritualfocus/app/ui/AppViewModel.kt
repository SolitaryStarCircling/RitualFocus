package com.ritualfocus.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ritualfocus.app.data.AppData
import com.ritualfocus.app.data.AppRepository
import com.ritualfocus.app.data.FocusRuntime
import com.ritualfocus.app.data.Habit
import com.ritualfocus.app.util.Dates
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID
import kotlin.math.ceil

sealed interface CycleEvent {
    data class FocusFinished(val minutes: Int) : CycleEvent
    data object BreakFinished : CycleEvent
}

class AppViewModel(app: Application) : AndroidViewModel(app) {

    private val repository = AppRepository(app)

    private val _data = MutableStateFlow(repository.load())
    val data: StateFlow<AppData> = _data.asStateFlow()

    private val _remaining = MutableStateFlow(0L)
    val remaining: StateFlow<Long> = _remaining.asStateFlow()

    private val _events = MutableSharedFlow<CycleEvent>(extraBufferCapacity = 4)
    val events: SharedFlow<CycleEvent> = _events.asSharedFlow()

    private var ticker: Job? = null

    init {
        restoreRuntime()
        ticker = viewModelScope.launch {
            while (isActive) {
                tick()
                delay(250)
            }
        }
    }

    /* ---------------- data plumbing ---------------- */

    private fun commit(next: AppData) {
        _data.value = next
        repository.save(next)
    }

    private fun update(transform: (AppData) -> AppData) {
        commit(transform(_data.value))
    }

    /* ---------------- habits ---------------- */

    fun addHabit(name: String, icon: String, color: String) {
        val habit = Habit(
            id = "h_" + UUID.randomUUID().toString().take(8),
            name = name.take(8),
            icon = icon,
            color = color,
            createdAt = Dates.key(),
            checkInDates = emptySet()
        )
        update { it.copy(habits = it.habits + habit) }
    }

    fun deleteHabit(id: String) {
        update { data -> data.copy(habits = data.habits.filterNot { it.id == id }) }
    }

    /** 返回操作后是否处于已打卡状态 */
    fun toggleCheckIn(id: String): Boolean {
        var checked = false
        val today = Dates.key()
        update { data ->
            data.copy(habits = data.habits.map { habit ->
                if (habit.id != id) {
                    habit
                } else if (habit.checkInDates.contains(today)) {
                    checked = false
                    habit.copy(checkInDates = habit.checkInDates - today)
                } else {
                    checked = true
                    habit.copy(checkInDates = habit.checkInDates + today)
                }
            })
        }
        return checked
    }

    /* ---------------- settings ---------------- */

    fun setTheme(theme: String) {
        update { it.copy(settings = it.settings.copy(theme = theme)) }
    }

    fun setDurations(focusMinutes: Int, breakMinutes: Int) {
        val focus = focusMinutes.coerceIn(10, 90)
        val rest = breakMinutes.coerceIn(1, 90)
        update { data ->
            val runtime = data.focusRuntime
            val total = if (runtime.isRunning) {
                runtime.total
            } else {
                val minutes = if (runtime.mode == "break") rest else focus
                (minutes * 60).toLong()
            }
            data.copy(
                settings = data.settings.copy(focusDuration = focus, breakDuration = rest),
                focusRuntime = runtime.copy(
                    total = total,
                    remaining = if (runtime.isRunning) runtime.remaining else total
                )
            )
        }
        if (!_data.value.focusRuntime.isRunning) {
            _remaining.value = _data.value.focusRuntime.remaining
        }
    }

    /* ---------------- timer ---------------- */

    fun toggleTimer() {
        if (_data.value.focusRuntime.isRunning) pauseTimer() else startTimer()
    }

    fun startTimer() {
        val runtime = _data.value.focusRuntime
        val remaining = if (runtime.remaining <= 0L) runtime.total else runtime.remaining
        val endAt = System.currentTimeMillis() + remaining * 1000
        update {
            it.copy(
                focusRuntime = runtime.copy(
                    isRunning = true,
                    endAt = endAt,
                    remaining = remaining
                )
            )
        }
        _remaining.value = remaining
    }

    fun pauseTimer() {
        val runtime = _data.value.focusRuntime
        val left = if (runtime.isRunning) secondsLeft(runtime.endAt) else runtime.remaining
        update {
            it.copy(
                focusRuntime = runtime.copy(
                    isRunning = false,
                    endAt = 0L,
                    remaining = left
                )
            )
        }
        _remaining.value = left
    }

    fun resetTimer() {
        val runtime = _data.value.focusRuntime
        val total = _data.value.modeSeconds(runtime.mode)
        update {
            it.copy(
                focusRuntime = runtime.copy(
                    isRunning = false,
                    endAt = 0L,
                    remaining = total,
                    total = total
                )
            )
        }
        _remaining.value = total
    }

    /* ---------------- internals ---------------- */

    private fun restoreRuntime() {
        val runtime = _data.value.focusRuntime
        if (runtime.isRunning) {
            val left = secondsLeft(runtime.endAt)
            if (left <= 0L) {
                finishCycle()
            } else {
                _remaining.value = left
            }
        } else {
            _remaining.value = runtime.remaining
        }
    }

    private fun tick() {
        val runtime = _data.value.focusRuntime
        if (!runtime.isRunning) return
        val left = secondsLeft(runtime.endAt)
        if (left <= 0L) {
            finishCycle()
        } else if (left != _remaining.value) {
            _remaining.value = left
        }
    }

    private fun secondsLeft(endAt: Long): Long =
        ceil((endAt - System.currentTimeMillis()) / 1000.0).toLong().coerceAtLeast(0L)

    private fun finishCycle() {
        val current = _data.value
        val runtime = current.focusRuntime
        val completedAt = if (runtime.endAt > 0L) {
            LocalDate.ofInstant(Instant.ofEpochMilli(runtime.endAt), ZoneId.systemDefault())
        } else {
            LocalDate.now()
        }

        val wasFocus = runtime.mode == "focus"
        val nextMode = if (wasFocus) "break" else "focus"
        val nextTotal = current.modeSeconds(nextMode)

        var records = current.focusRecords
        var finishedMinutes = 0
        if (wasFocus) {
            finishedMinutes = current.settings.focusDuration
            val key = Dates.key(completedAt)
            records = records.toMutableMap().apply {
                this[key] = (this[key] ?: 0) + finishedMinutes
            }
        }

        commit(
            current.copy(
                focusRecords = records,
                focusRuntime = FocusRuntime(
                    mode = nextMode,
                    isRunning = false,
                    endAt = 0L,
                    remaining = nextTotal,
                    total = nextTotal
                )
            )
        )
        _remaining.value = nextTotal
        _events.tryEmit(
            if (wasFocus) CycleEvent.FocusFinished(finishedMinutes) else CycleEvent.BreakFinished
        )
    }
}
