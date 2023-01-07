package com.junkfood.seal.ui.common


import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.IntOffset
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDeepLink
import androidx.navigation.NavGraphBuilder
import com.google.accompanist.navigation.animation.composable

@OptIn(ExperimentalAnimationApi::class)
fun NavGraphBuilder.animatedComposable(
    route: String,
    arguments: List<NamedNavArgument> = emptyList(),
    deepLinks: List<NavDeepLink> = emptyList(),
    content: @Composable AnimatedVisibilityScope.(NavBackStackEntry) -> Unit
) = composable(
    route = route,
    arguments = arguments,
    deepLinks = deepLinks,
    enterTransition = {
        fadeIn(animationSpec = tween(220, delayMillis = 90)) +
                scaleIn(
                    initialScale = 0.92f,
                    animationSpec = tween(220, delayMillis = 90)
                )
    },
    exitTransition = {
        fadeOut(animationSpec = tween(90))
    },
    popEnterTransition = {
        fadeIn(animationSpec = tween(220, delayMillis = 90)) +
                scaleIn(
                    initialScale = 0.92f,
                    animationSpec = tween(220, delayMillis = 90)
                )
    },
    popExitTransition = {
        fadeOut(animationSpec = tween(90))
    },
    content = content
)

const val duration = 350
const val initialOffset = 0.08f
//cubic-bezier(.4,.75,.45,1)
val easing = CubicBezierEasing(.2f,.7f,.2f,1f)
val tweenSpec = tween<Float>(durationMillis = duration, easing = easing)
val tweenSpecInt = tween<IntOffset>(durationMillis = duration, easing = easing)
val springSpec = spring<Float>(
    dampingRatio = Spring.DampingRatioNoBouncy,
    stiffness = Spring.StiffnessMedium,
)

fun slideInHorizontally(
    animationSpec: FiniteAnimationSpec<IntOffset> =
        spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium,
            visibilityThreshold = IntOffset.VisibilityThreshold
        ),
    isPop: Boolean = false,
    initialOffsetX: (fullWidth: Int) -> Int = {
        (it * initialOffset).toInt().run {
            if (isPop) -this else this
        }
    },
): EnterTransition =
    slideIn(
        initialOffset = { IntOffset(initialOffsetX(it.width), 0) },
        animationSpec = animationSpec
    )

fun slideOutHorizontally(
    animationSpec: FiniteAnimationSpec<IntOffset> =
        spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium,
            visibilityThreshold = IntOffset.VisibilityThreshold
        ),
    isPop: Boolean = false,
    targetOffsetX: (fullWidth: Int) -> Int = {
        (it * initialOffset).toInt().run {
            if (isPop) this else -this
        }
    },
): ExitTransition =
    slideOut(
        targetOffset = { IntOffset(targetOffsetX(it.width), 0) },
        animationSpec = animationSpec
    )

@OptIn(ExperimentalAnimationApi::class)
fun NavGraphBuilder.slideInHorizontallyComposable(
    route: String,
    arguments: List<NamedNavArgument> = emptyList(),
    deepLinks: List<NavDeepLink> = emptyList(),
    content: @Composable AnimatedVisibilityScope.(NavBackStackEntry) -> Unit
) = composable(
    route = route,
    arguments = arguments,
    deepLinks = deepLinks,
    enterTransition = { slideInHorizontally(tweenSpecInt) + fadeIn(springSpec) },
    exitTransition = { slideOutHorizontally(tweenSpecInt) + fadeOut(springSpec) },
    popEnterTransition = { slideInHorizontally(tweenSpecInt, isPop = true) + fadeIn(springSpec) },
    popExitTransition = { slideOutHorizontally(tweenSpecInt, isPop = true) + fadeOut(springSpec) },
    content = content
)

fun slideInVertically(
    animationSpec: FiniteAnimationSpec<IntOffset> =
        spring(
            stiffness = Spring.StiffnessMediumLow,
            visibilityThreshold = IntOffset.VisibilityThreshold
        ),
    initialOffsetY: (fullHeight: Int) -> Int = { it },
): EnterTransition =
    slideIn(
        initialOffset = { IntOffset(0, initialOffsetY(it.height)) },
        animationSpec = animationSpec
    )

fun slideOutVertically(
    animationSpec: FiniteAnimationSpec<IntOffset> =
        spring(
            stiffness = Spring.StiffnessMediumLow,
            visibilityThreshold = IntOffset.VisibilityThreshold
        ),
    targetOffsetY: (fullHeight: Int) -> Int = { it },
): ExitTransition =
    slideOut(
        targetOffset = { IntOffset(0, targetOffsetY(it.height)) },
        animationSpec = animationSpec
    )

@OptIn(ExperimentalAnimationApi::class)
fun NavGraphBuilder.slideInVerticallyComposable(
    route: String,
    arguments: List<NamedNavArgument> = emptyList(),
    deepLinks: List<NavDeepLink> = emptyList(),
    content: @Composable AnimatedVisibilityScope.(NavBackStackEntry) -> Unit
) = composable(
    route = route,
    arguments = arguments,
    deepLinks = deepLinks,
    enterTransition = { slideInVertically() },
    exitTransition = { slideOutVertically() },
    popEnterTransition = { slideInVertically() },
    popExitTransition = { slideOutVertically() },
    content = content
)
