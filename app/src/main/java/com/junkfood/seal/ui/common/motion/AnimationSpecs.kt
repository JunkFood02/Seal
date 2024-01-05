package com.junkfood.seal.ui.common.motion

import android.graphics.Path
import android.view.animation.PathInterpolator
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.tween
import com.junkfood.seal.ui.common.DURATION_ENTER

fun PathInterpolator.toEasing(): Easing {
    return Easing { f -> this.getInterpolation(f) }
}

private val path = Path().apply {
    moveTo(0f, 0f)
    cubicTo(0.05F, 0F, 0.133333F, 0.06F, 0.166666F, 0.4F)
    cubicTo(0.208333F, 0.82F, 0.25F, 1F, 1F, 1F)
}

private val emphasizePathInterpolator = PathInterpolator(path)
val emphasizeEasing = emphasizePathInterpolator.toEasing()
private val emphasizeEasingVariant = CubicBezierEasing(.2f, 0f, 0f, 1f)
private val emphasizedDecelerate = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)
private val emphasizedAccelerate = CubicBezierEasing(0.3f, 0f, 1f, 1f)

private val standardDecelerate = CubicBezierEasing(.0f, .0f, 0f, 1f)

private val motionEasingStandard = CubicBezierEasing(0.4F, 0.0F, 0.2F, 1F)

private val tweenSpec = tween<Float>(durationMillis = DURATION_ENTER, easing = emphasizeEasing)
