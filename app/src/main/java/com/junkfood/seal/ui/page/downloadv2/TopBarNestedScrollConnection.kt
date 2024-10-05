package com.junkfood.seal.ui.page.downloadv2

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource

internal class TopBarNestedScrollConnection(
    private val spacerHeight: Int,
    private val offset: () -> Int,
    private val onOffsetUpdate: (Int) -> Unit,
) : NestedScrollConnection {
    override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
        val delta = available.y.toInt()
        if (delta < 0) {
            val previousOffset = offset()
            if (previousOffset >= -spacerHeight) {
                val newOffset = (previousOffset + delta).coerceIn(-spacerHeight, 0)
                onOffsetUpdate(newOffset)
                val consumedOffset = newOffset - previousOffset
                return Offset(x = 0f, y = consumedOffset.toFloat())
            }
        }
        return super.onPreScroll(available, source)
    }

    override fun onPostScroll(
        consumed: Offset,
        available: Offset,
        source: NestedScrollSource,
    ): Offset {
        val delta = available.y.toInt()
        if (delta > 0) {
            val previousOffset = offset()
            if (previousOffset < 0) {
                val newOffset = (previousOffset + delta).coerceIn(-spacerHeight, 0)
                onOffsetUpdate(newOffset)
                val consumedOffset = newOffset - previousOffset
                return super.onPostScroll(
                    Offset(consumed.x, consumed.y + consumedOffset),
                    available,
                    source,
                )
            }
        }
        return super.onPostScroll(consumed, available, source)
    }
}
