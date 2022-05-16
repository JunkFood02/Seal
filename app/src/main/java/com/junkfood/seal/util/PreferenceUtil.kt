package com.junkfood.seal.util

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.junkfood.seal.BaseApplication.Companion.context
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
    const val DYNAMIC_COLORS = "dynamic_color"
    const val DARK_THEME = "dark_theme"
    var darkTheme = kv.decodeBool(DARK_THEME, true)
    val DARK_THEME_KEY = booleanPreferencesKey(DARK_THEME)

    fun darkThemeSwitch() {
        Log.d(TAG, darkTheme.toString())
        darkTheme = darkTheme.not()
        CoroutineScope(Job()).launch(Dispatchers.IO) {
            context.dataStore.edit { settings ->
                val value = settings[DARK_THEME_KEY] ?: true
                settings[DARK_THEME_KEY] = !value
            }
        }
    }

    private const val TAG = "PreferenceUtil"
}