package com.junkfood.seal.util

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.core.os.LocaleListCompat
import com.junkfood.seal.R
import com.junkfood.seal.util.PreferenceUtil.getInt


private fun getLanguageNumberByCode(languageCode: String): Int =
    languageMap.entries.find { it.value == languageCode }?.key ?: SYSTEM_DEFAULT

fun getLanguageNumber(): Int {
    return if (Build.VERSION.SDK_INT >= 33)
        getLanguageNumberByCode(
            LocaleListCompat.getAdjustedDefault()[0]?.toLanguageTag().toString()
        )
    else LANGUAGE.getInt()
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
            NORWEGIAN_BOKMAL -> R.string.la_nb
            DANISH -> R.string.la_da
            SPANISH -> R.string.la_es
            TURKISH -> R.string.la_tr
            UKRAINIAN -> R.string.la_uk
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
            TAMIL -> R.string.la_ta
            KOREAN -> R.string.la_ko
            SWEDISH -> R.string.la_sv
            else -> R.string.follow_system
        }
    )
}

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
private const val TAMIL = 35
private const val KOREAN = 36
private const val SWEDISH = 37

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
    KOREAN to "ko",
    MALAY to "ms",
    MALAYALAM to "ml",
    NORWEGIAN_BOKMAL to "nb",
    NORWEGIAN_NYNORSK to "nn",
    PERSIAN to "fa",
    POLISH to "pl",
    PORTUGUESE_BRAZIL to "pt-BR",
    PUNJABI to "pa",
    RUSSIAN to "ru",
    SERBIAN to "sr",
    SINHALA to "si",
    SPANISH to "es",
    SWEDISH to "sv",
    TAMIL to "ta",
    TURKISH to "tr",
    UKRAINIAN to "uk",
    VIETNAMESE to "vi",
)