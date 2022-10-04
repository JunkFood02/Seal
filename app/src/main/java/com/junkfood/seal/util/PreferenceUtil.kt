package com.junkfood.seal.util

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.core.os.LocaleListCompat
import com.junkfood.seal.BaseApplication.Companion.applicationScope
import com.junkfood.seal.BaseApplication.Companion.context
import com.junkfood.seal.R
import com.junkfood.seal.ui.theme.ColorScheme.DEFAULT_SEED_COLOR
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


object PreferenceUtil {
    private val kv = MMKV.defaultMMKV()

    fun updateValue(key: String, b: Boolean) = kv.encode(key, b)
    fun updateInt(key: String, int: Int) = kv.encode(key, int)
    fun getInt(key: String, int: Int) = kv.decodeInt(key, int)
    fun getValue(key: String): Boolean = kv.decodeBool(key, false)
    fun getValue(key: String, b: Boolean): Boolean = kv.decodeBool(key, b)
    fun getString(key: String): String? = kv.decodeString(key)

    fun getString(key: String, default: String): String = kv.decodeString(key, default).toString()
    fun updateString(key: String, string: String) = kv.encode(key, string)

    fun containsKey(key: String) = kv.containsKey(key)
    suspend fun getTemplate(): String {
        return DatabaseUtil.getTemplateList()[kv.decodeInt(TEMPLATE_INDEX, 0)].template
    }

    //        kv.decodeString(TEMPLATE, context.getString(R.string.template_example)).toString()
    fun getOutputPathTemplate(): String =
        kv.decodeString(OUTPUT_PATH_TEMPLATE, "%(uploader)s/%(playlist_title)s/").toString()

    fun getAudioFormat(): Int = kv.decodeInt(AUDIO_FORMAT, 0)

    fun getAudioFormatDesc(audioFormatCode: Int = getAudioFormat()): String {
        return when (audioFormatCode) {
            0 -> context.getString(R.string.not_convert)
            1 -> context.getString(R.string.convert_to).format("mp3")
            else -> context.getString(R.string.convert_to).format("m4a")
        }
    }

    fun getVideoResolution(): Int = kv.decodeInt(VIDEO_QUALITY, 0)

    fun getVideoResolutionDesc(videoQualityCode: Int = getVideoResolution()): String {
        return when (videoQualityCode) {
            1 -> "2160p"
            2 -> "1440p"
            3 -> "1080p"
            4 -> "720p"
            5 -> "480p"
            6 -> "360p"
            else -> context.getString(R.string.best_quality)
        }
    }

    fun getVideoFormat(): Int = kv.decodeInt(VIDEO_FORMAT, 0)

    fun getVideoFormatDesc(videoFormatCode: Int = getVideoFormat()): String {
        return when (videoFormatCode) {
            1 -> "MP4"
            2 -> "WebM"
            else -> context.getString(R.string.not_specified)
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
    const val DARK_THEME = "dark_theme_value"
    const val AUDIO_FORMAT = "audio_format"
    const val VIDEO_FORMAT = "video_format"
    const val VIDEO_QUALITY = "quality"
    const val WELCOME_DIALOG = "welcome_dialog"
    const val VIDEO_DIRECTORY = "download_dir"
    const val AUDIO_DIRECTORY = "audio_dir"
    const val SUBDIRECTORY = "sub-directory"
    const val PLAYLIST = "playlist"
    const val LANGUAGE = "language"
    const val NOTIFICATION = "notification"
    const val THEME_COLOR = "theme_color"
    const val CUSTOM_PATH = "custom_path"
    const val OUTPUT_PATH_TEMPLATE = "path_template"
    const val SUBTITLE = "subtitle"
    const val TEMPLATE_INDEX = "template_index"
    const val MAX_FILE_SIZE = "max_file_size"
    const val SPONSORBLOCK = "sponsorblock"
    const val SPONSORBLOCK_CATEGORIES = "sponsorblock_categories"
    const val ARIA2C = "aria2c"
    const val COOKIES = "cookies"
    const val COOKIES_FILE = "cookies_file"
    const val AUTO_UPDATE = "auto_update"
    const val PRIVATE_MODE = "private_mode"
    const val DYNAMIC_COLOR = "dynamic color"

    const val SYSTEM_DEFAULT = 0
    const val EMPTY_SEED_COLOR = 0

    // Do not modify
    private const val SIMPLIFIED_CHINESE = 1
    private const val ENGLISH = 2
    private const val CZECH = 3
    private const val FRENCH = 4
    private const val GERMAN = 5
    private const val NORWEGIAN = 6
    private const val DANISH = 7
    private const val SPANISH = 8
    private const val TURKISH = 9
    private const val UKRAINIAN = 10
    private const val RUSSIAN = 11
    private const val ARABIC = 12
    private const val PERSIAN = 13
    private const val INDONESIAN = 14
    private const val FILIPINO = 15
    private const val ITALIAN = 16
    private const val DUTCH = 17
    private const val PORTUGUESE_BRAZIL = 18
    private const val JAPANESE = 19
    private const val POLISH = 20
    private const val HUNGARIAN = 21
    private const val MALAY = 22
    private const val TRADITIONAL_CHINESE = 23
    private const val VIETNAMESE = 24
    private const val BELARUSIAN = 25

    // Sorted alphabetically
    val languageMap: Map<Int, String> = mapOf(
        Pair(ARABIC, "ar"),
        Pair(BELARUSIAN, "be"),
        Pair(SIMPLIFIED_CHINESE, "zh-CN"),
        Pair(TRADITIONAL_CHINESE, "zh-TW"),
        Pair(CZECH, "cs"),
        Pair(DANISH, "da"),
        Pair(DUTCH, "nl"),
        Pair(ENGLISH, "en-US"),
        Pair(FILIPINO, "fil"),
        Pair(FRENCH, "fr"),
        Pair(GERMAN, "de"),
        Pair(HUNGARIAN, "hu"),
        Pair(INDONESIAN, "in"),
        Pair(ITALIAN, "it"),
        Pair(JAPANESE, "ja"),
        Pair(MALAY, "ms"),
        Pair(NORWEGIAN, "nb-NO"),
        Pair(PERSIAN, "fa"),
        Pair(POLISH, "pl"),
        Pair(PORTUGUESE_BRAZIL, "pt-BR"),
        Pair(RUSSIAN, "ru"),
        Pair(SPANISH, "es"),
        Pair(TURKISH, "tr"),
        Pair(UKRAINIAN, "ua"),
        Pair(VIETNAMESE, "vi"),
    )

    fun getLanguageConfiguration(languageNumber: Int = kv.decodeInt(LANGUAGE)): String {
        return if (languageMap.containsKey(languageNumber)) languageMap[languageNumber].toString() else ""
    }

    fun getLanguageNumberByCode(languageCode: String): Int {
        languageMap.entries.forEach {
            if (it.value == languageCode) return it.key
        }
        return SYSTEM_DEFAULT
    }

    fun getLanguageNumber(): Int {
        return if (Build.VERSION.SDK_INT >= 33)
            getLanguageNumberByCode(
                LocaleListCompat.getAdjustedDefault()[0]?.toLanguageTag().toString()
            )
        else getInt(LANGUAGE, 0)
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
    fun getLanguageDesc(language: Int = getLanguageNumber()): String {
        return when (language) {
            SIMPLIFIED_CHINESE -> stringResource(R.string.la_zh_CN)
            ENGLISH -> stringResource(R.string.la_en_US)
            CZECH -> stringResource(R.string.la_cs)
            FRENCH -> stringResource(R.string.la_fr)
            GERMAN -> stringResource(R.string.la_de)
            NORWEGIAN -> stringResource(R.string.la_nb_NO)
            DANISH -> stringResource(R.string.la_da)
            SPANISH -> stringResource(R.string.la_es)
            TURKISH -> stringResource(R.string.la_tr)
            UKRAINIAN -> stringResource(R.string.la_ua)
            RUSSIAN -> stringResource(R.string.la_ru)
            ARABIC -> stringResource(R.string.la_ar)
            PERSIAN -> stringResource(R.string.la_fa)
            INDONESIAN -> stringResource(R.string.la_in)
            FILIPINO -> stringResource(R.string.la_fil)
            ITALIAN -> stringResource(R.string.la_it)
            DUTCH -> stringResource(R.string.la_nl)
            PORTUGUESE_BRAZIL -> stringResource(R.string.la_pt_BR)
            JAPANESE -> stringResource(R.string.la_ja)
            POLISH -> stringResource(R.string.la_pl)
            HUNGARIAN -> stringResource(R.string.la_hu)
            MALAY -> stringResource(R.string.la_ms)
            TRADITIONAL_CHINESE -> stringResource(R.string.la_zh_TW)
            VIETNAMESE -> stringResource(R.string.la_vi)
            BELARUSIAN -> stringResource(R.string.la_be)
            else -> stringResource(R.string.follow_system)
        }
    }

    fun getSponsorBlockCategories(): String =
        with(getString(SPONSORBLOCK_CATEGORIES)) {
            if (isNullOrEmpty()) "all"
            else this
        }

    fun getCookies(): String = getString(COOKIES_FILE) ?: ""
    data class AppSettings(
        val darkTheme: DarkThemePreference = DarkThemePreference(),
        val isDynamicColorEnabled: Boolean = false,
        val seedColor: Int = DEFAULT_SEED_COLOR
    )

    private val mutableAppSettingsStateFlow = MutableStateFlow(
        AppSettings(
            DarkThemePreference(
                kv.decodeInt(
                    DARK_THEME,
                    DarkThemePreference.FOLLOW_SYSTEM
                )
            ), seedColor = kv.decodeInt(THEME_COLOR, DEFAULT_SEED_COLOR)
        )
    )
    val AppSettingsStateFlow = mutableAppSettingsStateFlow.asStateFlow()

    fun switchDarkThemeMode(mode: Int) {
        applicationScope.launch(Dispatchers.IO) {
            mutableAppSettingsStateFlow.update {
                it.copy(darkTheme = DarkThemePreference(mode))
            }
            kv.encode(DARK_THEME, mode)
        }
    }

    fun modifyThemeSeedColor(colorArgb: Int) {
        applicationScope.launch(Dispatchers.IO) {
            mutableAppSettingsStateFlow.update {
                it.copy(seedColor = colorArgb)
            }
            kv.encode(THEME_COLOR, colorArgb)
        }
    }

    fun switchDynamicColor(enabled: Boolean = !mutableAppSettingsStateFlow.value.isDynamicColorEnabled) {
        applicationScope.launch(Dispatchers.IO) {
            mutableAppSettingsStateFlow.update {
                it.copy(isDynamicColorEnabled = enabled)
            }
            kv.encode(DYNAMIC_COLOR, enabled)
        }
    }

    class DarkThemePreference(
        var darkThemeValue: Int = FOLLOW_SYSTEM,
    ) {
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

    }

    private const val TAG = "PreferenceUtil"
}
