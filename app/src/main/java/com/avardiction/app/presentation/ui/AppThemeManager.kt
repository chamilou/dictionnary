package com.avardiction.app.presentation.ui

import android.content.Context

enum class AppThemeMode(val code: String) {
    SYSTEM("system"),
    LIGHT("light"),
    DARK("dark");

    companion object {
        fun fromCode(code: String?): AppThemeMode {
            return entries.firstOrNull { it.code == code } ?: SYSTEM
        }
    }
}

object AppThemeManager {
    private const val PREFERENCES_NAME = "dictionary_preferences"
    private const val KEY_THEME_MODE = "theme_mode"

    fun getSavedThemeMode(context: Context): AppThemeMode {
        val code = context
            .getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
            .getString(KEY_THEME_MODE, null)
        return AppThemeMode.fromCode(code)
    }

    fun saveThemeMode(context: Context, themeMode: AppThemeMode) {
        context
            .getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_THEME_MODE, themeMode.code)
            .apply()
    }
}
