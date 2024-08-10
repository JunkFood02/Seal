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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.junkfood.seal.ui.common.LocalDarkTheme
import com.junkfood.seal.ui.component.rememberSheetState
import com.junkfood.seal.ui.page.downloadv2.Config
import com.junkfood.seal.ui.page.downloadv2.ConfigureDialog
import com.junkfood.seal.ui.page.downloadv2.DownloadDialogViewModel
import com.junkfood.seal.ui.page.downloadv2.DownloadDialogViewModel.Action
import com.junkfood.seal.ui.page.downloadv2.DownloadDialogViewModel.SelectionState
import com.junkfood.seal.ui.page.downloadv2.DownloadDialogViewModel.SheetValue
import com.junkfood.seal.ui.page.downloadv2.FormatPage
import com.junkfood.seal.ui.theme.SealTheme
import com.junkfood.seal.util.CONFIGURE
import com.junkfood.seal.util.CUSTOM_COMMAND
import com.junkfood.seal.util.DownloadUtil
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
                intent.dataString?.let { url = it }
            }

            Intent.ACTION_SEND -> {
                intent.getStringExtra(Intent.EXTRA_TEXT)?.let { sharedContent ->
                    intent.removeExtra(Intent.EXTRA_TEXT)
                    matchUrlFromSharedText(sharedContent).let { matchedUrl -> url = matchedUrl }
                }
            }
        }
    }

    private fun onDownloadStarted(customCommand: Boolean) {
        if (customCommand) Downloader.executeCommandWithUrl(url)
        else Downloader.quickDownload(url = url)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        window.run {
            setBackgroundDrawable(ColorDrawable(0))
            setLayout(
                WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY)
            } else {
                setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT)
            }
        }
        handleShareIntent(intent)

        if (Build.VERSION.SDK_INT < 33) {
            runBlocking { setLanguage(PreferenceUtil.getLocaleFromPreference()) }
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
            val viewModel: DownloadDialogViewModel = viewModel()

            SealTheme(
                darkTheme = LocalDarkTheme.current.isDarkTheme(),
                isHighContrastModeEnabled = LocalDarkTheme.current.isHighContrastModeEnabled,
            ) {
                var preferences by remember {
                    mutableStateOf(DownloadUtil.DownloadPreferences.createFromPreferences())
                }
                val sheetValue = viewModel.sheetValueFlow.collectAsStateWithLifecycle().value
                val state = viewModel.sheetStateFlow.collectAsStateWithLifecycle().value
                val sheetState =
                    rememberSheetState(showSheet = sheetValue == SheetValue.Expanded) { showSheet ->
                        if (showSheet) {
                            viewModel.postAction(Action.ShowSheet)
                        } else {
                            viewModel.postAction(Action.HideSheet)
                        }
                    }

                LaunchedEffect(url) { viewModel.postAction(Action.ShowSheet) }

                val selectionState =
                    viewModel.selectionStateFlow.collectAsStateWithLifecycle().value

                ConfigureDialog(
                    url = url,
                    state = state,
                    sheetState = sheetState,
                    config = Config(),
                    preferences = preferences,
                    onPreferencesUpdate = { preferences = it },
                    onActionPosted = { viewModel.postAction(it) },
                )
                when (selectionState) {
                    is SelectionState.FormatSelection ->
                        FormatPage(
                            state = selectionState,
                            onDismissRequest = {
                                viewModel.postAction(Action.Reset)
                                this.finish()
                            })
                    else -> {}
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        handleShareIntent(intent)
        super.onNewIntent(intent)
    }
}
