package com.junkfood.seal.ui.page

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import com.junkfood.seal.BaseApplication
import com.junkfood.seal.BaseApplication.Companion.downloadDir
import com.junkfood.seal.BaseApplication.Companion.updateDownloadDir
import com.junkfood.seal.R
import com.junkfood.seal.util.DownloadUtil.updateYtDlp
import com.junkfood.seal.util.PreferenceUtil
import com.junkfood.ui.PreferenceItem
import com.junkfood.ui.PreferenceSwitch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun DownloadPreferences(navController: NavController) {
    var downloadDirectoryText by remember { mutableStateOf(downloadDir) }

    val storagePermission =
        rememberPermissionState(permission = Manifest.permission.WRITE_EXTERNAL_STORAGE)
    val launcher =
        rememberLauncherForActivityResult(object : ActivityResultContracts.OpenDocumentTree() {
            override fun createIntent(context: Context, input: Uri?): Intent {
                return (super.createIntent(context, input)).apply {
                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION or
                            Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
                }
            }
        }) {
            it?.let {
                val path =
                    "%s/%s".format(
                        Environment.getExternalStorageDirectory().absolutePath,
                        it.path!!.split("primary:")[1]
                    )
                updateDownloadDir(path)
                downloadDirectoryText = path
            }
        }

    fun openDirectoryChooser() {
        when (storagePermission.status) {
            is PermissionStatus.Granted -> {
                launcher.launch(null)
            }
            is PermissionStatus.Denied -> {
                storagePermission.launchPermissionRequest()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        LargeTopAppBar(
            title = {
                Text(
                    text = stringResource(id = R.string.download),
                    fontSize = MaterialTheme.typography.displaySmall.fontSize
                )
            }, navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.Outlined.ArrowBack,
                        contentDescription = "Localized description"
                    )
                }

            }, modifier = Modifier.padding(9f.dp)
        )
        var audioSwitch by remember { mutableStateOf(PreferenceUtil.getValue("extract_audio")) }
        var openSwitch by remember { mutableStateOf(PreferenceUtil.getValue("open_when_finish")) }
        var thumbnailSwitch by remember { mutableStateOf(PreferenceUtil.getValue("create_thumbnail")) }

        PreferenceItem(
            title = stringResource(id = R.string.download_directory),
            description = downloadDirectoryText,
            icon = null, enable = true
        ) {
            openDirectoryChooser()
        }

        var ytdlpVersion by remember { mutableStateOf(BaseApplication.ytdlpVersion) }
        PreferenceItem(
            title = stringResource(id = R.string.ytdlp_version),
            description = ytdlpVersion,
            icon = null, enable = true
        ) {
            CoroutineScope(Job()).launch {
                ytdlpVersion = updateYtDlp()
            }
        }



        PreferenceSwitch(
            title = stringResource(id = R.string.extract_audio), description = stringResource(
                id = R.string.extract_audio_summary
            ), icon = null, isChecked = audioSwitch, onClick = {
                audioSwitch = !audioSwitch
                PreferenceUtil.updateValue("extract_audio", audioSwitch)
            }
        )

        PreferenceSwitch(
            title = stringResource(id = R.string.create_thumbnail),
            description = stringResource(
                id = R.string.create_thumbnail_summary
            ),
            icon = null,
            isChecked = thumbnailSwitch,
            onClick = {
                thumbnailSwitch = !thumbnailSwitch
                PreferenceUtil.updateValue("create_thumbnail", thumbnailSwitch)
            }
        )
        PreferenceSwitch(
            title = stringResource(id = R.string.open_when_finish),
            description = stringResource(
                id = R.string.open_when_finish_summary
            ),
            icon = null,
            isChecked = openSwitch,
            onClick = {
                openSwitch = !openSwitch
                PreferenceUtil.updateValue("open_when_finish", openSwitch)
            }
        )
    }
}


