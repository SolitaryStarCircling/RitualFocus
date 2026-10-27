package com.ritualfocus.app.util

import android.content.Context
import android.net.Uri
import com.ritualfocus.app.data.AppData

object Export {

    fun json(data: AppData): String = data.toJson(pretty = true)

    fun csv(data: AppData): String {
        val names = data.habits.map { it.name }
        val dates = sortedSetOf<String>()
        data.habits.forEach { habit ->
            dates.add(habit.createdAt)
            dates.addAll(habit.checkInDates)
        }
        dates.addAll(data.focusRecords.keys)

        val header = buildString {
            append("日期")
            names.forEach { append(',').append(cell(it)) }
            append(",专注分钟")
        }

        val rows = dates.map { date ->
            buildString {
                append(date)
                data.habits.forEach { habit ->
                    append(',').append(if (habit.checkInDates.contains(date)) "1" else "0")
                }
                append(',').append(data.focusRecords[date] ?: 0)
            }
        }

        return "\uFEFF" + (listOf(header) + rows).joinToString("\n")
    }

    fun write(context: Context, uri: Uri, content: String) {
        context.contentResolver.openOutputStream(uri)?.use { stream ->
            stream.write(content.toByteArray(Charsets.UTF_8))
        }
    }

    private fun cell(value: String): String =
        if (value.contains(',') || value.contains('"') || value.contains('\n')) {
            "\"" + value.replace("\"", "\"\"") + "\""
        } else {
            value
        }
}
