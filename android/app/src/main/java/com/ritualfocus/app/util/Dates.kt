package com.ritualfocus.app.util

import java.time.LocalDate

object Dates {

    fun key(date: LocalDate = LocalDate.now()): String = date.toString()

    fun parse(key: String): LocalDate? = runCatching { LocalDate.parse(key) }.getOrNull()

    fun short(date: LocalDate): String = "${date.monthValue}/${date.dayOfMonth}"

    fun weekdayCn(date: LocalDate): String = when (date.dayOfWeek.value) {
        1 -> "周一"
        2 -> "周二"
        3 -> "周三"
        4 -> "周四"
        5 -> "周五"
        6 -> "周六"
        else -> "周日"
    }

    /** 本周周一 */
    fun weekStart(date: LocalDate = LocalDate.now()): LocalDate =
        date.minusDays(((date.dayOfWeek.value + 6) % 7).toLong())

    fun clock(seconds: Long): String {
        val safe = seconds.coerceAtLeast(0)
        return "%02d:%02d".format(safe / 60, safe % 60)
    }
}
