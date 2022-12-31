package com.junkfood.seal.util

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.core.os.LocaleListCompat
import com.google.android.material.color.DynamicColors
import com.junkfood.seal.App
import com.junkfood.seal.App.Companion.applicationScope
import com.junkfood.seal.App.Companion.context
import com.junkfood.seal.App.Companion.packageInfo
import com.junkfood.seal.R
import com.junkfood.seal.database.CommandTemplate
import com.junkfood.seal.database.CookieProfile
import com.junkfood.seal.ui.theme.ColorScheme.DEFAULT_SEED_COLOR
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
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
            7 -> context.getString(R.string.lowest_quality)
            else -> context.getString(R.string.best_quality)
        }
    }

    fun getVideoFormat(): Int = kv.decodeInt(VIDEO_FORMAT, 0)

    fun getVideoFormatDesc(videoFormatCode: Int = getVideoFormat()): String {
        return when (videoFormatCode) {
            1 -> "MP4"
            2 -> "WebM (VP9)"
            3 -> "WebM (AV1)"
            else -> context.getString(R.string.not_specified)
        }
    }

    fun isNetworkAvailableForDownload() =
        getValue(CELLULAR_DOWNLOAD) || !App.connectivityManager.isActiveNetworkMetered

    fun isAutoUpdateEnabled() = getValue(AUTO_UPDATE, !packageInfo.versionName.contains("F-Droid"))

    const val TEMPLATE_EXAMPLE =
        """--no-mtime -f "bv*[ext=mp4]+ba[ext=m4a]/b[ext=mp4] / bv*+ba/b""""

    const val CUSTOM_COMMAND = "custom_command"
    const val CONCURRENT = "concurrent_fragments"
    const val EXTRACT_AUDIO = "extract_audio"
    const val THUMBNAIL = "create_thumbnail"
    const val YT_DLP = "yt-dlp_init"
    const val DEBUG = "debug"
    const val CONFIGURE = "configure"
    private const val DARK_THEME = "dark_theme_value"
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
    private const val THEME_COLOR = "theme_color"
    const val CUSTOM_PATH = "custom_path"
    const val OUTPUT_PATH_TEMPLATE = "path_template"
    const val SUBTITLE = "subtitle"

    //    const val TEMPLATE_INDEX = "template_index"
    const val TEMPLATE_ID = "template_id"
    const val MAX_FILE_SIZE = "max_file_size"
    const val SPONSORBLOCK = "sponsorblock"
    const val SPONSORBLOCK_CATEGORIES = "sponsorblock_categories"
    const val ARIA2C = "aria2c"
    const val COOKIES = "cookies"
    const val AUTO_UPDATE = "auto_update"
    const val PRIVATE_MODE = "private_mode"
    private const val DYNAMIC_COLOR = "dynamic_color"
    const val CELLULAR_DOWNLOAD = "cellular_download"
    const val RATE_LIMIT = "rate_limit"
    const val MAX_RATE = "max_rate"
    private const val HIGH_CONTRAST = "high_contrast"
    const val DISABLE_PREVIEW = "disable_preview"
    const val PRIVATE_DIRECTORY = "private_directory"
    const val CROP_ARTWORK = "crop_artwork"
    const val FORMAT_SELECTION = "format_selection"
    const val SYSTEM_DEFAULT = 0

    // Do not modify
    private const val SIMPLIFIED_CHINESE = 1
    private const val ENGLISH = 2
    private const val CZECH = 3
    private const val FRENCH = 4
    private const val GERMAN = 5
    private const val NORWEGIAN_BOKMAL = 6
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
    private const val CROATIAN = 26
    private const val BASQUE = 27
    private const val HINDI = 28
    private const val MALAYALAM = 29
    private const val SINHALA = 30
    private const val SERBIAN = 31
    private const val AZERBAIJANI = 32
    private const val NORWEGIAN_NYNORSK = 33
    private const val PUNJABI = 34

    // Sorted alphabetically
    val languageMap: Map<Int, String> = mapOf(
        ARABIC to "ar",
        AZERBAIJANI to "az",
        BASQUE to "eu",
        BELARUSIAN to "be",
        SIMPLIFIED_CHINESE to "zh-CN",
        TRADITIONAL_CHINESE to "zh-TW",
        CROATIAN to "hr",
        CZECH to "cs",
        DANISH to "da",
        DUTCH to "nl",
        ENGLISH to "en-US",
        FILIPINO to "fil",
        FRENCH to "fr",
        GERMAN to "de",
        HINDI to "hi",
        HUNGARIAN to "hu",
        INDONESIAN to "in",
        ITALIAN to "it",
        JAPANESE to "ja",
        MALAY to "ms",
        MALAYALAM to "ml",
        NORWEGIAN_BOKMAL to "nb-NO",
        NORWEGIAN_NYNORSK to "nn",
        PERSIAN to "fa",
        POLISH to "pl",
        PORTUGUESE_BRAZIL to "pt-BR",
        PUNJABI to "pa",
        RUSSIAN to "ru",
        SERBIAN to "sr",
        SINHALA to "si",
        SPANISH to "es",
        TURKISH to "tr",
        UKRAINIAN to "ua",
        VIETNAMESE to "vi",
    )

    fun getLanguageConfiguration(languageNumber: Int = kv.decodeInt(LANGUAGE)) =
        languageMap.getOrElse(languageNumber) { "" }


    private fun getLanguageNumberByCode(languageCode: String): Int =
        languageMap.entries.find { it.value == languageCode }?.key ?: SYSTEM_DEFAULT


    fun getLanguageNumber(): Int {
        return if (Build.VERSION.SDK_INT >= 33)
            getLanguageNumberByCode(
                LocaleListCompat.getAdjustedDefault()[0]?.toLanguageTag().toString()
            )
        else getInt(LANGUAGE, SYSTEM_DEFAULT)
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
        return stringResource(
            when (language) {
                SIMPLIFIED_CHINESE -> R.string.la_zh_CN
                ENGLISH -> R.string.la_en_US
                CZECH -> R.string.la_cs
                FRENCH -> R.string.la_fr
                GERMAN -> R.string.la_de
                NORWEGIAN_BOKMAL -> R.string.la_nb_NO
                DANISH -> R.string.la_da
                SPANISH -> R.string.la_es
                TURKISH -> R.string.la_tr
                UKRAINIAN -> R.string.la_ua
                RUSSIAN -> R.string.la_ru
                ARABIC -> R.string.la_ar
                PERSIAN -> R.string.la_fa
                INDONESIAN -> R.string.la_in
                FILIPINO -> R.string.la_fil
                ITALIAN -> R.string.la_it
                DUTCH -> R.string.la_nl
                PORTUGUESE_BRAZIL -> R.string.la_pt_BR
                JAPANESE -> R.string.la_ja
                POLISH -> R.string.la_pl
                HUNGARIAN -> R.string.la_hu
                MALAY -> R.string.la_ms
                TRADITIONAL_CHINESE -> R.string.la_zh_TW
                VIETNAMESE -> R.string.la_vi
                BELARUSIAN -> R.string.la_be
                CROATIAN -> R.string.la_hr
                BASQUE -> R.string.la_eu
                HINDI -> R.string.la_hi
                MALAYALAM -> R.string.la_ml
                SINHALA -> R.string.la_si
                SERBIAN -> R.string.la_sr
                AZERBAIJANI -> R.string.la_az
                NORWEGIAN_NYNORSK -> R.string.la_nn
                PUNJABI -> R.string.la_pa
                else -> R.string.follow_system
            }
        )
    }

    fun getSponsorBlockCategories(): String =
        with(getString(SPONSORBLOCK_CATEGORIES)) {
            if (isNullOrEmpty()) "default"
            else this
        }

    private const val COOKIE_HEADER = "# Netscape HTTP Cookie File\n" +
            "# Auto-generated by Seal built-in WebView\n"

    private val cookiesStateFlow: StateFlow<String> =
        DatabaseUtil.getCookiesFlow().distinctUntilChanged().map {
            it.fold(StringBuilder(COOKIE_HEADER)) { acc: StringBuilder, cookieProfile: CookieProfile ->
                acc.append(cookieProfile.content)
            }.toString()
        }.stateIn(applicationScope, started = SharingStarted.Eagerly, COOKIE_HEADER)

    fun getCookies(): String = cookiesStateFlow.value

    val templateStateFlow: StateFlow<List<CommandTemplate>> =
        DatabaseUtil.getTemplateFlow().distinctUntilChanged().stateIn(
            applicationScope, started = SharingStarted.Eagerly, emptyList()
        )

    fun getTemplate(): CommandTemplate {
        return templateStateFlow.value.run {
            find { it.id == getInt(TEMPLATE_ID, 0) } ?: first()
        }
    }

    data class AppSettings(
        val darkTheme: DarkThemePreference = DarkThemePreference(),
        val isDynamicColorEnabled: Boolean = false,
        val seedColor: Int = DEFAULT_SEED_COLOR
    )

    fun getMaxDownloadRate(): String = getString(MAX_RATE, "1000")
    private val mutableAppSettingsStateFlow = MutableStateFlow(
        AppSettings(
            DarkThemePreference(
                darkThemeValue = kv.decodeInt(
                    DARK_THEME,
                    DarkThemePreference.FOLLOW_SYSTEM
                ), isHighContrastModeEnabled = kv.decodeBool(HIGH_CONTRAST, false)
            ),
            isDynamicColorEnabled = kv.decodeBool(
                DYNAMIC_COLOR,
                DynamicColors.isDynamicColorAvailable()
            ),
            seedColor = kv.decodeInt(THEME_COLOR, DEFAULT_SEED_COLOR)
        )
    )
    val AppSettingsStateFlow = mutableAppSettingsStateFlow.asStateFlow()

    fun modifyDarkThemePreference(
        darkThemeValue: Int = AppSettingsStateFlow.value.darkTheme.darkThemeValue,
        isHighContrastModeEnabled: Boolean = AppSettingsStateFlow.value.darkTheme.isHighContrastModeEnabled
    ) {
        applicationScope.launch(Dispatchers.IO) {
            mutableAppSettingsStateFlow.update {
                it.copy(
                    darkTheme = AppSettingsStateFlow.value.darkTheme.copy(
                        darkThemeValue = darkThemeValue,
                        isHighContrastModeEnabled = isHighContrastModeEnabled
                    )
                )
            }
            kv.encode(DARK_THEME, darkThemeValue)
            kv.encode(HIGH_CONTRAST, isHighContrastModeEnabled)
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

    data class DarkThemePreference(
        val darkThemeValue: Int = FOLLOW_SYSTEM,
        val isHighContrastModeEnabled: Boolean = false
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
