package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
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
    val language: String,
    val animationSpeedMs: Int = 4000,
    val geminiApiKey: String = "",
    val nameStaveShowFrame: Boolean = true,
    val nameStaveShowRuneRing: Boolean = true,
    val nameStaveShowCenterEmblem: Boolean = true,
    val nameStaveShowRayBurst: Boolean = true,
    val nameStaveShowBranchNotches: Boolean = true,
    val nameStaveShowFinials: Boolean = true,
    val nameStaveShowCornerAccents: Boolean = true,
    val nameStaveShowGlow: Boolean = true
)

class AppSettings(private val context: Context) {

    companion object {
        val KEY_ONBOARDING = booleanPreferencesKey("onboarding_done")
        val KEY_FUTHARK = stringPreferencesKey("default_futhark")
        val KEY_STYLE = stringPreferencesKey("default_style")
        val KEY_THEME_MODE = stringPreferencesKey("theme_mode") // "SYSTEM", "DARK", "LIGHT"
        val KEY_LANG = stringPreferencesKey("language")
        val KEY_ANIM_SPEED_MS = intPreferencesKey("anim_speed_ms")
        val KEY_GEMINI_API_KEY = stringPreferencesKey("gemini_api_key")

        val KEY_NAME_STAVE_SHOW_FRAME = booleanPreferencesKey("ns_show_frame")
        val KEY_NAME_STAVE_SHOW_RUNERING = booleanPreferencesKey("ns_show_runering")
        val KEY_NAME_STAVE_SHOW_CENTER_EMBLEM = booleanPreferencesKey("ns_show_center_emblem")
        val KEY_NAME_STAVE_SHOW_RAY_BURST = booleanPreferencesKey("ns_show_ray_burst")
        val KEY_NAME_STAVE_SHOW_BRANCH_NOTCHES = booleanPreferencesKey("ns_show_branch_notches")
        val KEY_NAME_STAVE_SHOW_FINIALS = booleanPreferencesKey("ns_show_finials")
        val KEY_NAME_STAVE_SHOW_CORNER_ACCENTS = booleanPreferencesKey("ns_show_corner_accents")
        val KEY_NAME_STAVE_SHOW_GLOW = booleanPreferencesKey("ns_show_glow")
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
            language = pref[KEY_LANG] ?: "ru",
            animationSpeedMs = pref[KEY_ANIM_SPEED_MS] ?: 4200,
            geminiApiKey = pref[KEY_GEMINI_API_KEY] ?: "",
            nameStaveShowFrame = pref[KEY_NAME_STAVE_SHOW_FRAME] ?: true,
            nameStaveShowRuneRing = pref[KEY_NAME_STAVE_SHOW_RUNERING] ?: true,
            nameStaveShowCenterEmblem = pref[KEY_NAME_STAVE_SHOW_CENTER_EMBLEM] ?: true,
            nameStaveShowRayBurst = pref[KEY_NAME_STAVE_SHOW_RAY_BURST] ?: true,
            nameStaveShowBranchNotches = pref[KEY_NAME_STAVE_SHOW_BRANCH_NOTCHES] ?: true,
            nameStaveShowFinials = pref[KEY_NAME_STAVE_SHOW_FINIALS] ?: true,
            nameStaveShowCornerAccents = pref[KEY_NAME_STAVE_SHOW_CORNER_ACCENTS] ?: true,
            nameStaveShowGlow = pref[KEY_NAME_STAVE_SHOW_GLOW] ?: true
        )
    }

    suspend fun setGeminiApiKey(apiKey: String) {
        context.dataStore.edit { it[KEY_GEMINI_API_KEY] = apiKey.trim() }
    }

    suspend fun setAnimationSpeedMs(speedMs: Int) {
        context.dataStore.edit { it[KEY_ANIM_SPEED_MS] = speedMs }
    }

    suspend fun setNameStaveElement(key: Preferences.Key<Boolean>, enabled: Boolean) {
        context.dataStore.edit { it[key] = enabled }
    }

    suspend fun setNameStaveAllElements(enabled: Boolean) {
        context.dataStore.edit {
            it[KEY_NAME_STAVE_SHOW_FRAME] = enabled
            it[KEY_NAME_STAVE_SHOW_RUNERING] = enabled
            it[KEY_NAME_STAVE_SHOW_CENTER_EMBLEM] = enabled
            it[KEY_NAME_STAVE_SHOW_RAY_BURST] = enabled
            it[KEY_NAME_STAVE_SHOW_BRANCH_NOTCHES] = enabled
            it[KEY_NAME_STAVE_SHOW_FINIALS] = enabled
            it[KEY_NAME_STAVE_SHOW_CORNER_ACCENTS] = enabled
            it[KEY_NAME_STAVE_SHOW_GLOW] = enabled
        }
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
