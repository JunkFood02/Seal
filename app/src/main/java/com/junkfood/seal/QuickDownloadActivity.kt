package com.junkfood.seal

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.core.os.LocaleListCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import com.junkfood.seal.ui.common.LocalDarkTheme
import com.junkfood.seal.ui.common.LocalDynamicColorSwitch
import com.junkfood.seal.ui.common.LocalWindowWidthState
import com.junkfood.seal.ui.common.SettingsProvider
import com.junkfood.seal.ui.page.download.DownloadSettingDialog
import com.junkfood.seal.ui.theme.SealTheme
import com.junkfood.seal.util.CONFIGURE
import com.junkfood.seal.util.CUSTOM_COMMAND
import com.junkfood.seal.util.PreferenceUtil
import com.junkfood.seal.util.PreferenceUtil.getBoolean
import com.junkfood.seal.util.matchUrlFromSharedText
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

private const val TAG = "ShareActivity"

class QuickDownloadActivity : ComponentActivity() {
    private var url: String = ""
    private fun handleShareIntent(intent: Intent) {
        Log.d(TAG, "handleShareIntent: $intent")
        when (intent.action) {
            Intent.ACTION_VIEW -> {
                intent.dataString?.let {
                    url = it
                }
            }

            Intent.ACTION_SEND -> {
                intent.getStringExtra(Intent.EXTRA_TEXT)
                    ?.let { sharedContent ->
                        intent.removeExtra(Intent.EXTRA_TEXT)
                        matchUrlFromSharedText(sharedContent)
                            .let { matchedUrl ->
                                url = matchedUrl
                            }
                    }
            }
        }
    }

    private fun onDownloadStarted(customCommand: Boolean) {
        if (customCommand)
            Downloader.executeCommandWithUrl(url)
        else
            Downloader.quickDownload(url = url)
    }

    @OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        ViewCompat.setOnApplyWindowInsetsListener(window.decorView) { v, insets ->
            v.setPadding(0, 0, 0, 0)
            insets
        }

        window.run {
            setBackgroundDrawable(ColorDrawable(0))
            setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY)
            } else {
                setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT)
            }
        }
        handleShareIntent(intent)
        runBlocking {
            if (Build.VERSION.SDK_INT < 33)
                AppCompatDelegate.setApplicationLocales(
                    LocaleListCompat.forLanguageTags(PreferenceUtil.getLanguageConfiguration())
                )
        }
        val isDialogEnabled = CONFIGURE.getBoolean()

        if (url.isEmpty()) {
            finish()
        }

        if (!isDialogEnabled) {
            onDownloadStarted(CUSTOM_COMMAND.getBoolean())
            this.finish()
        }

        setContent {
            val scope = rememberCoroutineScope()
            SettingsProvider(
                windowWidthSizeClass = calculateWindowSizeClass(this).widthSizeClass
            ) {
                SealTheme(
                    darkTheme = LocalDarkTheme.current.isDarkTheme(),
                    isHighContrastModeEnabled = LocalDarkTheme.current.isHighContrastModeEnabled,
                    isDynamicColorEnabled = LocalDynamicColorSwitch.current,
                ) {


                    var showDialog by remember { mutableStateOf(true) }
                    val drawerState =
                        rememberModalBottomSheetState(
                            initialValue = ModalBottomSheetValue.Expanded,
                            skipHalfExpanded = true
                        )

                    LaunchedEffect(drawerState.currentValue, showDialog) {
                        if (drawerState.currentValue == ModalBottomSheetValue.Hidden || !showDialog)
                            this@QuickDownloadActivity.finish()
                    }

                    DownloadSettingDialog(
                        useDialog = LocalWindowWidthState.current != WindowWidthSizeClass.Compact,
                        dialogState = showDialog,
                        isShareActivity = true,
                        drawerState = drawerState,
                        confirm = {
                            onDownloadStarted(PreferenceUtil.getValue(CUSTOM_COMMAND))
                        }) {
                        scope.launch { drawerState.hide() }
                        showDialog = false
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        intent?.let { handleShareIntent(it) }
        super.onNewIntent(intent)
    }
}