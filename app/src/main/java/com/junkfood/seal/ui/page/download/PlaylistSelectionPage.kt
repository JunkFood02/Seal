package com.junkfood.seal.ui.page.download

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.PlaylistAdd
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.junkfood.seal.Downloader
import com.junkfood.seal.R
import com.junkfood.seal.ui.component.PlaylistItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistSelectionPage(onBackPressed: () -> Unit = {}) {
    val onDismissRequest = {
        onBackPressed()
    }
    val playlistInfo by Downloader.playlistResult.collectAsStateWithLifecycle()
    val selectedItems = rememberSaveable(saver = listSaver<MutableList<Int>, Int>(
        save = {
            if (it.isNotEmpty()) {
                it.toList()
            } else {
                emptyList()
            }
        },
        restore = {
            it.toMutableStateList()
        }
    )) { mutableStateListOf() }
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    var showDialog by remember { mutableStateOf(false) }
    val playlistCount = playlistInfo.entries?.size ?: 0

//    BackHandler { onDismissRequest() }

    Scaffold(modifier = Modifier
        .fillMaxSize()
        .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(title = {
                Text(
                    text = if (selectedItems.isEmpty()) stringResource(id = R.string.download_playlist) else stringResource(
                        id = R.string.selected_item_count
                    ).format(selectedItems.size),
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp)
                )
            }, navigationIcon = {
                IconButton(onClick = { onDismissRequest() }) {
                    Icon(Icons.Outlined.Close, stringResource(R.string.close))
                }
            }, actions = {
                TextButton(
                    modifier = Modifier.padding(end = 8.dp), onClick = {
                        Downloader.downloadVideoInPlaylistByIndexList(
                            url = playlistInfo.webpageUrl.toString(),
                            indexList = selectedItems,
                            playlistItemList = playlistInfo.entries?.filterIndexed { index, _ ->
                                selectedItems.contains(index)
                            } ?: emptyList()
                        )
                        onDismissRequest()
                    }, enabled = selectedItems.isNotEmpty()
                ) {
                    Text(text = stringResource(R.string.start_download))
                }
            }, scrollBehavior = scrollBehavior
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp)
                    .navigationBarsPadding(),
                verticalArrangement = Arrangement.Center
            ) {
                Divider(modifier = Modifier.fillMaxWidth())
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Row(
                        modifier = Modifier.selectable(selected = selectedItems.size == playlistCount && selectedItems.size != 0,
                            indication = null,
                            interactionSource = remember {
                                MutableInteractionSource()
                            },
                            onClick = {
                                if (selectedItems.size == playlistCount) selectedItems.clear() else {
                                    selectedItems.clear()
                                    selectedItems.addAll(1..playlistCount)
                                }
                            }), verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            modifier = Modifier.padding(16.dp),
                            checked = selectedItems.size == playlistCount && selectedItems.size != 0,
                            onCheckedChange = null
                        )
                        Text(
                            text = stringResource(R.string.select_all),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(modifier = Modifier.padding(end = 4.dp),
                        onClick = { showDialog = true }) {
                        Icon(
                            imageVector = Icons.Outlined.PlaylistAdd,
                            contentDescription = stringResource(
                                R.string.download_range_selection
                            )
                        )
                    }
                }
            }

        }) { paddings ->
        Column(
            modifier = Modifier.padding(paddings)
        ) {
            LazyColumn {
                item {
                    Text(
                        modifier = Modifier.padding(16.dp),
                        text = stringResource(R.string.download_selection_desc).format(playlistInfo.title),
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                itemsIndexed(items = playlistInfo.entries ?: emptyList()) { _index, entry ->
                    val index = _index + 1
                    TooltipBox(state = rememberTooltipState(),
                        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                        tooltip = {
                            PlainTooltip {
                                Text(text = entry.title ?: index.toString())
                            }
                        }) {
                        PlaylistItem(modifier = Modifier
                            .padding(horizontal = 12.dp),
                            imageModel = entry.thumbnails?.lastOrNull()?.url ?: "",
                            title = entry.title ?: index.toString(),
                            author = entry.channel ?: entry.uploader ?: playlistInfo.channel
                            ?: playlistInfo.uploader,
                            selected = selectedItems.contains(index),
                            onClick = {
                                if (selectedItems.contains(index)) selectedItems.remove(index)
                                else selectedItems.add(index)
                            })
                    }

                }
            }
        }
    }
    if (showDialog) {
        PlaylistSelectionDialog(playlistInfo = playlistInfo,
            onDismissRequest = { showDialog = false },
            onConfirm = {
                selectedItems.clear()
                selectedItems.addAll(it)
            })
    }
}



