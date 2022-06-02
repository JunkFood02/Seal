package com.junkfood.seal.ui.page.settings.appearance

import android.os.Build
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.junkfood.seal.MainActivity
import com.junkfood.seal.R
import com.junkfood.seal.ui.component.*
import com.junkfood.seal.ui.component.LargeTopAppBar
import com.junkfood.seal.ui.core.LocalDarkTheme
import com.junkfood.seal.ui.core.LocalDynamicColor
import com.junkfood.seal.util.PreferenceUtil
import com.junkfood.seal.util.PreferenceUtil.DarkThemePreference.Companion.FOLLOW_SYSTEM
import com.junkfood.seal.util.PreferenceUtil.DarkThemePreference.Companion.OFF
import com.junkfood.seal.util.PreferenceUtil.DarkThemePreference.Companion.ON
import com.junkfood.seal.util.PreferenceUtil.LANGUAGE
import com.junkfood.seal.util.PreferenceUtil.getLanguageConfiguration

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppearancePreferences(
    navController: NavController,
) {
    val decayAnimationSpec = rememberSplineBasedDecay<Float>()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        decayAnimationSpec,
        rememberTopAppBarScrollState()
    )
    var showDarkThemeDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    val darkTheme = LocalDarkTheme.current
    var darkThemeValue by remember { mutableStateOf(darkTheme.darkThemeValue) }
    var language by remember { mutableStateOf(PreferenceUtil.getInt(LANGUAGE, 0)) }

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
                    IconButton(
                        modifier = Modifier.padding(start = 8.dp),
                        onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Outlined.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }, scrollBehavior = scrollBehavior
            )
        }, content = {
            Column(
                Modifier
                    .padding(it)
                    .verticalScroll(rememberScrollState())
            ) {
                PreferenceSwitch(
                    title = stringResource(id = R.string.dynamic_color),
                    description = stringResource(R.string.dynamic_color_desc),
                    icon = Icons.Outlined.Palette,
                    onClick = {
                        PreferenceUtil.dynamicColorSwitch()
                    }, enabled = Build.VERSION.SDK_INT >= 31,
                    isChecked = LocalDynamicColor.current.and(Build.VERSION.SDK_INT >= 31),
                    checkedIcon = Icons.Outlined.Check
                )

                PreferenceItem(
                    title = stringResource(id = R.string.dark_theme),
                    description = LocalDarkTheme.current.getDarkThemeDesc(),
                    icon = Icons.Outlined.DarkMode,
                    enabled = true
                ) {
                    showDarkThemeDialog = true
                }
                PreferenceItem(
                    title = stringResource(R.string.language),
                    icon = Icons.Outlined.Language,
                    description = PreferenceUtil.getLanguageDesc()
                ) {
                    showLanguageDialog = true
                }
            }
        })
    if (showDarkThemeDialog)
        AlertDialog(onDismissRequest = {
            showDarkThemeDialog = false
            darkThemeValue = darkTheme.darkThemeValue
        }, confirmButton = {
            ConfirmButton {
                showDarkThemeDialog = false
                darkTheme.switch(darkThemeValue)
            }
        }, dismissButton = {
            DismissButton {
                showDarkThemeDialog = false
                darkThemeValue = darkTheme.darkThemeValue
            }
        }, title = { Text(stringResource(R.string.dark_theme)) }, text = {
            Column() {
                SingleChoiceItem(
                    text = stringResource(R.string.follow_system),
                    selected = darkThemeValue == FOLLOW_SYSTEM
                ) {
                    darkThemeValue = FOLLOW_SYSTEM
                }
                SingleChoiceItem(
                    text = stringResource(androidx.compose.ui.R.string.on),
                    selected = darkThemeValue == ON
                ) {
                    darkThemeValue = ON
                }
                SingleChoiceItem(
                    text = stringResource(androidx.compose.ui.R.string.off),
                    selected = darkThemeValue == OFF
                ) {
                    darkThemeValue = OFF
                }
            }
        })

    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = {
                showLanguageDialog = false
                language = PreferenceUtil.getInt(LANGUAGE, 0)
            },
            confirmButton = {
                ConfirmButton {
                    showLanguageDialog = false
                    PreferenceUtil.updateInt(LANGUAGE, language)
                    MainActivity.setLanguage(getLanguageConfiguration())
                }
            }, dismissButton = {
                DismissButton {
                    showLanguageDialog = false
                    language = PreferenceUtil.getInt(LANGUAGE, 0)
                }
            },
            title = { Text(stringResource(R.string.language_settings)) }, text = {
                Column {
                    SingleChoiceItem(
                        text = stringResource(R.string.la_en_US),
                        selected = language == 2
                    ) { language = 2 }
                    SingleChoiceItem(
                        text = stringResource(R.string.la_zh_CN),
                        selected = language == 1
                    ) { language = 1 }
                }
            }
        )
    }
}