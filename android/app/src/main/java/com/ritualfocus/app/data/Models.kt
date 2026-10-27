package com.ritualfocus.app.data

import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate

data class Habit(
    val id: String,
    val name: String,
    val icon: String,
    val color: String,
    val createdAt: String,
    val checkInDates: Set<String>,
    val scheduleType: String = "daily",
    val weekdays: Set<Int> = emptySet(),
    val intervalDays: Int = 2,
    val scheduleAnchor: String = createdAt
)

data class Settings(
    val focusDuration: Int = 25,
    val breakDuration: Int = 5,
    val theme: String = "system"
)

data class FocusRuntime(
    val mode: String = "focus",
    val isRunning: Boolean = false,
    val endAt: Long = 0L,
    val remaining: Long = 1500L,
    val total: Long = 1500L
)

data class AppData(
    val habits: List<Habit> = emptyList(),
    val focusRecords: Map<String, Int> = emptyMap(),
    val settings: Settings = Settings(),
    val focusRuntime: FocusRuntime = FocusRuntime()
) {
    fun modeSeconds(mode: String): Long =
        ((if (mode == "break") settings.breakDuration else settings.focusDuration) * 60).toLong()

    fun toJson(pretty: Boolean = false): String {
        val root = JSONObject()
        root.put("version", 1)

        val habitsJson = JSONArray()
        habits.forEach { habit ->
            habitsJson.put(JSONObject().apply {
                put("id", habit.id)
                put("name", habit.name)
                put("icon", habit.icon)
                put("color", habit.color)
                put("createdAt", habit.createdAt)
                put("checkInDates", JSONArray(habit.checkInDates.sorted()))
                put("scheduleType", habit.scheduleType)
                put("weekdays", JSONArray(habit.weekdays.sorted()))
                put("intervalDays", habit.intervalDays)
                put("scheduleAnchor", habit.scheduleAnchor)
            })
        }
        root.put("habits", habitsJson)

        val recordsJson = JSONObject()
        focusRecords.toSortedMap().forEach { (key, value) -> recordsJson.put(key, value) }
        root.put("focusRecords", recordsJson)

        root.put("settings", JSONObject().apply {
            put("focusDuration", settings.focusDuration)
            put("breakDuration", settings.breakDuration)
            put("theme", settings.theme)
        })

        root.put("focusRuntime", JSONObject().apply {
            put("mode", focusRuntime.mode)
            put("isRunning", focusRuntime.isRunning)
            put("endAt", focusRuntime.endAt)
            put("remaining", focusRuntime.remaining)
            put("total", focusRuntime.total)
        })

        return if (pretty) root.toString(2) else root.toString()
    }

    companion object {
        fun fromJson(raw: String?): AppData {
            if (raw.isNullOrBlank()) return AppData()
            return try {
                val root = JSONObject(raw)

                val habits = mutableListOf<Habit>()
                val habitsJson = root.optJSONArray("habits")
                if (habitsJson != null) {
                    for (i in 0 until habitsJson.length()) {
                        val item = habitsJson.optJSONObject(i) ?: continue
                        val name = item.optString("name").trim()
                        if (name.isEmpty()) continue
                        val dates = mutableSetOf<String>()
                        val datesJson = item.optJSONArray("checkInDates")
                        if (datesJson != null) {
                            for (j in 0 until datesJson.length()) {
                                val key = datesJson.optString(j)
                                if (key.isNotBlank()) dates.add(key)
                            }
                        }
                        val createdAt = item.optString("createdAt").ifBlank {
                            LocalDate.now().toString()
                        }
                        val scheduleType = item.optString("scheduleType", "daily")
                            .takeIf { it == "weekdays" || it == "interval" } ?: "daily"
                        val weekdays = mutableSetOf<Int>()
                        val weekdaysJson = item.optJSONArray("weekdays")
                        if (weekdaysJson != null) {
                            for (k in 0 until weekdaysJson.length()) {
                                val day = weekdaysJson.optInt(k, 0)
                                if (day in 1..7) weekdays.add(day)
                            }
                        }
                        habits.add(
                            Habit(
                                id = item.optString("id").ifBlank {
                                    "h_" + System.currentTimeMillis().toString(36)
                                },
                                name = name.take(8),
                                icon = item.optString("icon").ifBlank { "🧘" },
                                color = item.optString("color").ifBlank { "#007AFF" },
                                createdAt = createdAt,
                                checkInDates = dates,
                                scheduleType = if (scheduleType == "weekdays" && weekdays.isEmpty()) {
                                    "daily"
                                } else {
                                    scheduleType
                                },
                                weekdays = weekdays,
                                intervalDays = item.optInt("intervalDays", 2).coerceIn(2, 30),
                                scheduleAnchor = item.optString("scheduleAnchor")
                                    .ifBlank { createdAt }
                            )
                        )
                    }
                }

                val records = mutableMapOf<String, Int>()
                val recordsJson = root.optJSONObject("focusRecords")
                if (recordsJson != null) {
                    val keys = recordsJson.keys()
                    while (keys.hasNext()) {
                        val key = keys.next()
                        val value = recordsJson.optInt(key, 0)
                        if (value > 0) records[key] = value
                    }
                }

                val settingsJson = root.optJSONObject("settings")
                val focusDuration = settingsJson?.optInt("focusDuration", 25) ?: 25
                val breakDuration = settingsJson?.optInt("breakDuration", 5) ?: 5
                val theme = settingsJson?.optString("theme", "system") ?: "system"
                val settings = Settings(
                    focusDuration = focusDuration.coerceIn(10, 90),
                    breakDuration = breakDuration.coerceIn(1, 90),
                    theme = if (theme == "light" || theme == "dark") theme else "system"
                )

                val runtimeJson = root.optJSONObject("focusRuntime")
                val defaultTotal = (settings.focusDuration * 60).toLong()
                val mode = if (runtimeJson?.optString("mode") == "break") "break" else "focus"
                val runtime = FocusRuntime(
                    mode = mode,
                    isRunning = runtimeJson?.optBoolean("isRunning", false) ?: false,
                    endAt = runtimeJson?.optLong("endAt", 0L) ?: 0L,
                    remaining = (runtimeJson?.optLong("remaining", defaultTotal) ?: defaultTotal)
                        .coerceAtLeast(0L),
                    total = (runtimeJson?.optLong("total", defaultTotal) ?: defaultTotal)
                        .let { if (it > 0) it else defaultTotal }
                )

                AppData(
                    habits = habits,
                    focusRecords = records,
                    settings = settings,
                    focusRuntime = runtime
                )
            } catch (e: Exception) {
                AppData()
            }
        }
    }
}
