@file:OptIn(ExperimentalPermissionsApi::class)

package com.junkfood.seal.ui.page.settings.download

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.SnippetFolder
import androidx.compose.material.icons.outlined.VideoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import com.junkfood.seal.BaseApplication
import com.junkfood.seal.R
import com.junkfood.seal.ui.component.LargeTopAppBar
import com.junkfood.seal.ui.component.PreferenceItem
import com.junkfood.seal.ui.component.PreferenceSwitch
import com.junkfood.seal.ui.component.Subtitle
import com.junkfood.seal.util.FileUtil
import com.junkfood.seal.util.PreferenceUtil
import com.junkfood.seal.util.PreferenceUtil.SUBDIRECTORY

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadDirectoryPreferences(onBackPressed: () -> Unit) {
    val decayAnimationSpec = rememberSplineBasedDecay<Float>()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        decayAnimationSpec,
        rememberTopAppBarScrollState()
    )
    var videoDirectoryText by remember { mutableStateOf(BaseApplication.videoDownloadDir) }
    var audioDirectoryText by remember { mutableStateOf(BaseApplication.audioDownloadDir) }
    var isSubdirectoryEnabled
            by remember { mutableStateOf(PreferenceUtil.getValue(SUBDIRECTORY, false)) }

    var isEditingAudioDirectory = false
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
                val path = FileUtil.getRealPath(it)
                BaseApplication.updateDownloadDir(path, isAudio = isEditingAudioDirectory)
                if (isEditingAudioDirectory)
                    audioDirectoryText = path
                else videoDirectoryText = path
            }
        }

    fun openDirectoryChooser() {
        if (Build.VERSION.SDK_INT >= 29 || storagePermission.status == PermissionStatus.Granted)
            launcher.launch(null)
        else storagePermission.launchPermissionRequest()
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        modifier = Modifier.padding(start = 8.dp),
                        text = stringResource(id = R.string.download_directory),
                    )
                }, navigationIcon = {
                    IconButton(
                        modifier = Modifier.padding(start = 8.dp),
                        onClick = onBackPressed
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }, scrollBehavior = scrollBehavior
            )
        }, content = {
            LazyColumn(modifier = Modifier.padding(it)) {
                item {
                    Subtitle(text = stringResource(R.string.general_settings))
                }
                item {
                    PreferenceItem(
                        title = stringResource(id = R.string.video_directory),
                        description = videoDirectoryText,
                        icon = Icons.Outlined.VideoLibrary
                    ) {
                        isEditingAudioDirectory = false
                        openDirectoryChooser()
                    }
                }
                item {
                    PreferenceItem(
                        title = stringResource(id = R.string.audio_directory),
                        description = audioDirectoryText,
                        icon = Icons.Outlined.LibraryMusic
                    ) {
                        isEditingAudioDirectory = true
                        openDirectoryChooser()
                    }
                }
                item {
                    PreferenceSwitch(
                        title = stringResource(id = R.string.subdirectory),
                        description = stringResource(id = R.string.subdirectory_desc),
                        icon = Icons.Outlined.SnippetFolder, isChecked = isSubdirectoryEnabled
                    ) {
                        isSubdirectoryEnabled = !isSubdirectoryEnabled
                        PreferenceUtil.updateValue(SUBDIRECTORY, isSubdirectoryEnabled)
                    }
                }
            }
        })
}