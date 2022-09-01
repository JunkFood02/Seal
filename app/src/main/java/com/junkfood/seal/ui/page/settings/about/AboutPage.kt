package com.junkfood.seal.ui.page.settings.about

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.junkfood.seal.R
import com.junkfood.seal.ui.component.BackButton
import com.junkfood.seal.ui.component.DismissButton
import com.junkfood.seal.ui.component.LargeTopAppBar
import com.junkfood.seal.ui.component.PreferenceItem
import com.junkfood.seal.util.TextUtil
import com.junkfood.seal.util.UpdateUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private const val releaseURL = "https://github.com/JunkFood02/Seal/releases"
private const val repoUrl = "https://github.com/JunkFood02/Seal"
const val weblate = "https://hosted.weblate.org/engage/seal/"
private const val githubIssueUrl = "https://github.com/JunkFood02/Seal/issues/new/choose"

private const val TAG = "AboutPage"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutPage(onBackPressed: () -> Unit, jumpToCreditsPage: () -> Unit) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        rememberTopAppBarState(),
        canScroll = { true })
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val configuration = LocalConfiguration.current
    val screenDensity = configuration.densityDpi / 160f
    val screenHeight = (configuration.screenHeightDp.toFloat() * screenDensity).roundToInt()
    val screenWidth = (configuration.screenWidthDp.toFloat() * screenDensity).roundToInt()
    var latestRelease by remember { mutableStateOf(UpdateUtil.LatestRelease()) }
    var showUpdateDialog by remember { mutableStateOf(false) }
    var currentDownloadStatus by remember { mutableStateOf(UpdateUtil.DownloadStatus.NotYet as UpdateUtil.DownloadStatus) }
    val scope = rememberCoroutineScope()
    var updateJob: Job? = null
    val settings =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            UpdateUtil.installLatestApk()
        }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { result ->
        if (result) {
            UpdateUtil.installLatestApk()
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (!context.packageManager.canRequestPackageInstalls())
                    settings.launch(
                        Intent(
                            Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                            Uri.parse("package:${context.packageName}"),
                        )
                    )
                else
                    UpdateUtil.installLatestApk()
            }
        }
    }


    val info = if (Build.VERSION.SDK_INT >= 33) context.packageManager.getPackageInfo(
        context.packageName, PackageManager.PackageInfoFlags.of(0)
    )
    else context.packageManager.getPackageInfo(context.packageName, 0)

    val versionName = info.versionName

    val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        info.longVersionCode
    } else {
        info.versionCode.toLong()
    }
    val release = if (Build.VERSION.SDK_INT >= 30) {
        Build.VERSION.RELEASE_OR_CODENAME
    } else {
        Build.VERSION.RELEASE
    }

    val infoBuilder = StringBuilder()
    val deviceInformation =
        infoBuilder.append("App version: $versionName")
            .append(" ($versionCode)\n")
            .append("Device information: Android $release (API ${Build.VERSION.SDK_INT})\n")
            .append(Build.SUPPORTED_ABIS.contentToString())
            .append("\nScreen resolution: $screenHeight x $screenWidth").toString()
    val uriHandler = LocalUriHandler.current
    fun openUrl(url: String) {
        uriHandler.openUri(url)
    }
    Scaffold(modifier = Modifier
        .fillMaxSize()
        .nestedScroll(scrollBehavior.nestedScrollConnection), topBar = {
        LargeTopAppBar(title = {
            Text(
                modifier = Modifier.padding(start = 8.dp),
                text = stringResource(id = R.string.about),
            )
        }, navigationIcon = {
            BackButton(modifier = Modifier.padding(start = 8.dp)) {
                onBackPressed()
            }
        }, scrollBehavior = scrollBehavior
        )
    }, content = {
        LazyColumn(modifier = Modifier.padding(it)) {
            item {
                PreferenceItem(
                    title = stringResource(R.string.readme),
                    description = stringResource(R.string.readme_desc),
                    icon = Icons.Outlined.Description,
                ) { openUrl(repoUrl) }
            }
            if (!versionName.contains("F-Droid"))
                item {
                    PreferenceItem(
                        title = stringResource(R.string.check_for_updates),
                        description = stringResource(R.string.check_for_updates_desc),
                        icon = Icons.Outlined.Update
                    ) {
                        scope.launch(Dispatchers.IO) {
                            try {
                                val temp = UpdateUtil.checkForUpdate()
                                if (temp == null) {
                                    TextUtil.makeToastSuspend(context.getString(R.string.app_up_to_date))
                                } else {
                                    latestRelease = temp
                                    showUpdateDialog = true
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                                TextUtil.makeToastSuspend(context.getString(R.string.app_update_failed))
                                return@launch
                            }

                        }
                    }
                }
            item {
                PreferenceItem(
                    title = stringResource(R.string.release),
                    description = stringResource(R.string.release_desc),
                    icon = Icons.Outlined.NewReleases,
                ) { openUrl(releaseURL) }
            }
            item {
                PreferenceItem(
                    title = stringResource(R.string.github_issue),
                    description = stringResource(R.string.github_issue_desc),
                    icon = Icons.Outlined.ContactSupport,
                ) { openUrl(githubIssueUrl) }
            }
            item {
                PreferenceItem(
                    title = stringResource(id = R.string.credits),
                    description = stringResource(id = R.string.credits_desc),
                    icon = Icons.Outlined.AutoAwesome,
                ) { jumpToCreditsPage() }
            }
            item {
                PreferenceItem(
                    title = stringResource(R.string.translate),
                    description = stringResource(R.string.translate_desc),
                    icon = Icons.Outlined.Translate
                ) { openUrl(weblate) }
            }
            item {
                PreferenceItem(
                    title = stringResource(R.string.version),
                    description = versionName,
                    icon = Icons.Outlined.Info,
                ) {
                    clipboardManager.setText(AnnotatedString(deviceInformation))
                    TextUtil.makeToast(R.string.info_copied)
                }
            }
        }
    })
    if (showUpdateDialog) {
        UpdateDialog(
            onDismissRequest = {
                showUpdateDialog = false
                updateJob?.cancel()
            },
            title = latestRelease.name.toString(),
            onConfirmUpdate = {
                updateJob = scope.launch(Dispatchers.IO) {
                    kotlin.runCatching {
                        UpdateUtil.downloadApk(latestRelease = latestRelease)
                            .collect { downloadStatus ->
                                currentDownloadStatus = downloadStatus
                                if (downloadStatus is UpdateUtil.DownloadStatus.Finished) {
                                    launcher.launch(Manifest.permission.REQUEST_INSTALL_PACKAGES)
                                }
                            }
                    }.onFailure {
                        it.printStackTrace()
                        currentDownloadStatus = UpdateUtil.DownloadStatus.NotYet
                        TextUtil.makeToastSuspend(context.getString(R.string.app_update_failed))
                    }
                }
            },
            releaseNote = latestRelease.body.toString(),
            downloadStatus = currentDownloadStatus
        )
    }
}


@Composable
fun UpdateDialog(
    onDismissRequest: () -> Unit,
    title: String,
    onConfirmUpdate: () -> Unit,
    releaseNote: String,
    downloadStatus: UpdateUtil.DownloadStatus
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
            Text(releaseNote)
        })
}