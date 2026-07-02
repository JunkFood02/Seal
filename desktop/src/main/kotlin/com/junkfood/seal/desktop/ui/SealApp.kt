package com.junkfood.seal.desktop.ui

import androidx.compose.animation.Crossfade
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.junkfood.seal.desktop.data.DesktopSettings
import com.junkfood.seal.desktop.data.HistoryEntry
import com.junkfood.seal.desktop.data.HistoryStore
import com.junkfood.seal.desktop.data.SettingsStore
import com.junkfood.seal.desktop.download.DownloadPreferences
import com.junkfood.seal.desktop.download.DownloadState
import com.junkfood.seal.desktop.download.DownloadTask
import com.junkfood.seal.desktop.download.YtDlpDownloader
import com.junkfood.seal.desktop.platform.PathIntegration
import com.junkfood.seal.desktop.platform.WindowsIntegration
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private sealed interface Screen {
    data object Home : Screen

    data object VideoList : Screen

    data object Settings : Screen
}

/**
 * Root composable wiring the home, video list, and settings screens together.
 *
 * The download task list and [YtDlpDownloader] live here (not in [HomeScreen]) and the screen
 * switch below uses [Crossfade] instead of a raw `when`, so navigating to Settings/Downloads and
 * back doesn't wipe an in-progress download or cause an abrupt full-tree rebuild.
 *
 * [sharedUrl] is a link handed to Seal by another app (share/protocol/CLI, see Main); it opens
 * the download dialog prefilled, and [onSharedUrlConsumed] clears it once acted upon.
 */
@Composable
fun SealApp(sharedUrl: String? = null, onSharedUrlConsumed: () -> Unit = {}) {
    var screen by remember { mutableStateOf<Screen>(Screen.Home) }
    var settings by remember { mutableStateOf(SettingsStore.load()) }
    var history by remember { mutableStateOf(HistoryStore.load()) }

    val downloader = remember { YtDlpDownloader() }
    val tasks = remember { mutableStateListOf<DownloadTask>() }
    val downloadJobs = remember { mutableMapOf<Long, Job>() }
    val scope = rememberCoroutineScope()
    var nextId by remember { mutableStateOf(0L) }

    // First launch of a packaged install (the jpackage installer can't run custom actions, so
    // both run here instead): put the bundled yt-dlp/ffmpeg dir on the user PATH so the tools
    // are also usable from any terminal, and register the Windows shell integration (seal://
    // protocol, URL associations for shared links, and Seal's own uninstaller entry).
    LaunchedEffect(Unit) {
        if (!settings.addedToPath) {
            val dir = downloader.binaryDirectory
            if (dir != null) {
                val added = withContext(Dispatchers.IO) { PathIntegration.ensureOnUserPath(dir) }
                if (added) {
                    settings = settings.copy(addedToPath = true)
                    SettingsStore.save(settings)
                }
            }
        }
        if (!settings.shellIntegrated) {
            val registered = withContext(Dispatchers.IO) { WindowsIntegration.register() }
            if (registered) {
                settings = settings.copy(shellIntegrated = true)
                SettingsStore.save(settings)
            }
        }
    }

    // A link shared from another app always lands on Home, where the prefilled dialog opens.
    LaunchedEffect(sharedUrl) { if (sharedUrl != null) screen = Screen.Home }

    fun updateTask(taskId: Long, transform: (DownloadTask) -> DownloadTask) {
        val index = tasks.indexOfFirst { it.id == taskId }
        if (index >= 0) tasks[index] = transform(tasks[index])
    }

    fun startDownload(url: String, preferences: DownloadPreferences) {
        val taskId = nextId++
        tasks.add(0, DownloadTask(id = taskId, url = url))
        downloadJobs[taskId] =
            scope.launch {
                val outputDir = File(settings.downloadDirectory)
                // conflate() drops intermediate progress updates while the UI is busy, so bursts
                // of yt-dlp progress lines recompose at most at UI rate instead of queueing up.
                downloader.download(url, outputDir, preferences).conflate().collect { state ->
                    updateTask(taskId) { it.copy(state = state) }
                    if (state is DownloadState.Completed) {
                        history =
                            HistoryStore.append(
                                HistoryEntry(
                                    id = taskId,
                                    title = state.title ?: url,
                                    url = url,
                                    filePath = state.filePath,
                                )
                            )
                    }
                }
                downloadJobs.remove(taskId)
            }
    }

    fun cancelDownload(taskId: Long) {
        downloadJobs.remove(taskId)?.cancel()
        updateTask(taskId) { task ->
            val title = (task.state as? DownloadState.Running)?.title
            task.copy(state = DownloadState.Canceled(title = title))
        }
    }

    Crossfade(targetState = screen) { current ->
        when (current) {
            Screen.Home ->
                HomeScreen(
                    settings = settings,
                    tasks = tasks,
                    sharedUrl = sharedUrl,
                    onSharedUrlConsumed = onSharedUrlConsumed,
                    onStartDownload = ::startDownload,
                    onCancelDownload = ::cancelDownload,
                    onOpenVideoList = { screen = Screen.VideoList },
                    onOpenSettings = { screen = Screen.Settings },
                    fetchVideoInfo = downloader::fetchVideoInfo,
                )

            Screen.VideoList ->
                VideoListScreen(
                    entries = history,
                    onBack = { screen = Screen.Home },
                    onDelete = { id -> history = HistoryStore.remove(id) },
                )

            Screen.Settings ->
                SettingsScreen(
                    settings = settings,
                    ytDlpDirectory = downloader.binaryDirectory,
                    onBack = { screen = Screen.Home },
                    onSettingsChange = { updated: DesktopSettings ->
                        settings = updated
                        SettingsStore.save(updated)
                    },
                )
        }
    }
}
