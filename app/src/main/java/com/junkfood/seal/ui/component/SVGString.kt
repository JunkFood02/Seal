package com.junkfood.seal.ui.component

import android.util.Log
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

private const val TAG = "SVGString"

@Composable
fun String.parseDynamicColor(
    colorScheme: ColorScheme = MaterialTheme.colorScheme,
): String =
    replace("fill=\"(.+?)\"".toRegex()) {
        val value = it.groupValues[1]
        Log.i(TAG, "parseDynamicColor: $value")
        if (value.startsWith("#")) return@replace it.value
        try {
            val (scheme, tone) = value.split("(?<=\\d)(?=\\D)|(?=\\d)(?<=\\D)".toRegex())
            val argb = when (scheme) {
                "p" -> colorScheme.primary
                "s" -> colorScheme.secondary
                "t" -> colorScheme.tertiary
                else -> Color.Transparent
            }.toArgb()
            "fill=\"${String.format("#%06X", 0xFFFFFF and argb)}\""
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("RLog", "parseDynamicColor: ${e.message}")
            it.value
        }
    }