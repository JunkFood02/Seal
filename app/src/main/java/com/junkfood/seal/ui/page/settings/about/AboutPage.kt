package com.junkfood.seal.ui.page.settings.about

import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.junkfood.seal.R
import com.junkfood.seal.ui.component.LargeTopAppBar
import com.junkfood.seal.ui.component.PreferenceItem

const val releaseURL = "https://github.com/JunkFood02/Seal/releases/latest"
const val repoUrl = "https://github.com/JunkFood02/Seal"
const val weblate = "https://hosted.weblate.org/engage/seal/"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutPage(onBackPressed: () -> Unit, jumpToCreditsPage: () -> Unit) {
    val decayAnimationSpec = rememberSplineBasedDecay<Float>()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        decayAnimationSpec,
        rememberTopAppBarScrollState()
    )
    val context = LocalContext.current
    val info = if (Build.VERSION.SDK_INT >= 33) context.packageManager.getPackageInfo(
        context.packageName,
        PackageManager.PackageInfoFlags.of(0)
    )
    else context.packageManager.getPackageInfo(context.packageName, 0)

    val versionName = info.versionName

    val creditsDialog = remember { mutableStateOf(false) }
    val uriHandler = LocalUriHandler.current
    fun openUrl(url: String) {
        uriHandler.openUri(url)
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
                        text = stringResource(id = R.string.about),
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
                    PreferenceItem(
                        title = stringResource(R.string.readme),
                        description = stringResource(R.string.readme_desc),
                        icon = Icons.Outlined.Description,
                    ) { openUrl(repoUrl) }
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
                        icon = null,
                        enabled = false
                    )
                }
            }
        })

}