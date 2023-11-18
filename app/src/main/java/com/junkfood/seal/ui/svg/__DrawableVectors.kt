package com.junkfood.seal.ui.svg

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.junkfood.seal.ui.svg.drawablevectors.Coder
import com.junkfood.seal.ui.svg.drawablevectors.Download
import com.junkfood.seal.ui.svg.drawablevectors.VideoFiles
import com.junkfood.seal.ui.svg.drawablevectors.VideoSteaming
import kotlin.collections.List as ____KtList

public object DynamicColorImageVectors

internal var currentColorScheme: ColorScheme? = null


private var __AllAssets: ____KtList<ImageVector>? = null

public val DynamicColorImageVectors.AllAssets: ____KtList<ImageVector>
    @Composable
    get() {
        if (__AllAssets != null) {
            return __AllAssets!!
        }
        __AllAssets = listOf(Download, Coder, VideoSteaming, VideoFiles)
        return __AllAssets!!
    }
