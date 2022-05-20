package com.junkfood.seal.util

import android.content.Context
import android.util.Log
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.junkfood.seal.BaseApplication.Companion.context
import com.junkfood.seal.R
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch


object PreferenceUtil {
    val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

    private val kv = MMKV.defaultMMKV()

    fun updateValue(key: String, b: Boolean) = kv.encode(key, b)
    fun getValue(key: String): Boolean = kv.decodeBool(key, false)
    fun getValue(key: String, b: Boolean): Boolean = kv.decodeBool(key, b)
    fun getString(key: String): String? = kv.decodeString(key)
    fun updateString(key: String, string: String) = kv.encode(key, string)
    const val CUSTOM_COMMAND = "custom_command"
    const val EXTRACT_AUDIO = "extract_audio"
    const val THUMBNAIL = "create_thumbnail"
    const val TEMPLATE = "template"
    const val OPEN_IMMEDIATELY = "open_when_finish"
    const val YT_DLP = "yt-dlp_init"
    const val DEBUG = "debug"
    const val CONFIGURE = "configure"
    const val DYNAMIC_COLORS = "dynamic_color"
    const val DARK_THEME = "dark_theme_value"

    val DARK_THEME_KEY = intPreferencesKey(DARK_THEME)
    val DYNAMIC_COLOR_KEY = booleanPreferencesKey(DYNAMIC_COLORS)


    fun dynamicColorSwitch() {
        CoroutineScope(Job()).launch(Dispatchers.IO) {
            context.dataStore.edit { settings ->
                val value = settings[DYNAMIC_COLOR_KEY] ?: true
                settings[DYNAMIC_COLOR_KEY] = !value
                kv.encode(DYNAMIC_COLORS, !value)
            }
        }
    }


    data class AppSettings(
        val darkTheme: DarkThemePreference = DarkThemePreference(),
        val dynamicColor: Boolean = true
    )

    class DarkThemePreference(var darkThemeValue: Int = FOLLOW_SYSTEM) {
        companion object {
            const val FOLLOW_SYSTEM = 1
            const val ON = 2
            const val OFF = 3
        }

        @Composable
        fun isDarkTheme(): Boolean {
            return if (darkThemeValue == FOLLOW_SYSTEM)
                isSystemInDarkTheme()
            else darkThemeValue == ON
        }

        fun getDarkThemeDesc(): Int {
            return when (darkThemeValue) {
                FOLLOW_SYSTEM -> R.string.follow_system
                ON -> R.string.on
                else -> R.string.off
            }
        }

        fun switch(value: Int) {
            darkThemeValue = value
            Log.d(TAG, value.toString())
            CoroutineScope(Job()).launch(Dispatchers.IO) {
                kv.encode(DARK_THEME, value)
                context.dataStore.edit { settings ->
                    settings[DARK_THEME_KEY] = value
                }
            }
        }
    }


    @Composable
    fun initialAppSettings(): AppSettings {
        return AppSettings(
            DarkThemePreference(
                kv.decodeInt(
                    DARK_THEME,
                    DarkThemePreference.FOLLOW_SYSTEM
                )
            ), kv.decodeBool(DYNAMIC_COLORS, true)
        )
    }

    private const val TAG = "PreferenceUtil"
}