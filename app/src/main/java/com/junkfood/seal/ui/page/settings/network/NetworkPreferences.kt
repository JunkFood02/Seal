package com.junkfood.seal.ui.page.settings.network

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Cookie
import androidx.compose.material.icons.outlined.OfflineBolt
import androidx.compose.material.icons.outlined.SignalCellular4Bar
import androidx.compose.material.icons.outlined.SignalCellularConnectedNoInternet4Bar
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.junkfood.seal.R
import com.junkfood.seal.ui.component.BackButton
import com.junkfood.seal.ui.component.LargeTopAppBar
import com.junkfood.seal.ui.component.PreferenceItem
import com.junkfood.seal.ui.component.PreferenceSubtitle
import com.junkfood.seal.ui.component.PreferenceSwitch
import com.junkfood.seal.ui.component.PreferenceSwitchWithDivider
import com.junkfood.seal.util.PreferenceUtil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetworkPreferences(
    navigateToCookieGeneratorPage: () -> Unit = {},
    onBackPressed: () -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        rememberTopAppBarState(),
        canScroll = { true }
    )

    var showConcurrentDownloadDialog by remember { mutableStateOf(false) }
    var showCookiesDialog by rememberSaveable { mutableStateOf(false) }
    var showRateLimitDialog by remember { mutableStateOf(false) }
    var aria2c by remember { mutableStateOf(PreferenceUtil.getValue(PreferenceUtil.ARIA2C)) }
    var isCookiesEnabled by remember {
        mutableStateOf(PreferenceUtil.getValue(PreferenceUtil.COOKIES))
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
                        text = stringResource(id = R.string.network),
                    )
                }, navigationIcon = {
                    BackButton(modifier = Modifier.padding(start = 8.dp)) {
                        onBackPressed()
                    }
                }, scrollBehavior = scrollBehavior
            )
        }, content = {
            LazyColumn(Modifier.padding(it)) {
                item {
                    PreferenceSubtitle(text = stringResource(R.string.general_settings))
                }
                item {
                    var isRateLimitEnabled by remember {
                        mutableStateOf(PreferenceUtil.getValue(PreferenceUtil.RATE_LIMIT))
                    }

                    PreferenceSwitchWithDivider(
                        title = stringResource(R.string.rate_limit),
                        description = stringResource(R.string.rate_limit_desc),
                        icon = Icons.Outlined.Speed,
                        isChecked = isRateLimitEnabled,
                        onChecked = {
                            isRateLimitEnabled = !isRateLimitEnabled
                            PreferenceUtil.updateValue(
                                PreferenceUtil.RATE_LIMIT,
                                isRateLimitEnabled
                            )
                        },
                        onClick = { showRateLimitDialog = true }
                    )
                }
                item {
                    var isDownloadWithCellularEnabled by remember {
                        mutableStateOf(PreferenceUtil.getValue(PreferenceUtil.CELLULAR_DOWNLOAD))
                    }
                    PreferenceSwitch(
                        title = stringResource(R.string.download_with_cellular),
                        description = stringResource(R.string.download_with_cellular_desc),
                        icon = if (isDownloadWithCellularEnabled) Icons.Outlined.SignalCellular4Bar
                        else Icons.Outlined.SignalCellularConnectedNoInternet4Bar,
                        isChecked = isDownloadWithCellularEnabled,
                        onClick = {
                            isDownloadWithCellularEnabled = !isDownloadWithCellularEnabled
                            PreferenceUtil.updateValue(
                                PreferenceUtil.CELLULAR_DOWNLOAD,
                                isDownloadWithCellularEnabled
                            )
                        }
                    )
                }

                item {
                    PreferenceSubtitle(text = stringResource(id = R.string.advanced_settings))
                }

                item {
                    PreferenceSwitch(
                        title = stringResource(R.string.aria2),
                        icon = Icons.Outlined.Bolt,
                        description = stringResource(
                            R.string.aria2_desc
                        ),
                        isChecked = aria2c,
                        onClick = {
                            aria2c = !aria2c
                            PreferenceUtil.updateValue(PreferenceUtil.ARIA2C, aria2c)
                        }
                    )
                }
                item {
                    PreferenceItem(
                        title = stringResource(id = R.string.concurrent_download),
                        description = stringResource(R.string.concurrent_download_desc),
                        icon = Icons.Outlined.OfflineBolt,
                        enabled = !aria2c,
                    ) { showConcurrentDownloadDialog = true }
                }
                item {
                    PreferenceSwitchWithDivider(title = stringResource(R.string.cookies),
                        description = stringResource(R.string.cookies_desc),
                        isChecked = isCookiesEnabled,
                        icon = Icons.Outlined.Cookie,
                        onChecked = {
                            isCookiesEnabled = !isCookiesEnabled
                            PreferenceUtil.updateValue(PreferenceUtil.COOKIES, isCookiesEnabled)
                        }, onClick = { showCookiesDialog = true })
                }

            }
        })

    if (showConcurrentDownloadDialog) {
        ConcurrentDownloadDialog {
            showConcurrentDownloadDialog = false
        }
    }

    if (showCookiesDialog) {
        CookiesDialog(navigateToCookieGeneratorPage = navigateToCookieGeneratorPage) {
            showCookiesDialog = false
        }
    }
    if (showRateLimitDialog) {
        RateLimitDialog {
            showRateLimitDialog = false
        }
    }
}