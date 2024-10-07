package com.junkfood.seal.ui.page.downloadv2

import androidx.compose.animation.core.AnimationState
import androidx.compose.animation.core.DecayAnimationSpec
import androidx.compose.animation.core.animateDecay
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.unit.Velocity
import kotlin.math.abs

private const val TAG = "TopBarNestedScrollConne"

/*
 offset < 0 = scroll down, finger & content going upward
 offset > 0 = scroll up, finger & content going downward
*/

internal class TopBarNestedScrollConnection(
    private val maxOffset: Float,
    private val flingAnimationSpec: DecayAnimationSpec<Float>,
    private val offset: () -> Float,
    private val onOffsetUpdate: (Float) -> Unit,
) : NestedScrollConnection {
    override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
        val delta = available.y
        if (delta < 0f) {
            val previousOffset = offset()
            if (previousOffset >= 0) {
                val newOffset = (previousOffset + delta).coerceIn(0f, maxOffset)
                onOffsetUpdate(newOffset)
                val consumedOffset = newOffset - previousOffset
                return Offset(x = 0f, y = consumedOffset)
            }
        }
        return super.onPreScroll(available, source)
    }

    override fun onPostScroll(
        consumed: Offset,
        available: Offset,
        source: NestedScrollSource,
    ): Offset {
        val delta = available.y
        val consumedY = consumed.y
        val previousOffset = offset()

        if (delta < 0f || consumedY < 0f) {
            if (previousOffset >= 0) {
                val newOffset = (previousOffset + consumedY).coerceIn(0f, maxOffset)
                onOffsetUpdate(newOffset)
                val consumedOffset = newOffset - previousOffset
                return Offset(0f, consumedOffset)
            }
        }
        if (delta > 0f) {
            val newOffset = (previousOffset + delta).coerceIn(0f, maxOffset)
            onOffsetUpdate(newOffset)
            val consumedOffset = newOffset - previousOffset
            return Offset(0f, consumedOffset)
        }

        return super.onPostScroll(consumed, available, source)
    }

    override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
        val superConsumed = super.onPostFling(consumed, available)
        return superConsumed + settleAppBar(available.y)
    }

    private suspend fun settleAppBar(velocity: Float): Velocity {
        if (offset() < 0.01f) {
            return Velocity.Zero
        }
        var remainingVelocity = velocity

        if (abs(velocity) > 1f) {
            var lastValue = 0f
            AnimationState(initialValue = 0f, initialVelocity = velocity).animateDecay(
                flingAnimationSpec
            ) {
                val delta = value - lastValue
                val newOffset = (offset() + delta).coerceIn(0f, maxOffset)

                onOffsetUpdate(newOffset)
                val consumed = abs(newOffset - offset())
                lastValue = value
                remainingVelocity = this.velocity
                // avoid rounding errors and stop if anything is unconsumed
                if (abs(delta - consumed) > 0.5f) this.cancelAnimation()
            }
        }
        return Velocity(0f, remainingVelocity)
    }
}
