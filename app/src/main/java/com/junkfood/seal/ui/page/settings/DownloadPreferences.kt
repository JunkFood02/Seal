package com.junkfood.seal.ui.page.settings

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.ExperimentalUnitApi
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import com.junkfood.seal.BaseApplication
import com.junkfood.seal.BaseApplication.Companion.downloadDir
import com.junkfood.seal.BaseApplication.Companion.updateDownloadDir
import com.junkfood.seal.R
import com.junkfood.seal.ui.component.PreferenceItem
import com.junkfood.seal.ui.component.PreferenceSwitch
import com.junkfood.seal.util.DownloadUtil
import com.junkfood.seal.util.PreferenceUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@OptIn(
    ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class,
    ExperimentalUnitApi::class
)
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

    val decayAnimationSpec = rememberSplineBasedDecay<Float>()
    val scrollBehavior = remember(decayAnimationSpec) {
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(decayAnimationSpec)
    }
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            com.junkfood.seal.ui.component.LargeTopAppBar(
                title = {
                    Text(
                        modifier = Modifier.padding(start = 8.dp),
                        text = stringResource(id = R.string.download),
                        //fontSize = MaterialTheme.typography.displaySmall.fontSize
                    )
                }, navigationIcon = {
                    IconButton(
                        modifier = Modifier.padding(start = 8.dp),
                        onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Outlined.ArrowBack,
                            contentDescription = "Localized description"
                        )
                    }
                }, scrollBehavior = scrollBehavior
            )
        }, content = {
            LazyColumn(
                modifier = Modifier
                    .padding(it)
            ) {
                item {
                    PreferenceItem(
                        title = stringResource(id = R.string.download_directory),
                        description = downloadDirectoryText,
                        icon = null, enable = true
                    ) {
                        openDirectoryChooser()
                    }
                }
                item {
                    var ytdlpVersion by remember {
                        mutableStateOf(BaseApplication.ytdlpVersion)
                    }
                    PreferenceItem(
                        title = stringResource(id = R.string.ytdlp_version),
                        description = ytdlpVersion,
                        icon = null, enable = true
                    ) {
                        CoroutineScope(Job()).launch {
                            ytdlpVersion = DownloadUtil.updateYtDlp()
                        }
                    }
                }
                item {
                    var audioSwitch by remember { mutableStateOf(PreferenceUtil.getValue("extract_audio")) }
                    PreferenceSwitch(
                        title = stringResource(id = R.string.extract_audio),
                        description = stringResource(
                            id = R.string.extract_audio_summary
                        ),
                        icon = null,
                        isChecked = audioSwitch,
                        onClick = {
                            audioSwitch = !audioSwitch
                            PreferenceUtil.updateValue("extract_audio", audioSwitch)
                        }
                    )
                }
                item {
                    var thumbnailSwitch by remember { mutableStateOf(PreferenceUtil.getValue("create_thumbnail")) }
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
                }

                item {
                    var openSwitch by remember { mutableStateOf(PreferenceUtil.getValue("open_when_finish")) }
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
        }
    )
}


