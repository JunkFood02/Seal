package com.junkfood.seal.ui.page.settings.network

import android.content.res.Configuration
import android.webkit.CookieManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Cookie
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.FileCopy
import androidx.compose.material.icons.outlined.GeneratingTokens
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.junkfood.seal.R
import com.junkfood.seal.database.objects.CookieProfile
import com.junkfood.seal.ui.common.HapticFeedback.slightHapticFeedback
import com.junkfood.seal.ui.common.booleanState
import com.junkfood.seal.ui.component.BackButton
import com.junkfood.seal.ui.component.ConfirmButton
import com.junkfood.seal.ui.component.DialogSwitchItem
import com.junkfood.seal.ui.component.DismissButton
import com.junkfood.seal.ui.component.HelpDialog
import com.junkfood.seal.ui.component.HorizontalDivider
import androidx.compose.material3.LargeTopAppBar
import com.junkfood.seal.ui.component.PasteFromClipBoardButton
import com.junkfood.seal.ui.component.PreferenceItemVariant
import com.junkfood.seal.ui.component.PreferenceSwitchWithContainer
import com.junkfood.seal.ui.component.SealDialog
import com.junkfood.seal.ui.component.TextButtonWithIcon
import com.junkfood.seal.ui.theme.SealTheme
import com.junkfood.seal.ui.theme.generateLabelColor
import com.junkfood.seal.util.COOKIES
import com.junkfood.seal.util.DownloadUtil
import com.junkfood.seal.util.DownloadUtil.toCookiesFileContent
import com.junkfood.seal.util.FileUtil
import com.junkfood.seal.util.FileUtil.getCookiesFile
import com.junkfood.seal.util.PreferenceUtil
import com.junkfood.seal.util.PreferenceUtil.getBoolean
import com.junkfood.seal.util.PreferenceUtil.updateBoolean
import com.junkfood.seal.util.USER_AGENT
import com.junkfood.seal.util.matchUrlFromClipboard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CookieProfilePage(
    cookiesViewModel: CookiesViewModel = koinViewModel(),
    navigateToCookieGeneratorPage: () -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        rememberTopAppBarState(),
        canScroll = { true })
    val cookies = cookiesViewModel.cookiesFlow.collectAsState(emptyList()).value
    val scope = rememberCoroutineScope()
    val hapticFeedback = LocalHapticFeedback.current
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val state by cookiesViewModel.stateFlow.collectAsStateWithLifecycle()
    var showClearCookieDialog by remember { mutableStateOf(false) }
    var isCookieEnabled by remember { mutableStateOf(COOKIES.getBoolean()) }
    val cookieManager = CookieManager.getInstance()
    var showHelpDialog by remember { mutableStateOf(false) }
    val view = LocalView.current

    var cookieList by remember {
        mutableStateOf(listOf<Cookie>())
    }

    var shouldUpdateCookies by remember {
        mutableStateOf(false)
    }

    DisposableEffect(shouldUpdateCookies) {
        scope.launch(Dispatchers.IO) {
            DownloadUtil.getCookieListFromDatabase().getOrNull()?.let {
                cookieList = it
                FileUtil.writeContentToFile(it.toCookiesFileContent(), context.getCookiesFile())
            }
        }
        onDispose {
            shouldUpdateCookies = false
        }
    }


    val exportLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.CreateDocument("text/plain")) { uri ->
            uri?.let {
                scope.launch(Dispatchers.IO) {
                    context.contentResolver.openOutputStream(uri)?.use {
                        it.write(cookieList.toCookiesFileContent().toByteArray())
                    }
                }
            }
        }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(title = {
                Text(
                    modifier = Modifier,
                    text = stringResource(id = R.string.cookies),
                )
            }, navigationIcon = {
                BackButton {
                    onNavigateBack()
                }
            }, actions = {
                var expanded by remember { mutableStateOf(false) }
                IconButton(onClick = { showHelpDialog = true }) {
                    Icon(
                        imageVector = Icons.Outlined.HelpOutline,
                        contentDescription = stringResource(R.string.how_does_it_work)
                    )
                }
                IconButton(onClick = { expanded = true }) {
                    Icon(
                        Icons.Outlined.MoreVert,
                        contentDescription = stringResource(
                            R.string.show_more_actions
                        )
                    )
                }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    var userAgent by USER_AGENT.booleanState
                    fun toggleUserAgent(boolean: Boolean = !userAgent) {
                        expanded = false
                        userAgent = boolean
                        USER_AGENT.updateBoolean(boolean)
                    }
                    DropdownMenuItem(
                        modifier = Modifier.toggleable(
                            value = userAgent,
                            onValueChange = ::toggleUserAgent
                        ),
                        leadingIcon = {
                            Checkbox(
                                checked = userAgent,
                                onCheckedChange = null,
                                modifier = Modifier.clearAndSetSemantics { })
                        },
                        text = { Text(stringResource(id = R.string.ua_header)) },
                        onClick = ::toggleUserAgent,
                    )
                    DropdownMenuItem(
                        leadingIcon = { Icon(Icons.Outlined.FileCopy, null) },
                        text = {
                            Text(stringResource(id = R.string.export_to_file))
                        },
                        enabled = cookieList.isNotEmpty(),
                        onClick = {
                            expanded = false
                            exportLauncher.launch("cookies_exported${System.currentTimeMillis()}.txt")
                        })
                    DropdownMenuItem(
                        leadingIcon = { Icon(Icons.Outlined.DeleteForever, null) },
                        text = {
                            Text(stringResource(id = R.string.clear_all_cookies))
                        },
                        onClick = {
                            expanded = false
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            showClearCookieDialog = true
                        })

                }
            }, scrollBehavior = scrollBehavior)
        },
    )
    { paddingValues ->
        LazyColumn(modifier = Modifier, contentPadding = paddingValues) {
            item {
                PreferenceSwitchWithContainer(
                    title = stringResource(R.string.use_cookies),
                    icon = null,
                    isChecked = isCookieEnabled,
                    onClick = {
                        if (isCookieEnabled) {
                            isCookieEnabled = false
                            COOKIES.updateBoolean(false)
                        } else if ((cookies.isEmpty() || !cookieManager.hasCookies()) && !isCookieEnabled) {
                            showHelpDialog = true
                        } else {
                            isCookieEnabled = true
                            COOKIES.updateBoolean(true)
                        }
                    })
            }
            itemsIndexed(cookies) { _, item ->
                PreferenceItemVariant(
                    modifier = Modifier.padding(vertical = 4.dp),
                    title = item.url,
                    onClick = { cookiesViewModel.showEditCookieDialog(item) },
                    onClickLabel = stringResource(
                        id = R.string.edit
                    ), onLongClick = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        cookiesViewModel.showDeleteCookieDialog(item)
                    }, onLongClickLabel = stringResource(R.string.remove)
                )
            }

            item {
                PreferenceItemVariant(
                    title = stringResource(id = R.string.generate_new_cookies),
                    icon = Icons.Outlined.Add
                ) { cookiesViewModel.showEditCookieDialog() }
            }
            item {
                HorizontalDivider()
                val cookiesCount = cookieList.size
                val siteCount = cookieList.distinctBy { it.domain }.size
                Text(
                    text = stringResource(R.string.cookies_in_database, cookiesCount, siteCount),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

    }
    if (state.showEditDialog) {
        CookieGeneratorDialog(
            cookiesViewModel = cookiesViewModel,
            navigateToCookieGeneratorPage = navigateToCookieGeneratorPage
        ) {
            cookiesViewModel.hideDialog()
            shouldUpdateCookies = true
        }
    }

    if (state.showDeleteDialog) {
        DeleteCookieDialog(cookiesViewModel) { cookiesViewModel.hideDialog() }
    }

    if (showHelpDialog) {
        HelpDialog(text = stringResource(id = R.string.cookies_usage_msg), onDismissRequest = {
            showHelpDialog = false
        })
    }
    if (showClearCookieDialog) {
        ClearCookiesDialog(onDismissRequest = { showClearCookieDialog = false }) {
            view.slightHapticFeedback()
            scope.launch(Dispatchers.IO) {
                CookieManager.getInstance().removeAllCookies(null)
            }.invokeOnCompletion {
                shouldUpdateCookies = true
            }
        }
    }
}

@OptIn(
    ExperimentalMaterial3Api::class
)
@Composable
fun CookieGeneratorDialog(
    cookiesViewModel: CookiesViewModel = viewModel(),
    navigateToCookieGeneratorPage: () -> Unit = {},
    onDismissRequest: () -> Unit
) {

    val state by cookiesViewModel.stateFlow.collectAsStateWithLifecycle()
    val profile = state.editingCookieProfile
    val url = profile.url

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            CookieManager.getInstance().flush()
        }
    }
    AlertDialog(onDismissRequest = onDismissRequest, icon = {
        Icon(Icons.Outlined.Cookie, null)
    }, title = { Text(stringResource(R.string.cookies)) }, text = {
        Column(Modifier.verticalScroll(rememberScrollState())) {
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                value = url, label = { Text("URL") },
                onValueChange = { cookiesViewModel.updateUrl(it) }, trailingIcon = {
                    PasteFromClipBoardButton {
                        cookiesViewModel.updateUrl(
                            matchUrlFromClipboard(it)
                        )
                    }
                }, maxLines = 1,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
            )

            TextButtonWithIcon(
                onClick = { navigateToCookieGeneratorPage() },
                icon = Icons.Outlined.GeneratingTokens,
                text = stringResource(id = R.string.generate_new_cookies)
            )

        }
    }, dismissButton = {
        DismissButton {
            onDismissRequest()
        }
    }, confirmButton = {
        ConfirmButton(enabled = url.isNotEmpty()) {
            cookiesViewModel.updateCookieProfile()
            onDismissRequest()
        }
    })

}

@Composable
fun DeleteCookieDialog(
    cookiesViewModel: CookiesViewModel = viewModel(),
    onDismissRequest: () -> Unit = {}
) {
    val state by cookiesViewModel.stateFlow.collectAsState()
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(stringResource(R.string.remove)) },
        text = {
            Text(
                stringResource(R.string.remove_cookie_profile_desc).format(state.editingCookieProfile.url),
                style = LocalTextStyle.current.copy(lineBreak = LineBreak.Paragraph)
            )
        },
        dismissButton = {
            DismissButton {
                onDismissRequest()
            }
        }, confirmButton = {
            ConfirmButton {
                cookiesViewModel.deleteCookieProfile()
                onDismissRequest()
            }
        }, icon = { Icon(Icons.Outlined.Delete, null) })
}

@Composable
fun ClearCookiesDialog(
    onDismissRequest: () -> Unit = {}, onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(stringResource(R.string.clear_all_cookies)) },
        text = {
            Text(
                stringResource(R.string.clear_all_cookies_desc),
                style = MaterialTheme.typography.bodyLarge
            )
        },
        dismissButton = {
            DismissButton {
                onDismissRequest()
            }
        }, confirmButton = {
            ConfirmButton {
                onConfirm()
                onDismissRequest()
            }
        }, icon = { Icon(Icons.Outlined.DeleteForever, null) })
}

@Composable
fun CookiesQuickSettingsDialog(
    onDismissRequest: () -> Unit = {},
    onConfirm: () -> Unit = {},
    cookieProfiles: List<CookieProfile> = emptyList(),
    onCookieProfileClicked: (CookieProfile) -> Unit = {},
    isCookiesEnabled: Boolean = false,
    onCookiesToggled: (Boolean) -> Unit = {},
) {
    SealDialog(onDismissRequest = onDismissRequest, confirmButton = {
        ConfirmButton(text = stringResource(id = androidx.appcompat.R.string.abc_action_mode_done)) {
            onDismissRequest()
            onConfirm()
        }
    }, icon = { Icon(imageVector = Icons.Outlined.Cookie, contentDescription = null) },
        title = {
            Text(
                text = stringResource(id = R.string.cookies),
                textAlign = TextAlign.Center
            )
        }, text = {
            Column {
                Text(
                    text = stringResource(id = R.string.refresh_cookies_desc),
                    modifier = Modifier.padding(horizontal = 24.dp),
//                    style = MaterialTheme.typography.labelLarge,
                )
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(Modifier.padding(horizontal = 24.dp))
                Spacer(modifier = Modifier.height(4.dp))
                LazyColumn() {
                    items(items = cookieProfiles) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onCookieProfileClicked(it) }
                                .padding(horizontal = 24.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .padding(end = 12.dp)
                                    .size(16.dp)
                                    .background(
                                        color = it.url
                                            .hashCode()
                                            .generateLabelColor(), shape = CircleShape
                                    )
                                    .clearAndSetSemantics { }
                            ) {}
                            Text(
                                text = it.url
//                                , style = MaterialTheme.typography.labelLarge
                                , modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                HorizontalDivider(Modifier.padding(horizontal = 24.dp))
                DialogSwitchItem(
                    text = stringResource(id = R.string.use_cookies),
                    value = isCookiesEnabled,
                    onValueChange = onCookiesToggled
                )
            }
        })
}

@Preview
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CookiesQuickSettingsDialogPreview() {
    SealTheme {
        var isCookiesEnabled by remember {
            mutableStateOf(false)
        }
        CookiesQuickSettingsDialog(
            cookieProfiles = mutableListOf<CookieProfile>().apply {
                repeat(4) {
                    add(
                        CookieProfile(
                            id = it,
                            url = "https://www.example$it.com",
                            content = ""
                        )
                    )
                }
            }, isCookiesEnabled = isCookiesEnabled, onCookiesToggled = { isCookiesEnabled = it }
        )
    }
}