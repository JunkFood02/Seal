package com.junkfood.seal.ui.common.motion

/*
 * Copyright 2021 SOUP
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp

/**
 * Returns the provided [Dp] as an [Int] value by the [LocalDensity].
 *
 * @param slideDistance Value to the slide distance dimension, 30dp by default.
 */
@Composable
public fun rememberSlideDistance(slideDistance: Dp = MotionConstants.DefaultSlideDistance): Int {
    val density = LocalDensity.current
    return remember(density, slideDistance) { with(density) { slideDistance.roundToPx() } }
}

private const val ProgressThreshold = 0.35f

private val Int.ForOutgoing: Int
    get() = (this * ProgressThreshold).toInt()

private val Int.ForIncoming: Int
    get() = this - this.ForOutgoing

/** [materialSharedAxisX] allows to switch a layout with shared X-axis transition. */
public fun materialSharedAxisX(
    initialOffsetX: (fullWidth: Int) -> Int,
    targetOffsetX: (fullWidth: Int) -> Int,
    durationMillis: Int = MotionConstants.DefaultMotionDuration,
): ContentTransform =
    materialSharedAxisXIn(
        initialOffsetX = initialOffsetX,
        durationMillis = durationMillis,
    ) togetherWith
        materialSharedAxisXOut(targetOffsetX = targetOffsetX, durationMillis = durationMillis)

/** [materialSharedAxisXIn] allows to switch a layout with shared X-axis enter transition. */
public fun materialSharedAxisXIn(
    initialOffsetX: (fullWidth: Int) -> Int,
    durationMillis: Int = MotionConstants.DefaultMotionDuration,
): EnterTransition =
    slideInHorizontally(
        animationSpec = tween(durationMillis = durationMillis, easing = FastOutSlowInEasing),
        initialOffsetX = initialOffsetX,
    ) +
        fadeIn(
            animationSpec =
                tween(
                    durationMillis = durationMillis.ForIncoming,
                    delayMillis = durationMillis.ForOutgoing,
                    easing = LinearOutSlowInEasing,
                )
        )

/** [materialSharedAxisXOut] allows to switch a layout with shared X-axis exit transition. */
public fun materialSharedAxisXOut(
    targetOffsetX: (fullWidth: Int) -> Int,
    durationMillis: Int = MotionConstants.DefaultMotionDuration,
): ExitTransition =
    slideOutHorizontally(
        animationSpec = tween(durationMillis = durationMillis, easing = FastOutSlowInEasing),
        targetOffsetX = targetOffsetX,
    ) +
        fadeOut(
            animationSpec =
                tween(
                    durationMillis = durationMillis.ForOutgoing,
                    delayMillis = 0,
                    easing = FastOutLinearInEasing,
                )
        )

/** [materialSharedAxisY] allows to switch a layout with shared Y-axis transition. */
public fun materialSharedAxisY(
    initialOffsetY: (fullWidth: Int) -> Int,
    targetOffsetY: (fullWidth: Int) -> Int,
    durationMillis: Int = MotionConstants.DefaultMotionDuration,
): ContentTransform =
    materialSharedAxisYIn(
        initialOffsetY = initialOffsetY,
        durationMillis = durationMillis,
    ) togetherWith
        materialSharedAxisYOut(targetOffsetY = targetOffsetY, durationMillis = durationMillis)

/** [materialSharedAxisYIn] allows to switch a layout with shared Y-axis enter transition. */
public fun materialSharedAxisYIn(
    initialOffsetY: (fullWidth: Int) -> Int,
    durationMillis: Int = MotionConstants.DefaultMotionDuration,
): EnterTransition =
    slideInVertically(
        animationSpec = tween(durationMillis = durationMillis, easing = FastOutSlowInEasing),
        initialOffsetY = initialOffsetY,
    ) +
        fadeIn(
            animationSpec =
                tween(
                    durationMillis = durationMillis.ForIncoming,
                    delayMillis = durationMillis.ForOutgoing,
                    easing = LinearOutSlowInEasing,
                )
        )

/** [materialSharedAxisYOut] allows to switch a layout with shared Y-axis exit transition. */
public fun materialSharedAxisYOut(
    targetOffsetY: (fullWidth: Int) -> Int,
    durationMillis: Int = MotionConstants.DefaultMotionDuration,
): ExitTransition =
    slideOutVertically(
        animationSpec = tween(durationMillis = durationMillis, easing = FastOutSlowInEasing),
        targetOffsetY = targetOffsetY,
    ) +
        fadeOut(
            animationSpec =
                tween(
                    durationMillis = durationMillis.ForOutgoing,
                    delayMillis = 0,
                    easing = FastOutLinearInEasing,
                )
        )

/**
 * [materialSharedAxisZ] allows to switch a layout with shared Z-axis transition.
 *
 * @param forward whether the direction of the animation is forward.
 * @param durationMillis the duration of transition.
 */
public fun materialSharedAxisZ(
    forward: Boolean,
    durationMillis: Int = MotionConstants.DefaultMotionDuration,
): ContentTransform =
    materialSharedAxisZIn(forward = forward, durationMillis = durationMillis) togetherWith
        materialSharedAxisZOut(forward = forward, durationMillis = durationMillis)

/**
 * [materialSharedAxisZIn] allows to switch a layout with shared Z-axis enter transition.
 *
 * @param forward whether the direction of the animation is forward.
 * @param durationMillis the duration of the enter transition.
 */
public fun materialSharedAxisZIn(
    forward: Boolean,
    durationMillis: Int = MotionConstants.DefaultMotionDuration,
): EnterTransition =
    fadeIn(
        animationSpec =
            tween(
                durationMillis = durationMillis.ForIncoming,
                delayMillis = durationMillis.ForOutgoing,
                easing = LinearOutSlowInEasing,
            )
    ) +
        scaleIn(
            animationSpec = tween(durationMillis = durationMillis, easing = FastOutSlowInEasing),
            initialScale = if (forward) 0.8f else 1.1f,
        )

/**
 * [materialSharedAxisZOut] allows to switch a layout with shared Z-axis exit transition.
 *
 * @param forward whether the direction of the animation is forward.
 * @param durationMillis the duration of the exit transition.
 */
public fun materialSharedAxisZOut(
    forward: Boolean,
    durationMillis: Int = MotionConstants.DefaultMotionDuration,
): ExitTransition =
    fadeOut(
        animationSpec =
            tween(
                durationMillis = durationMillis.ForOutgoing,
                delayMillis = 0,
                easing = FastOutLinearInEasing,
            )
    ) +
        scaleOut(
            animationSpec = tween(durationMillis = durationMillis, easing = FastOutSlowInEasing),
            targetScale = if (forward) 1.1f else 0.8f,
        )
