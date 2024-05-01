package com.junkfood.seal

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.junkfood.seal.ui.common.LocalDarkTheme
import com.junkfood.seal.ui.common.LocalWindowWidthState
import com.junkfood.seal.ui.common.SettingsProvider
import com.junkfood.seal.ui.page.download.DownloadSettingDialog
import com.junkfood.seal.ui.theme.SealTheme
import com.junkfood.seal.util.CONFIGURE
import com.junkfood.seal.util.CUSTOM_COMMAND
import com.junkfood.seal.util.PreferenceUtil
import com.junkfood.seal.util.PreferenceUtil.getBoolean
import com.junkfood.seal.util.matchUrlFromSharedText
import com.junkfood.seal.util.setLanguage
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

    @OptIn(
        ExperimentalMaterial3WindowSizeClassApi::class
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

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

        if (Build.VERSION.SDK_INT < 33) {
            runBlocking {
                setLanguage(PreferenceUtil.getLocaleFromPreference())
            }
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
                ) {


                    var showDialog by remember { mutableStateOf(true) }

                    val useDialog = LocalWindowWidthState.current != WindowWidthSizeClass.Compact
                    DownloadSettingDialog(
                        useDialog = useDialog,
                        showDialog = showDialog,
                        isQuickDownload = true,
                        onDownloadConfirm = {
                            onDownloadStarted(PreferenceUtil.getValue(CUSTOM_COMMAND))
                        },
                        onDismissRequest = {
                            showDialog = false
                            this@QuickDownloadActivity.finish()
                        },
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        handleShareIntent(intent)
        super.onNewIntent(intent)
    }
}