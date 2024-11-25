package com.junkfood.seal.ui.page.downloadv2.configure

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AddLink
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material.icons.outlined.ContentPasteGo
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TriStateCheckbox
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.junkfood.seal.R
import com.junkfood.seal.ui.component.ClearButton
import com.junkfood.seal.ui.component.FilledButtonWithIcon
import com.junkfood.seal.ui.component.OutlinedButtonWithIcon
import com.junkfood.seal.ui.component.OutlinedDismissButton
import com.junkfood.seal.ui.component.SealDialog
import com.junkfood.seal.ui.page.downloadv2.configure.DownloadDialogViewModel.Action
import com.junkfood.seal.ui.theme.ErrorTonalPalettes
import com.junkfood.seal.util.findURLsFromString

@Composable
fun InputUrlPage(
    modifier: Modifier = Modifier,
    config: Config,
    onConfigUpdate: (Config) -> Unit,
    onActionPost: (Action) -> Unit,
) {
    val clipboardManager = LocalClipboardManager.current
    val urlList = remember { mutableStateListOf<String>() }
    val savedLinks = remember(config) { mutableStateListOf<String>() }

    LaunchedEffect(Unit) {
        clipboardManager.getText()?.let {
            urlList.clear()
            urlList.addAll(findURLsFromString(it.toString()).toSet())
        }
    }

    LaunchedEffect(config) { savedLinks.addAll(config.savedLinks) }

    InputUrlPageImpl(
        modifier = modifier,
        urlListFromClipboard = urlList,
        savedLinks = savedLinks,
        onSaveLink = { savedLinks.add(it) },
        onRemoveSavedLink = { savedLinks.remove(it) },
        onActionPost = onActionPost,
    )

    DisposableEffect(Unit) {
        onDispose { onConfigUpdate(config.copy(savedLinks = savedLinks.toSet())) }
    }
}

@Preview
@Composable
private fun InputUrlPreview() {
    val urlList = remember {
        mutableStateListOf<String>().apply { repeat(20) { add("https://www.example$it.com/") } }
    }
    InputUrlPageImpl(
        urlListFromClipboard = listOf("https://www.example.com"),
        savedLinks = urlList,
        onSaveLink = { urlList.add(it) },
        onRemoveSavedLink = { urlList.remove(it) },
    ) {}
}

@Composable
private fun InputUrlPageImpl(
    modifier: Modifier = Modifier,
    urlListFromClipboard: List<String>,
    savedLinks: List<String> = emptyList(),
    onSaveLink: (String) -> Unit = {},
    onRemoveSavedLink: (String) -> Unit = {},
    onActionPost: (Action) -> Unit,
) {

    var url by remember { mutableStateOf("") }
    var showPasteDialog by remember { mutableStateOf(false) }
    var showSavedUrlDialog by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Header(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
            title = stringResource(R.string.new_task),
            icon = Icons.Outlined.Add,
        )
        OutlinedTextField(
            value = url,
            onValueChange = { url = it },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp).padding(horizontal = 32.dp),
            label = { Text(stringResource(R.string.video_url)) },
            maxLines = 3,
            trailingIcon = {
                if (url.isNotEmpty()) {
                    ClearButton { url = "" }
                }
            },
        )

        LazyRow(
            modifier = Modifier.padding(top = 8.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 32.dp),
        ) {
            if (urlListFromClipboard.isNotEmpty()) {
                item(key = "paste url") {
                    SuggestionChip(
                        modifier = Modifier.animateItem(),
                        onClick = { url = urlListFromClipboard.first() },
                        label = { Text(stringResource(R.string.paste_msg)) },
                        icon = {
                            Icon(
                                imageVector = Icons.Outlined.ContentPaste,
                                contentDescription = null,
                                modifier = Modifier.size(SuggestionChipDefaults.IconSize),
                            )
                        },
                    )
                }
            }

            if (urlListFromClipboard.size > 1) {
                item(key = "paste multiple url") {
                    SuggestionChip(
                        modifier = Modifier.animateItem(),
                        onClick = { showPasteDialog = true },
                        label = {
                            Text(
                                stringResource(
                                    R.string.select_multiple_link,
                                    urlListFromClipboard.size,
                                )
                            )
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Outlined.ContentPasteGo,
                                contentDescription = null,
                                modifier = Modifier.size(SuggestionChipDefaults.IconSize),
                            )
                        },
                    )
                }
            }

            item(key = "saved urls") {
                val addToSavedLinks by remember {
                    derivedStateOf { url.isNotBlank() && !savedLinks.contains(url) }
                }
                if (addToSavedLinks || savedLinks.isNotEmpty()) {
                    SuggestionChip(
                        modifier = Modifier.animateItem(),
                        onClick = {
                            if (addToSavedLinks) onSaveLink(url)
                            else {
                                showSavedUrlDialog = true
                            }
                        },
                        label = {
                            Row(modifier = Modifier.animateContentSize()) {
                                if (addToSavedLinks) {
                                    Text(
                                        stringResource(
                                            R.string.add_to,
                                            stringResource(R.string.saved_urls),
                                        )
                                    )
                                } else {
                                    Text(stringResource(R.string.saved_urls))
                                }
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Outlined.AddLink,
                                contentDescription = null,
                                modifier = Modifier.size(SuggestionChipDefaults.IconSize),
                            )
                        },
                    )
                }
            }
        }

        Row(
            modifier =
                Modifier.align(Alignment.End).padding(top = 24.dp).padding(horizontal = 32.dp)
        ) {
            OutlinedButtonWithIcon(
                modifier = Modifier.padding(horizontal = 12.dp),
                onClick = { onActionPost(Action.HideSheet) },
                icon = Icons.Outlined.Cancel,
                text = stringResource(R.string.cancel),
            )
            FilledButtonWithIcon(
                icon = Icons.AutoMirrored.Outlined.ArrowForward,
                text = stringResource(R.string.proceed),
            ) {
                onActionPost(Action.ProceedWithURLs(listOf(url)))
            }
        }
    }
    if (showPasteDialog) {
        URLSelectionDialog(
            urlListFromClipboard = urlListFromClipboard,
            onDismissRequest = { showPasteDialog = false },
            onConfirm = { onActionPost(Action.ProceedWithURLs(it)) },
        )
    }

    if (showSavedUrlDialog) {
        SavedUrlDialogImpl(
            urls = savedLinks,
            onRemoveLink = onRemoveSavedLink,
            onActionPost = onActionPost,
            onDismissRequest = { showSavedUrlDialog = false },
        )
    }
}

@Composable
private fun URLSelectionDialog(
    modifier: Modifier = Modifier,
    urlListFromClipboard: List<String>,
    onDismissRequest: () -> Unit,
    onConfirm: (List<String>) -> Unit,
) {
    val indexList =
        remember(urlListFromClipboard) {
            mutableStateListOf<Int>().apply { addAll(urlListFromClipboard.indices) }
        }

    SealDialog(
        modifier = modifier,
        onDismissRequest = onDismissRequest,
        title = { Text(stringResource(R.string.select_multiple_link, urlListFromClipboard.size)) },
        icon = { Icon(Icons.Outlined.AddLink, null) },
        confirmButton = {
            FilledButtonWithIcon(
                icon = Icons.AutoMirrored.Outlined.ArrowForward,
                text = stringResource(R.string.proceed),
                enabled = indexList.isNotEmpty(),
            ) {
                onConfirm(indexList.map { urlListFromClipboard[it] })
                onDismissRequest()
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismissRequest) { Text(stringResource(R.string.cancel)) }
        },
        text = {
            Box(modifier = Modifier.fillMaxSize()) {
                HorizontalDivider(modifier = Modifier.align(Alignment.TopCenter))
                LazyColumn(modifier = Modifier.padding(bottom = 48.dp).heightIn(max = 600.dp)) {
                    itemsIndexed(urlListFromClipboard) { index, url ->
                        DialogCheckBoxItemVariant(text = url, checked = indexList.contains(index)) {
                            if (!it) {
                                indexList -= index
                            } else {
                                indexList += index
                            }
                        }
                    }
                }

                val checkBoxState =
                    remember(indexList.size) {
                        if (indexList.isEmpty()) {
                            ToggleableState.Off
                        } else if (indexList.size < urlListFromClipboard.size) {
                            ToggleableState.Indeterminate
                        } else {
                            ToggleableState.On
                        }
                    }
                Column(modifier = Modifier.align(Alignment.BottomCenter)) {
                    HorizontalDivider(modifier = Modifier)

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp).height(48.dp),
                    ) {
                        TriStateCheckbox(
                            state = checkBoxState,
                            onClick = {
                                when (checkBoxState) {
                                    ToggleableState.On -> indexList.clear()
                                    ToggleableState.Off ->
                                        indexList.addAll(urlListFromClipboard.indices)
                                    ToggleableState.Indeterminate -> {
                                        indexList.clear()
                                        indexList.addAll(urlListFromClipboard.indices)
                                    }
                                }
                            },
                        )
                        Text(stringResource(R.string.select_all))
                    }
                }
            }
        },
    )
}

@Composable
private fun DialogCheckBoxItemVariant(
    modifier: Modifier = Modifier,
    text: String,
    checked: Boolean,
    onValueChange: (Boolean) -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }

    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .toggleable(
                    value = checked,
                    enabled = true,
                    onValueChange = onValueChange,
                    interactionSource = interactionSource,
                    indication = LocalIndication.current,
                )
                .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Checkbox(
            modifier = Modifier.clearAndSetSemantics {},
            checked = checked,
            onCheckedChange = onValueChange,
            interactionSource = interactionSource,
        )
        Text(
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun DialogSingleChoiceItemVariant(
    modifier: Modifier = Modifier,
    text: String,
    selected: Boolean,
    onSelect: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .selectable(
                    selected = selected,
                    enabled = true,
                    onClick = onSelect,
                    interactionSource = interactionSource,
                    indication = LocalIndication.current,
                )
                .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            selected = selected,
            onClick = onSelect,
            modifier = Modifier.clearAndSetSemantics {},
            interactionSource = interactionSource,
        )
        Text(
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable fun SavedURLsDialog(modifier: Modifier = Modifier) {}

@Preview
@Composable
private fun SavedUrlPreview() {
    val urls = remember {
        mutableStateListOf<String>().apply { repeat(10) { add("https://www.example$it.com/") } }
    }
    SavedUrlDialogImpl(urls = urls, onActionPost = {}, onRemoveLink = { urls.remove(it) }) {}
}

@Composable
private fun SavedUrlDialogImpl(
    modifier: Modifier = Modifier,
    urls: List<String>,
    onRemoveLink: (String) -> Unit,
    onActionPost: (Action) -> Unit,
    onDismissRequest: () -> Unit,
) {
    if (urls.isEmpty()) {
        onDismissRequest()
    }
    var selectedUrl: String? by remember(urls.size) { mutableStateOf(null) }
    val hapticFeedback = LocalHapticFeedback.current

    SealDialog(
        modifier = modifier,
        icon = { Icon(Icons.Outlined.Link, contentDescription = null) },
        title = { Text(stringResource(R.string.saved_urls)) },
        onDismissRequest = onDismissRequest,
        dismissButton = { OutlinedDismissButton(onClick = onDismissRequest) },
        confirmButton = {
            FilledButtonWithIcon(
                icon = Icons.AutoMirrored.Outlined.ArrowForward,
                text = stringResource(R.string.proceed),
                enabled = selectedUrl != null,
            ) {
                onActionPost(Action.ProceedWithURLs(listOf(selectedUrl!!)))
            }
        },
        text = {
            Box(modifier = Modifier.animateContentSize()) {
                HorizontalDivider(Modifier.align(Alignment.TopCenter).zIndex(1f))
                HorizontalDivider(Modifier.align(Alignment.BottomCenter).zIndex(1f))

                LazyColumn(modifier = Modifier.heightIn(max = 600.dp)) {
                    items(items = urls, key = { it }) {
                        val dismissState = rememberSwipeToDismissBoxState()
                        LaunchedEffect(dismissState.currentValue) {
                            when (dismissState.currentValue) {
                                SwipeToDismissBoxValue.EndToStart -> {
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.Confirm)
                                    onRemoveLink(it)
                                }
                                else -> {}
                            }
                        }
                        val containerColor by
                            animateColorAsState(
                                when (dismissState.targetValue) {
                                    SwipeToDismissBoxValue.EndToStart ->
                                        ErrorTonalPalettes.accent1(80.0)
                                    else -> MaterialTheme.colorScheme.surfaceContainerHigh
                                }
                            )
                        val contentColor by
                            animateColorAsState(
                                when (dismissState.targetValue) {
                                    SwipeToDismissBoxValue.Settled ->
                                        MaterialTheme.colorScheme.onSurface
                                    else -> ErrorTonalPalettes.accent1(10.0)
                                }
                            )
                        SwipeToDismissBox(
                            modifier = Modifier.animateItem(),
                            state = dismissState,
                            enableDismissFromEndToStart = true,
                            enableDismissFromStartToEnd = false,
                            backgroundContent = {
                                Box(Modifier.fillMaxSize().background(containerColor)) {
                                    Icon(
                                        Icons.Outlined.Delete,
                                        null,
                                        modifier =
                                            Modifier.align(Alignment.CenterEnd)
                                                .padding(end = 16.dp)
                                                .size(28.dp),
                                        tint = contentColor,
                                    )
                                }
                            },
                        ) {
                            Surface(color = MaterialTheme.colorScheme.surfaceContainerHigh) {
                                DialogSingleChoiceItemVariant(
                                    text = it,
                                    selected = selectedUrl == it,
                                    onSelect = { selectedUrl = it },
                                )
                            }
                        }
                    }
                }
            }
        },
    )
}
