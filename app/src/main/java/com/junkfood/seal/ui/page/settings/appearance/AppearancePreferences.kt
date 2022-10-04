package com.junkfood.seal.ui.page.settings.appearance

import android.os.Build
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Brightness6
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.android.material.color.DynamicColors
import com.junkfood.seal.R
import com.junkfood.seal.ui.common.LocalDarkTheme
import com.junkfood.seal.ui.common.LocalDynamicColorSwitch
import com.junkfood.seal.ui.common.LocalSeedColor
import com.junkfood.seal.ui.common.Route
import com.junkfood.seal.ui.component.*
import com.junkfood.seal.ui.component.LargeTopAppBar
import com.junkfood.seal.ui.theme.ColorScheme
import com.junkfood.seal.ui.theme.ColorScheme.DEFAULT_SEED_COLOR
import com.junkfood.seal.util.PreferenceUtil
import com.junkfood.seal.util.PreferenceUtil.DarkThemePreference.Companion.FOLLOW_SYSTEM
import com.junkfood.seal.util.PreferenceUtil.DarkThemePreference.Companion.OFF
import com.junkfood.seal.util.PreferenceUtil.DarkThemePreference.Companion.ON
import com.junkfood.seal.util.PreferenceUtil.EMPTY_SEED_COLOR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppearancePreferences(
    navController: NavHostController
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        rememberTopAppBarState(),
        canScroll = { true }
    )
    var showDarkThemeDialog by remember { mutableStateOf(false) }
    val darkTheme = LocalDarkTheme.current
    val dynamicColor = LocalDynamicColorSwitch.current
    var darkThemeValue by remember { mutableStateOf(darkTheme.darkThemeValue) }

    val switchDynamicColor: (Boolean) -> Unit = {
        if (!it) {
            PreferenceUtil.switchDynamicColor(ON)
            PreferenceUtil.modifyThemeSeedColor(EMPTY_SEED_COLOR)
        } else {
            PreferenceUtil.switchDynamicColor(OFF)
            PreferenceUtil.modifyThemeSeedColor(DEFAULT_SEED_COLOR)
        }
    }

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
                    .verticalScroll(rememberScrollState())
            ) {
                VideoCard(modifier = Modifier.padding(18.dp))
//                CardPreview()
                Column {
                    Row(
                        modifier = Modifier
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 14.dp, vertical = 12.dp)
                    ) {
                        if (DynamicColors.isDynamicColorAvailable()) {
                            ColorButton(color = dynamicDarkColorScheme(LocalContext.current).primary)
                        }
                        ColorButton(color = Color(ColorScheme.DEFAULT_SEED_COLOR))
                        ColorButton(color = Color.Yellow)
                        ColorButton(
                            color = Color(
                                material.io.color.hct.Hct.from(60.0, 150.0, 70.0).toInt()
                            )
                        )
                        ColorButton(
                            color = Color(
                                material.io.color.hct.Hct.from(125.0, 50.0, 60.0).toInt()
                            )
                        )
                        ColorButton(color = Color.Red)
                        ColorButton(color = Color.Magenta)
                        ColorButton(color = Color.Blue)
                    }
                }
                PreferenceSwitch(
                    title = stringResource(id = R.string.dynamic_color),
                    description = stringResource(
                        id = R.string.dynamic_color_des
                    ),
                    icon = Icons.Outlined.Brightness6,
                    enabled = LocalDynamicColorSwitch.current.enable,
                    isChecked = LocalDynamicColorSwitch.current.dynamicColorSwitch == ON,
                    onClick = {
                        switchDynamicColor(dynamicColor.getDynamicSwtich())
                    }
                )

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
                Column {
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
    val corePalette = material.io.color.palettes.CorePalette.of(color.toArgb())
    val lightColor = corePalette.a2.tone(80)
    val seedColor = corePalette.a2.tone(60)
    val darkColor = corePalette.a2.tone(60)

    val showColor = if (LocalDarkTheme.current.isDarkTheme()) darkColor else lightColor
    val currentColor = LocalSeedColor.current == seedColor
    val state = animateDpAsState(targetValue = if (currentColor) 48.dp else 36.dp)
    val state2 = animateDpAsState(targetValue = if (currentColor) 18.dp else 0.dp)
    ElevatedCard(modifier = modifier
        .clearAndSetSemantics { }
        .padding(4.dp)
        .size(72.dp), onClick = {
        PreferenceUtil.switchDynamicColor(OFF)
        PreferenceUtil.modifyThemeSeedColor(seedColor)
    }) {
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