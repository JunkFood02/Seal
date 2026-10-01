package com.junkfood.seal.ui.page

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.junkfood.seal.download.DownloaderV2
import com.junkfood.seal.download.Task
import com.junkfood.seal.util.PreferenceUtil
import com.junkfood.seal.util.PreferenceUtil.getBoolean
import com.junkfood.seal.util.PreferenceUtil.getLong
import com.junkfood.seal.util.PreferenceUtil.getString
import com.junkfood.seal.util.UpdateUtil
import com.junkfood.seal.util.YT_DLP_AUTO_UPDATE
import com.junkfood.seal.util.YT_DLP_UPDATE_INTERVAL
import com.junkfood.seal.util.YT_DLP_UPDATE_TIME
import com.junkfood.seal.util.YT_DLP_VERSION
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.compose.koinInject

@Composable
fun YtdlpUpdater() {
    val downloader: DownloaderV2 = koinInject()
    val taskStateMap = downloader.getTaskStateMap()

    LaunchedEffect(Unit) {
        val hasActiveTasks =
            taskStateMap.any { (_, state) ->
                state.downloadState is Task.DownloadState.Running ||
                    state.downloadState is Task.DownloadState.FetchingInfo
            }
        if (hasActiveTasks) return@LaunchedEffect

        if (!YT_DLP_AUTO_UPDATE.getBoolean() && YT_DLP_VERSION.getString().isNotEmpty())
            return@LaunchedEffect

        if (!PreferenceUtil.isNetworkAvailableForDownload()) {
            return@LaunchedEffect
        }

        val lastUpdateTime = YT_DLP_UPDATE_TIME.getLong()
        val currentTime = System.currentTimeMillis()

        if (currentTime < lastUpdateTime + YT_DLP_UPDATE_INTERVAL.getLong()) {
            return@LaunchedEffect
        }

        runCatching { withContext(Dispatchers.IO) { UpdateUtil.updateYtDlp() } }
            .onFailure { it.printStackTrace() }
    }
}
