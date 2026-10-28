package com.ritualfocus.app.util

import com.ritualfocus.app.data.AppData
import com.ritualfocus.app.data.Habit
import java.time.LocalDate
import java.time.temporal.ChronoUnit

object Stats {

    fun isChecked(habit: Habit, key: String): Boolean = habit.checkInDates.contains(key)

    /** 该习惯在指定日期是否属于计划打卡日（创建日之前不算） */
    fun isScheduled(habit: Habit, date: LocalDate): Boolean {
        val created = Dates.parse(habit.createdAt)
        if (created != null && date.isBefore(created)) return false
        return when (habit.scheduleType) {
            "weekdays" -> habit.weekdays.contains(date.dayOfWeek.value)
            "interval" -> {
                val anchor = Dates.parse(habit.scheduleAnchor)
                    ?: Dates.parse(habit.createdAt)
                    ?: return true
                if (date.isBefore(anchor)) return false
                val step = habit.intervalDays.coerceAtLeast(1)
                ChronoUnit.DAYS.between(anchor, date) % step == 0L
            }
            else -> true
        }
    }

    /** 当天没有任何计划习惯时视为休息日 */
    fun isRestDay(data: AppData, date: LocalDate): Boolean =
        data.habits.none { isScheduled(it, date) }

    /** 该日期是否已有习惯存在（用于区分「休息日」与「还没开始记录」） */
    fun hasHabitHistory(data: AppData, date: LocalDate): Boolean =
        data.habits.any { habit ->
            Dates.parse(habit.createdAt)?.let { !it.isAfter(date) } == true
        }

    fun scheduledHabits(data: AppData, date: LocalDate): List<Habit> =
        data.habits.filter { isScheduled(it, date) }

    /** 当天计划中的习惯数量 */
    fun todayScheduledTotal(data: AppData): Int =
        scheduledHabits(data, LocalDate.now()).size

    /** 当天已完成（仅统计计划中的习惯，额外打卡不计入） */
    fun todayDone(data: AppData): Int {
        val today = LocalDate.now()
        val key = Dates.key(today)
        return scheduledHabits(data, today).count { isChecked(it, key) }
    }

    /** 非计划日额外打卡的习惯数 */
    fun todayExtraCheckIns(data: AppData): Int {
        val today = LocalDate.now()
        val key = Dates.key(today)
        return data.habits.count { !isScheduled(it, today) && isChecked(it, key) }
    }

    fun currentStreak(habit: Habit, today: LocalDate = LocalDate.now()): Int {
        if (habit.checkInDates.isEmpty()) return 0
        val created = Dates.parse(habit.createdAt)
        var cursor = today
        if (isScheduled(habit, cursor) && !isChecked(habit, Dates.key(cursor))) {
            cursor = cursor.minusDays(1)
        }
        var count = 0
        var guard = 0
        while (guard < 3700) {
            if (created != null && cursor.isBefore(created)) break
            if (isScheduled(habit, cursor)) {
                if (isChecked(habit, Dates.key(cursor))) count++ else break
            }
            cursor = cursor.minusDays(1)
            guard++
        }
        return count
    }

    fun longestStreak(habit: Habit): Int {
        val start = Dates.parse(habit.createdAt)
            ?: habit.checkInDates.mapNotNull { Dates.parse(it) }.minOrNull()
            ?: return 0
        val today = LocalDate.now()
        var cursor = start
        var best = 0
        var run = 0
        while (!cursor.isAfter(today)) {
            if (isScheduled(habit, cursor)) {
                if (isChecked(habit, Dates.key(cursor))) {
                    run++
                    if (run > best) best = run
                } else {
                    run = 0
                }
            }
            cursor = cursor.plusDays(1)
        }
        return best
    }

    fun bestCurrentStreak(data: AppData): Int = data.habits.maxOfOrNull { currentStreak(it) } ?: 0

    fun longestOverallStreak(data: AppData): Int = data.habits.maxOfOrNull { longestStreak(it) } ?: 0

    fun monthCompletionRate(data: AppData): Int {
        val today = LocalDate.now()
        var scheduled = 0
        var done = 0
        for (day in 1..today.dayOfMonth) {
            val date = today.withDayOfMonth(day)
            data.habits.forEach { habit ->
                if (isScheduled(habit, date)) {
                    scheduled++
                    if (isChecked(habit, Dates.key(date))) done++
                }
            }
        }
        if (scheduled == 0) return 0
        return ((done.toDouble() / scheduled) * 100).toInt().coerceIn(0, 100)
    }

    fun totalFocusMinutes(data: AppData): Int = data.focusRecords.values.sum()

    /** 当天计划习惯的完成比例，映射到 0..4 色阶 */
    fun habitLevel(data: AppData, date: LocalDate): Int {
        val scheduled = scheduledHabits(data, date)
        if (scheduled.isEmpty()) return 0
        val key = Dates.key(date)
        val done = scheduled.count { isChecked(it, key) }
        if (done == 0) return 0
        val ratio = done.toDouble() / scheduled.size
        return when {
            ratio < 0.34 -> 1
            ratio < 0.67 -> 2
            ratio < 1.0 -> 3
            else -> 4
        }
    }

    fun focusLevel(minutes: Int): Int = when {
        minutes <= 0 -> 0
        minutes <= 25 -> 1
        minutes <= 50 -> 2
        minutes <= 75 -> 3
        else -> 4
    }

    fun scheduleLabel(habit: Habit): String = when (habit.scheduleType) {
        "weekdays" -> {
            val days = habit.weekdays.sorted().joinToString("/") { weekdayShort(it) }
            "每周 $days"
        }
        "interval" -> "每 ${habit.intervalDays} 天"
        else -> "每天"
    }

    fun weekdayShort(value: Int): String =
        listOf("一", "二", "三", "四", "五", "六", "日")
            .getOrElse(value - 1) { "?" }
}
