package com.junkfood.seal.ui.page.settings.appearance

import android.os.Build
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.android.material.color.DynamicColors
import com.junkfood.seal.R
import com.junkfood.seal.ui.color.hct.Hct
import com.junkfood.seal.ui.color.palettes.CorePalette
import com.junkfood.seal.ui.common.LocalDarkTheme
import com.junkfood.seal.ui.common.LocalSeedColor
import com.junkfood.seal.ui.common.Route
import com.junkfood.seal.ui.component.*
import com.junkfood.seal.ui.component.LargeTopAppBar
import com.junkfood.seal.ui.page.download.VideoCard
import com.junkfood.seal.ui.theme.ColorScheme
import com.junkfood.seal.util.PreferenceUtil
import com.junkfood.seal.util.PreferenceUtil.DarkThemePreference.Companion.FOLLOW_SYSTEM
import com.junkfood.seal.util.PreferenceUtil.DarkThemePreference.Companion.OFF
import com.junkfood.seal.util.PreferenceUtil.DarkThemePreference.Companion.ON

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppearancePreferences(
    navController: NavHostController
) {
    val decayAnimationSpec = rememberSplineBasedDecay<Float>()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        decayAnimationSpec,
        rememberTopAppBarState(),
        canScroll = { true }
    )
    var showDarkThemeDialog by remember { mutableStateOf(false) }
    val darkTheme = LocalDarkTheme.current
    var darkThemeValue by remember { mutableStateOf(darkTheme.darkThemeValue) }



    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        modifier = Modifier.padding(start = 8.dp),
                        text = stringResource(id = R.string.display),
                    )
                }, navigationIcon = {
                    BackButton(modifier = Modifier.padding(start = 8.dp)) {
                        navController.popBackStack()
                    }
                }, scrollBehavior = scrollBehavior
            )
        }, content = {
            Column(
                Modifier
                    .padding(it)
                    .padding(
                        bottom = WindowInsets.systemBars
                            .asPaddingValues()
                            .calculateBottomPadding()
                    )
                    .verticalScroll(rememberScrollState())
            ) {
                VideoCard(
                    modifier = Modifier.padding(18.dp),
                    thumbnailUrl = R.drawable.sample,
                    onClick = {},
                    title = "Video title sample text",
                    author = "Video creator sample text",
                    progress = 100f
                )
                Column {
                    Row(
                        modifier = Modifier
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 14.dp, vertical = 12.dp)
                    ) {
                        if (DynamicColors.isDynamicColorAvailable()) {
                            ColorButton(color = dynamicDarkColorScheme(LocalContext.current).primary)
                            ColorButton(color = dynamicDarkColorScheme(LocalContext.current).tertiary)
                        }
                        ColorButton(color = Color(ColorScheme.DEFAULT_SEED_COLOR))
                        ColorButton(color = Color.Yellow)
                        ColorButton(color = Color(Hct.from(60.0, 150.0, 70.0).toInt()))
                        ColorButton(color = Color(Hct.from(125.0, 50.0, 60.0).toInt()))
                        ColorButton(color = Color.Red)
                        ColorButton(color = Color.Magenta)
                        ColorButton(color = Color.Blue)
                    }
                }

                PreferenceItem(
                    title = stringResource(id = R.string.dark_theme),
                    description = LocalDarkTheme.current.getDarkThemeDesc(),
                    icon = Icons.Outlined.DarkMode,
                    enabled = true
                ) { showDarkThemeDialog = true }
                if (Build.VERSION.SDK_INT >= 24)
                    PreferenceItem(
                        title = stringResource(R.string.language),
                        icon = Icons.Outlined.Language,
                        description = PreferenceUtil.getLanguageDesc()
                    ) { navController.navigate(Route.LANGUAGES) }
            }
        })
    if (showDarkThemeDialog)
        AlertDialog(onDismissRequest = {
            showDarkThemeDialog = false
            darkThemeValue = darkTheme.darkThemeValue
        }, confirmButton = {
            ConfirmButton {
                showDarkThemeDialog = false
                PreferenceUtil.switchDarkThemeMode(darkThemeValue)
            }
        }, dismissButton = {
            DismissButton {
                showDarkThemeDialog = false
                darkThemeValue = darkTheme.darkThemeValue
            }
        }, icon = { Icon(Icons.Outlined.DarkMode, null) },
            title = { Text(stringResource(R.string.dark_theme)) }, text = {
                Column() {
                    SingleChoiceItem(
                        text = stringResource(R.string.follow_system),
                        selected = darkThemeValue == FOLLOW_SYSTEM
                    ) {
                        darkThemeValue = FOLLOW_SYSTEM
                    }
                    SingleChoiceItem(
                        text = stringResource(R.string.on),
                        selected = darkThemeValue == ON
                    ) {
                        darkThemeValue = ON
                    }
                    SingleChoiceItem(
                        text = stringResource(R.string.off),
                        selected = darkThemeValue == OFF
                    ) {
                        darkThemeValue = OFF
                    }
                }
            })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorButton(modifier: Modifier = Modifier, color: Color) {
    val corePalette = CorePalette.of(color.toArgb())
    val lightColor = corePalette.a2.tone(80)
    val seedColor = corePalette.a2.tone(60)
    val darkColor = corePalette.a2.tone(60)

    val showColor = if (LocalDarkTheme.current.isDarkTheme()) darkColor else lightColor
    val currentColor = LocalSeedColor.current == seedColor
    val state = animateDpAsState(targetValue = if (currentColor) 48.dp else 36.dp)
    val state2 = animateDpAsState(targetValue = if (currentColor) 18.dp else 0.dp)
    ElevatedCard(modifier = modifier
        .padding(4.dp)
        .size(72.dp), onClick = { PreferenceUtil.modifyThemeSeedColor(seedColor) }) {
        Box(Modifier.fillMaxSize()) {
            Box(
                modifier = modifier
                    .size(state.value)
                    .clip(CircleShape)
                    .background(Color(showColor))
                    .align(Alignment.Center)
            ) {

                Icon(
                    Icons.Outlined.Check,
                    null,
                    modifier = Modifier
                        .size(state2.value)
                        .align(Alignment.Center)
                        .clip(CircleShape),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }

}