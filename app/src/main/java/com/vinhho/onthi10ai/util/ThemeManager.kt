package com.vinhho.onthi10ai.util

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ThemeManager(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _themeMode = MutableStateFlow(
        prefs.getInt(KEY_THEME_MODE, MODE_LIGHT)
    )
    val themeMode: StateFlow<Int> = _themeMode.asStateFlow()

    fun isDarkMode(): Boolean = _themeMode.value == MODE_DARK

    fun applySavedTheme() {
        AppCompatDelegate.setDefaultNightMode(_themeMode.value)
    }

    fun setDarkMode(enabled: Boolean) {
        val target = if (enabled) MODE_DARK else MODE_LIGHT
        if (_themeMode.value == target && AppCompatDelegate.getDefaultNightMode() == target) {
            return
        }
        prefs.edit().putInt(KEY_THEME_MODE, target).apply()
        _themeMode.value = target
        AppCompatDelegate.setDefaultNightMode(target)
    }

    fun toggleTheme(): Boolean {
        val nextDark = !isDarkMode()
        setDarkMode(nextDark)
        return nextDark
    }

    companion object {
        private const val PREFS_NAME = "giasuai_theme_prefs"
        private const val KEY_THEME_MODE = "app_theme_mode"

        const val MODE_LIGHT = AppCompatDelegate.MODE_NIGHT_NO
        const val MODE_DARK = AppCompatDelegate.MODE_NIGHT_YES
    }
}
