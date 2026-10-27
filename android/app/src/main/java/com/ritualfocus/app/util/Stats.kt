package com.ritualfocus.app.util

import com.ritualfocus.app.data.AppData
import com.ritualfocus.app.data.Habit
import java.time.LocalDate
import java.time.temporal.ChronoUnit

object Stats {

    fun isChecked(habit: Habit, key: String): Boolean = habit.checkInDates.contains(key)

    fun currentStreak(habit: Habit, today: LocalDate = LocalDate.now()): Int {
        if (habit.checkInDates.isEmpty()) return 0
        var cursor = if (isChecked(habit, Dates.key(today))) today else today.minusDays(1)
        if (!isChecked(habit, Dates.key(cursor))) return 0
        var count = 0
        while (isChecked(habit, Dates.key(cursor))) {
            count++
            cursor = cursor.minusDays(1)
        }
        return count
    }

    fun longestStreak(habit: Habit): Int {
        val dates = habit.checkInDates.mapNotNull { Dates.parse(it) }.sorted()
        if (dates.isEmpty()) return 0
        var best = 1
        var run = 1
        for (i in 1 until dates.size) {
            if (ChronoUnit.DAYS.between(dates[i - 1], dates[i]) == 1L) run++ else run = 1
            if (run > best) best = run
        }
        return best
    }

    fun todayDone(data: AppData): Int {
        val key = Dates.key()
        return data.habits.count { isChecked(it, key) }
    }

    fun bestCurrentStreak(data: AppData): Int = data.habits.maxOfOrNull { currentStreak(it) } ?: 0

    fun longestOverallStreak(data: AppData): Int = data.habits.maxOfOrNull { longestStreak(it) } ?: 0

    fun monthCompletionRate(data: AppData): Int {
        val today = LocalDate.now()
        val days = today.dayOfMonth
        val habitCount = data.habits.size
        if (habitCount == 0) return 0
        var done = 0
        data.habits.forEach { habit ->
            habit.checkInDates.forEach { key ->
                val date = Dates.parse(key)
                if (date != null && date.year == today.year && date.monthValue == today.monthValue) {
                    done++
                }
            }
        }
        return ((done.toDouble() / (habitCount * days)) * 100).toInt().coerceIn(0, 100)
    }

    fun totalFocusMinutes(data: AppData): Int = data.focusRecords.values.sum()

    fun habitLevel(data: AppData, date: LocalDate): Int {
        val total = data.habits.size
        if (total == 0) return 0
        val key = Dates.key(date)
        val done = data.habits.count { isChecked(it, key) }
        if (done == 0) return 0
        val ratio = done.toDouble() / total
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
}
