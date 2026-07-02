package com.junkfood.seal.desktop.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.LaunchedEffect
import com.junkfood.seal.desktop.data.DesktopSettings
import com.junkfood.seal.desktop.download.DownloadPreferences
import com.junkfood.seal.desktop.download.DownloadState
import com.junkfood.seal.desktop.download.DownloadTask
import com.junkfood.seal.desktop.download.VideoInfo
import java.awt.Desktop
import java.io.File

private data class FormatSelectionState(
    val url: String,
    val preferences: DownloadPreferences,
    val info: VideoInfo? = null,
    val error: String? = null,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    settings: DesktopSettings,
    tasks: List<DownloadTask>,
    onStartDownload: (String, DownloadPreferences) -> Unit,
    onCancelDownload: (Long) -> Unit,
    onOpenVideoList: () -> Unit,
    onOpenSettings: () -> Unit,
    fetchVideoInfo: suspend (String) -> VideoInfo,
    sharedUrl: String? = null,
    onSharedUrlConsumed: () -> Unit = {},
) {
    var showInputDialog by remember { mutableStateOf(false) }
    var showOverflowMenu by remember { mutableStateOf(false) }
    var formatSelection by remember { mutableStateOf<FormatSelectionState?>(null) }

    // A link shared from another app opens the dialog directly, prefilled with that link.
    if (showInputDialog || sharedUrl != null) {
        DownloadDialog(
            initialUrl = sharedUrl,
            defaultPreferences = settings.downloadPreferences,
            onDismiss = {
                showInputDialog = false
                onSharedUrlConsumed()
            },
            onConfirm = { url, prefs ->
                showInputDialog = false
                onSharedUrlConsumed()
                onStartDownload(url, prefs)
            },
            onSelectFormats = { url, prefs ->
                showInputDialog = false
                onSharedUrlConsumed()
                formatSelection = FormatSelectionState(url = url, preferences = prefs)
            },
        )
    }

    formatSelection?.let { selection ->
        // Fetch formats once per URL while the selector is open.
        LaunchedEffect(selection.url) {
            runCatching { fetchVideoInfo(selection.url) }
                .onSuccess { info -> formatSelection = formatSelection?.copy(info = info) }
                .onFailure { e ->
                    formatSelection =
                        formatSelection?.copy(error = e.message ?: "Failed to fetch formats")
                }
        }
        FormatSelectorDialog(
            videoInfo = selection.info,
            error = selection.error,
            onDismiss = { formatSelection = null },
            onConfirm = { formatId ->
                formatSelection = null
                onStartDownload(selection.url, selection.preferences.copy(formatId = formatId))
            },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Seal") },
                navigationIcon = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Outlined.Menu, contentDescription = "Settings")
                    }
                },
                actions = {
                    IconButton(onClick = onOpenVideoList) {
                        Icon(Icons.AutoMirrored.Outlined.List, contentDescription = "Downloads")
                    }
                    Box {
                        IconButton(onClick = { showOverflowMenu = true }) {
                            Icon(Icons.Outlined.MoreVert, contentDescription = "More options")
                        }
                        DropdownMenu(
                            expanded = showOverflowMenu,
                            onDismissRequest = { showOverflowMenu = false },
                        ) {
                            DropdownMenuItem(
                                text = { Text("Open downloads folder") },
                                onClick = {
                                    showOverflowMenu = false
                                    val dir = File(settings.downloadDirectory).apply { mkdirs() }
                                    runCatching { Desktop.getDesktop().open(dir) }
                                },
                            )
                            DropdownMenuItem(
                                text = { Text("Settings") },
                                onClick = {
                                    showOverflowMenu = false
                                    onOpenSettings()
                                },
                            )
                        }
                    }
                },
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showInputDialog = true },
                text = { Text("Paste link") },
                icon = { Icon(Icons.Outlined.FileDownload, contentDescription = null) },
            )
        },
    ) { padding: PaddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (tasks.isEmpty()) {
                EmptyState()
            } else {
                // Adaptive grid: one column on a narrow window, more as the window widens, so
                // desktop-sized windows aren't a single stretched phone column.
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 380.dp),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    items(tasks, key = { it.id }) { task ->
                        TaskCard(
                            task = task,
                            onCancel = { onCancelDownload(task.id) },
                            modifier = Modifier.animateItem(),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            Icons.Outlined.FileDownload,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.outline,
        )
        Text(
            text = "No downloads yet",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 16.dp),
        )
        Text(
            text = "Paste a video link to get started",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun TaskCard(task: DownloadTask, onCancel: () -> Unit, modifier: Modifier = Modifier) {
    val state = task.state
    Card(
        modifier = modifier.fillMaxWidth().animateContentSize(),
        onClick = {
            if (state is DownloadState.Completed) {
                runCatching { Desktop.getDesktop().open(File(state.filePath)) }
            }
        },
        enabled = state is DownloadState.Completed,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                val title =
                    when (state) {
                        is DownloadState.Running -> state.title
                        is DownloadState.Completed -> state.title
                        is DownloadState.Canceled -> state.title
                        else -> null
                    } ?: task.url
                Text(text = title, style = MaterialTheme.typography.titleSmall, maxLines = 2)

                when (state) {
                    is DownloadState.FetchingInfo ->
                        Text(
                            text = "Fetching info…",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp),
                        )

                    is DownloadState.Running -> {
                        val animatedProgress by animateFloatAsState(state.progress)
                        LinearProgressIndicator(
                            progress = { animatedProgress },
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        )
                        Text(
                            text = listOf(state.progressText, state.speedText)
                                .filter { it.isNotBlank() }
                                .joinToString(" · "),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }

                    is DownloadState.Completed ->
                        Text(
                            text = "Saved to ${state.filePath} — click to open",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp),
                        )

                    is DownloadState.Canceled ->
                        Text(
                            text = "Canceled",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp),
                        )

                    is DownloadState.Error ->
                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                }
            }

            if (state is DownloadState.FetchingInfo || state is DownloadState.Running) {
                IconButton(onClick = onCancel) {
                    Icon(Icons.Outlined.Cancel, contentDescription = "Cancel download")
                }
            }
        }
    }
}
