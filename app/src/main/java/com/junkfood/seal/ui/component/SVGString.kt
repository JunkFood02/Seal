package com.junkfood.seal.ui.component

import android.util.Log
import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

private const val TAG = "SVGString"

fun String.parseDynamicColor(
    colorScheme: ColorScheme,
): String =
    replace("fill=\"(.+?)\"".toRegex()) {
        val value = it.groupValues[1]
        Log.i(TAG, "parseDynamicColor: $value")
        if (value.startsWith("#")) return@replace it.value
        try {
            val argb = when (value) {
                "p" -> colorScheme.primary
                "s" -> colorScheme.secondary
                "t" -> colorScheme.tertiary
                "su" -> colorScheme.surface
                "osu" -> colorScheme.onSurface
                "sc" -> colorScheme.secondaryContainer
                "pc" -> colorScheme.primaryContainer
                else -> Color.Transparent
            }.toArgb()
            "fill=\"${String.format("#%06X", 0xFFFFFF and argb)}\""
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("RLog", "parseDynamicColor: ${e.message}")
            it.value
        }
    }