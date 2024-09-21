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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.junkfood.seal.ui.common.LocalDarkTheme
import com.junkfood.seal.ui.common.SettingsProvider
import com.junkfood.seal.ui.page.downloadv2.Config
import com.junkfood.seal.ui.page.downloadv2.DownloadDialog
import com.junkfood.seal.ui.page.downloadv2.DownloadDialogViewModel
import com.junkfood.seal.ui.page.downloadv2.DownloadDialogViewModel.Action
import com.junkfood.seal.ui.page.downloadv2.DownloadDialogViewModel.SelectionState
import com.junkfood.seal.ui.page.downloadv2.DownloadDialogViewModel.SheetValue
import com.junkfood.seal.ui.page.downloadv2.FormatPage
import com.junkfood.seal.ui.theme.SealTheme
import com.junkfood.seal.util.DownloadUtil
import com.junkfood.seal.util.PreferenceUtil
import com.junkfood.seal.util.matchUrlFromSharedText
import com.junkfood.seal.util.setLanguage
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.KoinContext

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

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class, ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        window.run {
            setBackgroundDrawable(ColorDrawable(0))
            setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
            )
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

        if (url.isEmpty()) {
            finish()
        }

        setContent {
            KoinContext {
                val viewModel: DownloadDialogViewModel = koinViewModel()

                SettingsProvider(calculateWindowSizeClass(this).widthSizeClass) {
                    SealTheme(
                        darkTheme = LocalDarkTheme.current.isDarkTheme(),
                        isHighContrastModeEnabled = LocalDarkTheme.current.isHighContrastModeEnabled,
                    ) {
                        var preferences by remember {
                            mutableStateOf(DownloadUtil.DownloadPreferences.createFromPreferences())
                        }
                        val sheetValue =
                            viewModel.sheetValueFlow.collectAsStateWithLifecycle().value
                        val state = viewModel.sheetStateFlow.collectAsStateWithLifecycle().value

                        LaunchedEffect(url) {
                            viewModel.postAction(Action.ShowSheet(listOf(url)))
                        }

                        val selectionState =
                            viewModel.selectionStateFlow.collectAsStateWithLifecycle().value

                        val scope = rememberCoroutineScope()

                        if (sheetValue == SheetValue.Expanded) {
                            val sheetState =
                                rememberModalBottomSheetState(skipPartiallyExpanded = true)

                            DownloadDialog(
                                state = state,
                                sheetState = sheetState,
                                config = Config(),
                                preferences = preferences,
                                onPreferencesUpdate = { preferences = it },
                                onActionPost = {
                                    viewModel.postAction(it)
                                    if (it !is Action.FetchFormats && it !is Action.FetchPlaylist) {
                                        scope.launch {
                                            sheetState.hide()
                                        }.invokeOnCompletion { this@QuickDownloadActivity.finish() }
                                    }
                                },
                            )
                        }
                        when (selectionState) {
                            is SelectionState.FormatSelection ->
                                FormatPage(
                                    state = selectionState,
                                    onDismissRequest = {
                                        viewModel.postAction(Action.Reset)
                                        this.finish()
                                    },
                                )

                            SelectionState.Idle -> {}
                            is SelectionState.PlaylistSelection -> {}
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        handleShareIntent(intent)
        super.onNewIntent(intent)
    }
}
