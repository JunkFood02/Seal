package com.junkfood.seal.util

import com.tencent.mmkv.MMKV


object PreferenceUtil {

    private val kv = MMKV.defaultMMKV()

    fun updateValue(key: String, b: Boolean) = kv.encode(key, b)
    fun getValue(key: String): Boolean = kv.decodeBool(key, true)
    fun getString(key: String): String? = kv.decodeString(key)
    fun updateString(key: String, string: String) = kv.encode(key, string)
}