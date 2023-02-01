package com.junkfood.seal.ui.page

import android.os.Build
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NewReleases
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.junkfood.seal.R
import com.junkfood.seal.ui.component.DismissButton
import com.junkfood.seal.util.ToastUtil
import com.junkfood.seal.util.UpdateUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@Composable
fun UpdateDialog(
    onDismissRequest: () -> Unit,
    latestRelease: UpdateUtil.LatestRelease,
) {
    var currentDownloadStatus by remember { mutableStateOf(UpdateUtil.DownloadStatus.NotYet as UpdateUtil.DownloadStatus) }
    val context = LocalContext.current

    val scope = rememberCoroutineScope()
    UpdateDialogImpl(
        onDismissRequest = onDismissRequest,
        title = latestRelease.name.toString(),
        onConfirmUpdate = {
            scope.launch(Dispatchers.IO) {
                runCatching {
                    UpdateUtil.downloadApk(latestRelease = latestRelease)
                        .collect { downloadStatus ->
                            currentDownloadStatus = downloadStatus
                            if (downloadStatus is UpdateUtil.DownloadStatus.Finished) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    UpdateUtil.installLatestApk()
                                }
                            }
                        }
                }.onFailure {
                    it.printStackTrace()
                    currentDownloadStatus = UpdateUtil.DownloadStatus.NotYet
                    ToastUtil.makeToastSuspend(context.getString(R.string.app_update_failed))
                    return@launch
                }
            }
        },
        releaseNote = latestRelease.body.toString(),
        downloadStatus = currentDownloadStatus
    )
}

@Composable
fun UpdateDialogImpl(
    onDismissRequest: () -> Unit,
    title: String,
    onConfirmUpdate: () -> Unit,
    releaseNote: String,
    downloadStatus: UpdateUtil.DownloadStatus,
) {
    AlertDialog(
        onDismissRequest = {},
        title = { Text(title) },
        icon = { Icon(Icons.Outlined.NewReleases, null) }, confirmButton = {
            TextButton(onClick = { if (downloadStatus !is UpdateUtil.DownloadStatus.Progress) onConfirmUpdate() }) {
                when (downloadStatus) {
                    is UpdateUtil.DownloadStatus.Progress -> Text("${downloadStatus.percent} %")
                    else -> Text(stringResource(R.string.update))
                }
            }
        }, dismissButton = {
            DismissButton { onDismissRequest() }
        }, text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                Text(releaseNote)
            }
        })
}