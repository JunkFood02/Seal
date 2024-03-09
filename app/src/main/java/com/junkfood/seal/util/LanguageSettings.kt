package com.junkfood.seal.util

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.core.os.LocaleListCompat
import com.junkfood.seal.R
import java.util.Locale


@Composable
@Deprecated("See Locale.toDisplayName()")
fun getLanguageDesc(language: Int): String {
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
            PORTUGUESE_PORTUGAL -> R.string.la_pt
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
private const val PORTUGUESE_PORTUGAL = 38
private const val CATALAN = 39
private const val HEBREW = 40
private const val PORTUGUESE = 41

val LocaleLanguageCodeMap =
    mapOf(
        Locale("ar") to ARABIC,
        Locale("az") to AZERBAIJANI,
        Locale("eu") to BASQUE,
        Locale("be") to BELARUSIAN,
        Locale.forLanguageTag("zh-Hans") to SIMPLIFIED_CHINESE,
        Locale.forLanguageTag("zh-Hant") to TRADITIONAL_CHINESE,
        Locale("ca") to CATALAN,
        Locale("hr") to CROATIAN,
        Locale("cs") to CZECH,
        Locale("da") to DANISH,
        Locale("nl") to DUTCH,
        Locale("en", "US") to ENGLISH,
        Locale("fil") to FILIPINO,
        Locale("fr") to FRENCH,
        Locale("de") to GERMAN,
        Locale("he") to HEBREW,
        Locale("hi") to HINDI,
        Locale("hu") to HUNGARIAN,
        Locale("in") to INDONESIAN,
        Locale("it") to ITALIAN,
        Locale("ja") to JAPANESE,
        Locale("ko") to KOREAN,
        Locale("ms") to MALAY,
        Locale("ml") to MALAYALAM,
        Locale("nb") to NORWEGIAN_BOKMAL,
        Locale("nn") to NORWEGIAN_NYNORSK,
        Locale("fa") to PERSIAN,
        Locale("pl") to POLISH,
        Locale("pt") to PORTUGUESE,
        Locale("pt", "PT") to PORTUGUESE_PORTUGAL,
        Locale("pt", "BR") to PORTUGUESE_BRAZIL,
        Locale("pa") to PUNJABI,
        Locale("ru") to RUSSIAN,
        Locale("sr") to SERBIAN,
        Locale("si") to SINHALA,
        Locale("es") to SPANISH,
        Locale("sv") to SWEDISH,
        Locale("ta") to TAMIL,
        Locale("tr") to TURKISH,
        Locale("uk") to UKRAINIAN,
        Locale("vi") to VIETNAMESE,
    )


@Composable
fun Locale?.toDisplayName(): String = this?.getDisplayName(this) ?: stringResource(
    id = R.string.follow_system
)

fun setLanguage(locale: Locale?) {
    val localeList = locale?.let {
        LocaleListCompat.create(it)
    } ?: LocaleListCompat.getEmptyLocaleList()
    AppCompatDelegate.setApplicationLocales(localeList)
}