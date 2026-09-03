package com.example.ui.theme

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

enum class AppThemeMode(val title: String) {
    LIGHT("Light"),
    DARK("Dark")
}

object ThemeManager {
    private const val PREFS_NAME = "sevenhooks_theme_prefs"
    private const val KEY_THEME_MODE = "app_theme_mode"

    var currentThemeMode by mutableStateOf(AppThemeMode.LIGHT)
        private set

    fun init(context: Context) {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedMode = prefs.getString(KEY_THEME_MODE, AppThemeMode.LIGHT.name)
        currentThemeMode = try {
            AppThemeMode.valueOf(savedMode ?: AppThemeMode.LIGHT.name)
        } catch (e: Exception) {
            AppThemeMode.LIGHT
        }
    }

    fun setThemeMode(context: Context, mode: AppThemeMode) {
        currentThemeMode = mode
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_THEME_MODE, mode.name).apply()
    }
}
