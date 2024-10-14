package com.junkfood.seal.ui.common

import android.view.HapticFeedbackConstants
import android.view.View

object HapticFeedback {
    fun View.slightHapticFeedback() = this.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)

    fun View.longPressHapticFeedback() =
        this.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
}
