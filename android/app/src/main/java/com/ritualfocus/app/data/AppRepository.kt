package com.ritualfocus.app.data

import android.content.Context

class AppRepository(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences("ritualfocus", Context.MODE_PRIVATE)

    fun load(): AppData = AppData.fromJson(prefs.getString(KEY, null))

    fun save(data: AppData) {
        prefs.edit().putString(KEY, data.toJson()).apply()
    }

    private companion object {
        const val KEY = "data_v1"
    }
}
