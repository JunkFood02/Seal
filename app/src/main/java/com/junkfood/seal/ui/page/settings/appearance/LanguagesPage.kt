package com.junkfood.seal.ui.page.settings.appearance

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForwardIos
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.LocaleListCompat
import com.junkfood.seal.R
import com.junkfood.seal.ui.component.BackButton
import com.junkfood.seal.ui.component.HorizontalDivider
import com.junkfood.seal.ui.component.LargeTopAppBar
import com.junkfood.seal.ui.component.PreferenceSingleChoiceItem
import com.junkfood.seal.ui.component.PreferencesHintCard
import com.junkfood.seal.ui.page.settings.about.weblate
import com.junkfood.seal.ui.theme.SealTheme
import com.junkfood.seal.util.LocaleLanguageCodeMap
import com.junkfood.seal.util.PreferenceUtil
import com.junkfood.seal.util.setLanguage
import com.junkfood.seal.util.toDisplayName
import java.util.Locale


@Composable
fun LanguagePage(onBackPressed: () -> Unit = {}) {
    val selectedLocale by remember { mutableStateOf(Locale.getDefault()) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Intent(Settings.ACTION_APP_LOCALE_SETTINGS).apply {
            val uri = Uri.fromParts("package", context.packageName, null)
            data = uri
        }
    } else {
        Intent()
    }

    val isSystemLocaleSettingsAvailable =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.packageManager.queryIntentActivities(
                intent, PackageManager.MATCH_ALL
            ).isNotEmpty()
        } else {
            false
        }
    LanguagePageImpl(
        onBackPressed = onBackPressed,
        localeSet = LocaleLanguageCodeMap.keys,
        isSystemLocaleSettingsAvailable = isSystemLocaleSettingsAvailable,
        onNavigateToSystemLocaleSettings = {
            if (isSystemLocaleSettingsAvailable) {
                context.startActivity(intent)
            }
        },
        selectedLocale = selectedLocale,
    ) {
        PreferenceUtil.saveLocalePreference(it)
        setLanguage(it)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LanguagePageImpl(
    onBackPressed: () -> Unit = {},
    localeSet: Set<Locale>,
    isSystemLocaleSettingsAvailable: Boolean = false,
    onNavigateToSystemLocaleSettings: () -> Unit,
    selectedLocale: Locale,
    onLanguageSelected: (Locale?) -> Unit = {}
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        rememberTopAppBarState(),
        canScroll = { true }
    )
    val uriHandler = LocalUriHandler.current

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        modifier = Modifier,
                        text = stringResource(id = R.string.language),
                    )
                }, navigationIcon = {
                    BackButton {
                        onBackPressed()
                    }
                }, scrollBehavior = scrollBehavior
            )
        }, content = {
            LazyColumn(
                modifier = Modifier
                    .padding(it)
            ) {
                item {
                    PreferencesHintCard(
                        title = stringResource(R.string.translate),
                        description = stringResource(R.string.translate_desc),
                        icon = Icons.Outlined.Translate,
                    ) { uriHandler.openUri(weblate) }
                }


                item {
                    PreferenceSingleChoiceItem(
                        text = stringResource(id = R.string.follow_system),
                        selected = !localeSet.contains(selectedLocale),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 18.dp)
                    ) { onLanguageSelected(null) }
                }

                for (locale in localeSet) {
                    item {
                        PreferenceSingleChoiceItem(
                            text = locale.toDisplayName(),
                            selected = selectedLocale == locale,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 18.dp)
                        ) { onLanguageSelected(locale) }
                    }
                }


                if (isSystemLocaleSettingsAvailable) {
                    item {
                        HorizontalDivider()
                        Surface(
                            modifier = Modifier.clickable(
                                onClick = onNavigateToSystemLocaleSettings
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(PaddingValues(horizontal = 12.dp, vertical = 18.dp)),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(start = 10.dp)
                                ) {
                                    Text(
                                        text = stringResource(R.string.system_settings),
                                        maxLines = 1,
                                        style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.ArrowForwardIos,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .padding(end = 16.dp)
                                        .size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        })
}

@Preview
@Composable
private fun LanguagePagePreview() {
    var language by remember {
        mutableStateOf(Locale.JAPANESE)
    }
    val map = buildSet<Locale> {
        repeat(38) {
            add(Locale("en"))
        }
    }
    SealTheme {
        LanguagePageImpl(
            localeSet = map,
            isSystemLocaleSettingsAvailable = true,
            onNavigateToSystemLocaleSettings = { /*TODO*/ },
            selectedLocale = language
        ) {
            language = it
        }
    }
}