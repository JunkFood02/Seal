package com.junkfood.seal.ui.common


import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.IntOffset
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDeepLink
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.junkfood.seal.ui.common.motion.emphasizeEasing
import com.junkfood.seal.ui.common.motion.materialSharedAxisXIn
import com.junkfood.seal.ui.common.motion.materialSharedAxisXOut

@OptIn(ExperimentalAnimationApi::class)
fun NavGraphBuilder.fadeThroughComposable(
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

const val DURATION_ENTER = 400
const val DURATION_EXIT = 200
const val initialOffset = 0.10f


private val enterTween =
    tween<IntOffset>(durationMillis = DURATION_ENTER, easing = emphasizeEasing)
private val exitTween =
    tween<IntOffset>(durationMillis = DURATION_ENTER, easing = emphasizeEasing)

private val fadeSpring = spring<Float>(
    dampingRatio = Spring.DampingRatioNoBouncy,
    stiffness = Spring.StiffnessMedium,
)
private val fadeTween = tween<Float>(durationMillis = DURATION_EXIT)

private val fadeSpec = fadeTween

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
        materialSharedAxisXIn(initialOffsetX = { (it * initialOffset).toInt() })
    },
    exitTransition = {
        materialSharedAxisXOut(targetOffsetX = { -(it * initialOffset).toInt() })
    },
    popEnterTransition = {
        materialSharedAxisXIn(initialOffsetX = { -(it * initialOffset).toInt() })
    },
    popExitTransition = {
        materialSharedAxisXOut(targetOffsetX = { (it * initialOffset).toInt() })
    },
    content = content
)


fun NavGraphBuilder.animatedComposableLegacy(
    route: String,
    arguments: List<NamedNavArgument> = emptyList(),
    deepLinks: List<NavDeepLink> = emptyList(),
    content: @Composable AnimatedVisibilityScope.(NavBackStackEntry) -> Unit
) = composable(
    route = route,
    arguments = arguments,
    deepLinks = deepLinks,
    enterTransition = {
        slideInHorizontally(
            enterTween,
            initialOffsetX = { (it * initialOffset).toInt() }) + fadeIn(fadeSpec)
    },
    exitTransition = {
        slideOutHorizontally(
            exitTween,
            targetOffsetX = { -(it * initialOffset).toInt() }) + fadeOut(fadeSpec)
    },
    popEnterTransition = {
        slideInHorizontally(
            enterTween,
            initialOffsetX = { -(it * initialOffset).toInt() }) + fadeIn(fadeSpec)
    },
    popExitTransition = {
        slideOutHorizontally(
            exitTween,
            targetOffsetX = { (it * initialOffset).toInt() }) + fadeOut(fadeSpec)
    },
    content = content
)


fun NavGraphBuilder.animatedComposableVariant(
    route: String,
    arguments: List<NamedNavArgument> = emptyList(),
    deepLinks: List<NavDeepLink> = emptyList(),
    content: @Composable AnimatedVisibilityScope.(NavBackStackEntry) -> Unit
) = composable(
    route = route,
    arguments = arguments,
    deepLinks = deepLinks,
    enterTransition = {
        slideInHorizontally(
            enterTween,
            initialOffsetX = { (it * initialOffset).toInt() }) + fadeIn(fadeSpec)
    },
    exitTransition = {
        fadeOut(fadeSpec)
    },
    popEnterTransition = {
        fadeIn(fadeSpec)
    },
    popExitTransition = {
        slideOutHorizontally(
            exitTween,
            targetOffsetX = { (it * initialOffset).toInt() }) + fadeOut(fadeSpec)
    },
    content = content
)

//fun slideInVertically(
//    animationSpec: FiniteAnimationSpec<IntOffset> =
//        spring(
//            stiffness = Spring.StiffnessMedium,
//            visibilityThreshold = IntOffset.VisibilityThreshold
//        ),
//    initialOffsetY: (fullHeight: Int) -> Int = { it },
//): EnterTransition =
//    slideIn(
//        initialOffset = { IntOffset(0, initialOffsetY(it.height)) },
//        animationSpec = animationSpec
//    )

val springSpec = spring(
    stiffness = Spring.StiffnessMedium,
    visibilityThreshold = IntOffset.VisibilityThreshold
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
    enterTransition = {
        slideInVertically(
            initialOffsetY = { it }, animationSpec = enterTween
        ) + fadeIn()
    },
    exitTransition = { slideOutVertically() },
    popEnterTransition = { slideInVertically() },
    popExitTransition = {
        slideOutVertically(
            targetOffsetY = { it },
            animationSpec = enterTween
        ) + fadeOut()
    },
    content = content
)
