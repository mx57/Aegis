package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "runic_app_settings")

data class UserSettings(
    val hasCompletedOnboarding: Boolean,
    val defaultFuthark: String,
    val defaultStyle: String,
    val darkTheme: Boolean?,
    val language: String
)

class AppSettings(private val context: Context) {

    companion object {
        val KEY_ONBOARDING = booleanPreferencesKey("onboarding_done")
        val KEY_FUTHARK = stringPreferencesKey("default_futhark")
        val KEY_STYLE = stringPreferencesKey("default_style")
        val KEY_THEME_MODE = stringPreferencesKey("theme_mode") // "SYSTEM", "DARK", "LIGHT"
        val KEY_LANG = stringPreferencesKey("language")
    }

    val settingsFlow: Flow<UserSettings> = context.dataStore.data.map { pref ->
        val themeStr = pref[KEY_THEME_MODE] ?: "DARK"
        val darkTheme = when (themeStr) {
            "DARK" -> true
            "LIGHT" -> false
            else -> null
        }
        UserSettings(
            hasCompletedOnboarding = pref[KEY_ONBOARDING] ?: false,
            defaultFuthark = pref[KEY_FUTHARK] ?: "elder",
            defaultStyle = pref[KEY_STYLE] ?: "ORNAMENTAL",
            darkTheme = darkTheme,
            language = pref[KEY_LANG] ?: "ru"
        )
    }

    suspend fun setOnboardingDone(done: Boolean) {
        context.dataStore.edit { it[KEY_ONBOARDING] = done }
    }

    suspend fun setDefaultFuthark(futhark: String) {
        context.dataStore.edit { it[KEY_FUTHARK] = futhark }
    }

    suspend fun setDefaultStyle(style: String) {
        context.dataStore.edit { it[KEY_STYLE] = style }
    }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { it[KEY_THEME_MODE] = mode }
    }

    suspend fun setLanguage(lang: String) {
        context.dataStore.edit { it[KEY_LANG] = lang }
    }
}
