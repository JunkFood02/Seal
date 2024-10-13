package com.junkfood.seal.ui.page

import android.os.Build
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NewReleases
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.junkfood.seal.R
import com.junkfood.seal.util.ToastUtil
import com.junkfood.seal.util.UpdateUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


@Composable
fun UpdateDialog(
    onDismissRequest: () -> Unit,
    release: UpdateUtil.Release,
) {
    var currentDownloadStatus by remember { mutableStateOf(UpdateUtil.DownloadStatus.NotYet as UpdateUtil.DownloadStatus) }
    val context = LocalContext.current

    val scope = rememberCoroutineScope()
    UpdateDialogImpl(
        onDismissRequest = onDismissRequest,
        title = release.name.toString(),
        onConfirmUpdate = {
            scope.launch(Dispatchers.IO) {
                runCatching {
                    UpdateUtil.downloadApk(release = release)
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
        releaseNote = release.body.toString(),
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

            Button(
                onClick = { if (downloadStatus !is UpdateUtil.DownloadStatus.Progress) onConfirmUpdate() }) {
                Text(
                    when (downloadStatus) {
                        is UpdateUtil.DownloadStatus.Progress -> "${downloadStatus.percent} %"
                        else -> stringResource(R.string.update)
                    },
                    modifier = Modifier.animateContentSize(),
                    )
            }
        }, dismissButton = {
            OutlinedButton(onClick = onDismissRequest) { Text(text = stringResource(id = R.string.dismiss)) }
        }, text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                Text(releaseNote)
            }
        })
}

@Preview
@Composable
private fun Preview() {
    var b by remember {
        mutableStateOf(false)
    }
    val flow: MutableStateFlow<UpdateUtil.DownloadStatus> = remember {
        MutableStateFlow(UpdateUtil.DownloadStatus.NotYet)
    }

    LaunchedEffect(b) {
        if (b) {
            repeat(100) { i ->
                flow.update { UpdateUtil.DownloadStatus.Progress(percent = i) }
                delay(50)
            }
        } else {
            flow.update { UpdateUtil.DownloadStatus.NotYet }
        }
    }

    val status by flow.collectAsStateWithLifecycle()

    UpdateDialogImpl(
        onDismissRequest = { b = false },
        title = "v1.12.0",
        onConfirmUpdate = { b = true },
        releaseNote = "ReleaseNoteHTML",
        downloadStatus = status
    )
}