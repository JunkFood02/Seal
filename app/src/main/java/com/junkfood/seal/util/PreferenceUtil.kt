package com.junkfood.seal.util

import android.content.Context
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
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
    fun updateInt(key: String, int: Int) = kv.encode(key, int)
    fun getInt(key: String, int: Int) = kv.decodeInt(key, int)
    fun getValue(key: String): Boolean = kv.decodeBool(key, false)
    fun getValue(key: String, b: Boolean): Boolean = kv.decodeBool(key, b)
    fun getString(key: String): String? = kv.decodeString(key)
    fun updateString(key: String, string: String) = kv.encode(key, string)

    fun getTemplate(): String =
        kv.decodeString(TEMPLATE, context.getString(R.string.template_example)).toString()

    fun getAudioFormat(): Int = kv.decodeInt(AUDIO_FORMAT, 0)

    @Composable
    fun getAudioFormatDesc(audioFormatCode: Int = getAudioFormat()): String {
        return when (audioFormatCode) {
            0 -> stringResource(R.string.not_convert)
            1 -> stringResource(R.string.convert_to).format("mp3")
            else -> stringResource(R.string.convert_to).format("m4a")
        }
    }

    fun getVideoQuality(): Int = kv.decodeInt(VIDEO_QUALITY, 0)

    @Composable
    fun getVideoQualityDesc(videoQualityCode: Int = getVideoQuality()): String {
        return when (videoQualityCode) {
            1 -> "1440p"
            2 -> "1080p"
            3 -> "720p"
            4 -> "480p"
            else -> stringResource(R.string.best_quality)
        }
    }

    fun getVideoFormat(): Int = kv.decodeInt(VIDEO_FORMAT, 0)

    @Composable
    fun getVideoFormatDesc(videoFormatCode: Int = getVideoFormat()): String {
        return when (videoFormatCode) {
            1 -> "MP4"
            2 -> "WebM"
            else -> stringResource(R.string.not_specified)
        }
    }

    const val CUSTOM_COMMAND = "custom_command"
    const val CONCURRENT = "concurrent_fragments"
    const val EXTRACT_AUDIO = "extract_audio"
    const val THUMBNAIL = "create_thumbnail"
    const val TEMPLATE = "template"
    const val OPEN_IMMEDIATELY = "open_when_finish"
    const val YT_DLP = "yt-dlp_init"
    const val DEBUG = "debug"
    const val CONFIGURE = "configure"
    const val DYNAMIC_COLORS = "dynamic_color"
    const val DARK_THEME = "dark_theme_value"
    const val AUDIO_FORMAT = "audio_format"
    const val VIDEO_FORMAT = "video_format"
    const val VIDEO_QUALITY = "quality"
    const val WELCOME_DIALOG = "welcome_dialog"
    const val DOWNLOAD_DIRECTORY = "download_dir"
    const val PLAYLIST = "playlist"
    const val LANGUAGE = "language"
    val DARK_THEME_KEY = intPreferencesKey(DARK_THEME)
    val DYNAMIC_COLOR_KEY = booleanPreferencesKey(DYNAMIC_COLORS)

    const val FOLLOW_SYSTEM = 0
    const val SIMPLIFIED_CHINESE = 1
    const val ENGLISH = 2

    fun getLanguageConfiguration(language: Int = kv.decodeInt(LANGUAGE)): String {
        return when (language) {
            SIMPLIFIED_CHINESE -> "zh-CN"
            ENGLISH -> "en-US"
            else -> ""
        }
    }

    fun getConcurrentFragments(level: Int = kv.decodeInt(CONCURRENT, 1)): Float {
        return when (level) {
            1 -> 0f
            4 -> 0.25f
            8 -> 0.5f
            12 -> 0.75f
            else -> 1f
        }
    }

    @Composable
    fun getLanguageDesc(language: Int = kv.decodeInt(LANGUAGE)): String {
        return when (language) {
            SIMPLIFIED_CHINESE -> stringResource(R.string.la_zh_CN)
            ENGLISH -> stringResource(R.string.la_en_US)
            else -> stringResource(R.string.defaults)
        }
    }

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

        @Composable
        fun getDarkThemeDesc(): String {
            return when (darkThemeValue) {
                FOLLOW_SYSTEM -> stringResource(R.string.follow_system)
                ON -> stringResource(R.string.on)
                else -> stringResource(R.string.off)
            }
        }

        fun switch(value: Int) {
            darkThemeValue = value
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