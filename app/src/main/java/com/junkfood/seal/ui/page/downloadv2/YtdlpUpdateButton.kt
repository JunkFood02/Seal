package com.junkfood.seal.ui.page.downloadv2

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Update
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.junkfood.seal.App
import com.junkfood.seal.Downloader
import com.junkfood.seal.R
import com.junkfood.seal.download.DownloaderV2
import com.junkfood.seal.download.Task.DownloadState.FetchingInfo
import com.junkfood.seal.download.Task.DownloadState.Running
import com.junkfood.seal.util.PreferenceUtil.getString
import com.junkfood.seal.util.UpdateUtil
import com.junkfood.seal.util.YT_DLP_VERSION
import com.junkfood.seal.util.makeToast
import com.yausername.youtubedl_android.YoutubeDL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.compose.koinInject

/**
 * Process-wide holder for a manual yt-dlp update.
 *
 * It runs in the application scope so that the update is not interrupted (and the button state is
 * not lost) when the user leaves the home page while it is running.
 */
private object YtdlpManualUpdate {
    var isUpdating by mutableStateOf(false)
        private set

    fun start() {
        if (isUpdating) return
        // Same guard the automatic updater uses: only update while the downloader is idle.
        if (Downloader.downloaderState.value !is Downloader.State.Idle) return

        isUpdating = true
        Downloader.updateState(Downloader.State.Updating)

        App.applicationScope.launch(Dispatchers.IO) {
            runCatching { UpdateUtil.updateYtDlp() }
                .onSuccess {
                    val version =
                        YT_DLP_VERSION.getString().ifEmpty {
                            runCatching { YoutubeDL.getInstance().version(App.context) }
                                .getOrNull()
                                .orEmpty()
                        }
                    val suffix = if (version.isBlank()) "" else " ($version)"
                    withContext(Dispatchers.Main) {
                        App.context.makeToast(
                            App.context.getString(R.string.yt_dlp_up_to_date) + suffix
                        )
                    }
                }
                .onFailure { th ->
                    th.printStackTrace()
                    withContext(Dispatchers.Main) {
                        App.context.makeToast(R.string.yt_dlp_update_fail)
                    }
                }
            Downloader.updateState(Downloader.State.Idle)
            isUpdating = false
        }
    }
}

/**
 * A single icon button that updates the yt-dlp library on demand.
 *
 * It is disabled while an update is running or while a download is active, because replacing the
 * yt-dlp files during a download can break that download.
 */
@Composable
fun YtdlpUpdateButton(modifier: Modifier = Modifier, downloader: DownloaderV2 = koinInject()) {
    val isUpdating = YtdlpManualUpdate.isUpdating
    val downloaderState by Downloader.downloaderState.collectAsStateWithLifecycle()

    val hasActiveDownloads =
        downloader.getTaskStateMap().values.any {
            it.downloadState is Running || it.downloadState is FetchingInfo
        }

    IconButton(
        modifier = modifier,
        enabled = !isUpdating && !hasActiveDownloads && downloaderState is Downloader.State.Idle,
        onClick = { YtdlpManualUpdate.start() },
    ) {
        if (isUpdating || downloaderState is Downloader.State.Updating) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
        } else {
            Icon(
                imageVector = Icons.Outlined.Update,
                contentDescription = stringResource(R.string.ytdlp_update_action),
            )
        }
    }
}
