package com.junkfood.seal.ui.page.settings.network

import android.content.res.Configuration
import android.webkit.CookieManager
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ContentPasteGo
import androidx.compose.material.icons.outlined.Cookie
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.GeneratingTokens
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.junkfood.seal.R
import com.junkfood.seal.database.CookieProfile
import com.junkfood.seal.ui.component.BackButton
import com.junkfood.seal.ui.component.ConfirmButton
import com.junkfood.seal.ui.component.DismissButton
import com.junkfood.seal.ui.component.HelpDialog
import com.junkfood.seal.ui.component.HorizontalDivider
import com.junkfood.seal.ui.component.LargeTopAppBar
import com.junkfood.seal.ui.component.PasteFromClipBoardButton
import com.junkfood.seal.ui.component.PreferenceItemVariant
import com.junkfood.seal.ui.component.PreferenceSwitchWithContainer
import com.junkfood.seal.ui.component.SealDialog
import com.junkfood.seal.ui.component.TextButtonWithIcon
import com.junkfood.seal.ui.theme.SealTheme
import com.junkfood.seal.ui.theme.generateLabelColor
import com.junkfood.seal.util.COOKIES
import com.junkfood.seal.util.DownloadUtil
import com.junkfood.seal.util.FileUtil
import com.junkfood.seal.util.FileUtil.getCookiesFile
import com.junkfood.seal.util.PreferenceUtil
import com.junkfood.seal.util.PreferenceUtil.updateBoolean
import com.junkfood.seal.util.matchUrlFromClipboard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CookieProfilePage(
    cookiesViewModel: CookiesViewModel = viewModel(),
    navigateToCookieGeneratorPage: () -> Unit = {},
    onBackPressed: () -> Unit = {}
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
    var isCookieEnabled by remember { mutableStateOf(PreferenceUtil.getValue(COOKIES)) }
    val cookieManager = CookieManager.getInstance()
    var showHelpDialog by remember { mutableStateOf(false) }

    LaunchedEffect(state.showEditDialog) {
        withContext(Dispatchers.IO) {
            DownloadUtil.getCookiesContentFromDatabase().getOrNull()?.let {
                FileUtil.writeContentToFile(it, context.getCookiesFile())
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
                    onBackPressed()
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
                    DropdownMenuItem(
                        leadingIcon = { Icon(Icons.Outlined.ContentPasteGo, null) },
                        text = {
                            Text(stringResource(id = R.string.export_to_clipboard))
                        },
                        onClick = {
                            expanded = false
                            scope.launch(Dispatchers.IO) {
                                DownloadUtil.getCookiesContentFromDatabase().getOrNull()?.let {
                                    clipboardManager.setText(AnnotatedString(it))
                                }
                            }
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
        LazyColumn(modifier = Modifier.padding(paddingValues)) {
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
            /*            item {
                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider()
                            PreferenceInfo(
                                modifier = Modifier.padding(horizontal = 4.dp),
                                icon = Icons.Outlined.HelpOutline,
                                text = stringResource(id = R.string.cookies_usage_msg)
                            )
                        }*/
        }

    }
    if (state.showEditDialog) {
        CookieGeneratorDialog(
            cookiesViewModel,
            navigateToCookieGeneratorPage = navigateToCookieGeneratorPage
        ) {
            cookiesViewModel.hideDialog()
        }
    }

    if (state.showDeleteDialog) {
        DeleteCookieDialog(cookiesViewModel) { cookiesViewModel.hideDialog() }
    }

    if (showHelpDialog) {
        HelpDialog(text = stringResource(id = R.string.cookies_usage_msg)) {
            showHelpDialog = false
        }
    }
    if (showClearCookieDialog) {
        ClearCookiesDialog(onDismissRequest = { showClearCookieDialog = false }) {
            scope.launch(Dispatchers.IO) {
                CookieManager.getInstance().removeAllCookies(null)
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
        ConfirmButton {
            onDismissRequest()
            onConfirm()
        }
    }, dismissButton = {
        DismissButton {
            onDismissRequest()
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
                Row(modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onCookiesToggled(!isCookiesEnabled) }
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(id = R.string.use_cookies),
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.weight(1f)
                    )
                    val thumbContent: (@Composable () -> Unit)? = if (isCookiesEnabled) {
                        {
                            Icon(
                                imageVector = Icons.Outlined.Check,
                                contentDescription = null,
                                modifier = Modifier.size(SwitchDefaults.IconSize)
                            )
                        }
                    } else {
                        null
                    }
                    val density = LocalDensity.current
                    CompositionLocalProvider(
                        LocalDensity provides Density(
                            density.density * 0.8f,
                            density.fontScale
                        )
                    ) {
                        Switch(
                            checked = isCookiesEnabled,
                            onCheckedChange = onCookiesToggled,
                            modifier = Modifier.clearAndSetSemantics { },
                            thumbContent = thumbContent
                        )
                    }

                }
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