package com.junkfood.seal.ui.page.settings.network

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Cookie
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.GeneratingTokens
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.junkfood.seal.R
import com.junkfood.seal.ui.component.BackButton
import com.junkfood.seal.ui.component.ConfirmButton
import com.junkfood.seal.ui.component.DismissButton
import com.junkfood.seal.ui.component.HelpDialog
import com.junkfood.seal.ui.component.LargeTopAppBar
import com.junkfood.seal.ui.component.PasteFromClipBoardButton
import com.junkfood.seal.ui.component.PreferenceItemVariant
import com.junkfood.seal.ui.component.PreferenceSwitchWithContainer
import com.junkfood.seal.ui.component.TextButtonWithIcon
import com.junkfood.seal.util.COOKIES
import com.junkfood.seal.util.PreferenceUtil
import com.junkfood.seal.util.matchUrlFromClipboard

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

    val state by cookiesViewModel.stateFlow.collectAsStateWithLifecycle()

    var isCookieEnabled by remember { mutableStateOf(PreferenceUtil.getValue(COOKIES)) }

    var showHelpDialog by remember { mutableStateOf(false) }

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
                IconButton(onClick = { showHelpDialog = true }) {
                    Icon(
                        imageVector = Icons.Outlined.HelpOutline,
                        contentDescription = stringResource(R.string.how_does_it_work)
                    )
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
                        if (cookies.isEmpty() && !isCookieEnabled)
                            showHelpDialog = true
                        else {
                            isCookieEnabled = !isCookieEnabled
                            PreferenceUtil.updateValue(COOKIES, isCookieEnabled)
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
}

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalComposeUiApi::class
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
    val content = profile.content

    AlertDialog(onDismissRequest = onDismissRequest, icon = {
        Icon(Icons.Outlined.Cookie, null)
    }, title = { Text(stringResource(R.string.cookies)) }, text = {
        Column(Modifier.verticalScroll(rememberScrollState())) {
            val focusManager = LocalFocusManager.current
            val softwareKeyboardController = LocalSoftwareKeyboardController.current
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
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

            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 12.dp),
                value = content,
                label = { Text(stringResource(R.string.cookies_file_name)) },
                onValueChange = { cookiesViewModel.updateContent(it) }, minLines = 8, maxLines = 8,
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
        ConfirmButton(enabled = url.isNotEmpty() && content.isNotEmpty()) {
            cookiesViewModel.updateCookieProfile()
            onDismissRequest()
        }
    })

}

@OptIn(ExperimentalTextApi::class)
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
