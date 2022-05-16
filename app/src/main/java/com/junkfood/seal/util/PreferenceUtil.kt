package com.junkfood.seal.util

import com.tencent.mmkv.MMKV


object PreferenceUtil {

    private val kv = MMKV.defaultMMKV()

    fun updateValue(key: String, b: Boolean) = kv.encode(key, b)
    fun getValue(key: String): Boolean = kv.decodeBool(key, false)
    fun getValueOrTrue(key: String): Boolean = kv.decodeBool(key, true)
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
}