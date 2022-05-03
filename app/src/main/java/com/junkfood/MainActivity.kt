package com.junkfood

import android.Manifest
import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import com.junkfood.seal.BaseApplication
import com.junkfood.seal.BaseApplication.Companion.context
import com.junkfood.seal.BaseApplication.Companion.updateDownloadDir
import com.junkfood.seal.R
import com.junkfood.seal.ui.home.HomeViewModel
import com.junkfood.seal.util.DownloadUtil
import com.junkfood.seal.util.FileUtil
import com.junkfood.seal.util.TextUtil
import com.junkfood.ui.theme.SealTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    private lateinit var homeViewModel: HomeViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        homeViewModel = ViewModelProvider(this)[HomeViewModel::class.java]

        val activityResultLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { result ->
            var permissionGranted = true
            for (b in result.values) {
                permissionGranted = permissionGranted && b
            }
            if (permissionGranted || Build.VERSION.SDK_INT > 29) {
                updateDownloadDir()
                with(homeViewModel.url.value) {
                    if (isNullOrBlank()) Toast.makeText(
                        this@MainActivity,
                        "Url is empty",
                        Toast.LENGTH_SHORT
                    ).show()
                    else {
                        homeViewModel.viewModelScope.launch(Dispatchers.IO) {
                            val downloadResult = DownloadUtil.downloadVideo(
                                this@with
                            ) { fl: Float, _: Long, _: String -> homeViewModel.updateProgress(fl) }
                            withContext(Dispatchers.Main) {
                                homeViewModel.updateProgress(100f)
                                if (PreferenceManager.getDefaultSharedPreferences(BaseApplication.context)
                                        .getBoolean("open", false)
                                ) FileUtil.openFile(
                                    this@MainActivity,
                                    downloadResult
                                )
                            }
                        }
                    }
                }
            } else Toast.makeText(
                context,
                getString(R.string.permission_denied),
                Toast.LENGTH_SHORT
            )
                .show()
        }

        setContent {
            SealTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16f.dp)
                            .fillMaxSize()
                    ) {
                        TitleBar(title = resources.getString(R.string.app_name)) {
                            Toast.makeText(
                                this@MainActivity,
                                "Settings",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        Box(
                            modifier = Modifier
                                .padding(16f.dp)
                                .fillMaxSize()
                        )
                        {
                            Column() {
                                val progress = homeViewModel.progress.observeAsState(0f).value
                                SimpleText(resources.getString(R.string.greeting))
                                InputUrl(
                                    url = homeViewModel.url,
                                    hint = resources.getString(R.string.video_url)
                                )
                                ProgressBar(progress = progress)
                            }
                            Column(modifier = Modifier.align(Alignment.BottomEnd)) {
                                FABs(
                                    downloadCallback = {
                                        activityResultLauncher.launch(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE))
                                    }
                                )
                                {
                                    TextUtil.readUrlFromClipboard()
                                        ?.let { homeViewModel.url.value = it }
                                }
                            }


                        }
                    }
                }
            }
        }
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    SealTheme {
        Greeting("Android")
    }
}

@Composable
fun SimpleText(text: String) {
    with(MaterialTheme.typography.titleLarge) {
        Text(text = text, fontSize = fontSize, fontWeight = fontWeight)
    }
}

@Composable
fun InputUrl(url: MutableLiveData<String>, hint: String) {
    val urlState = url.observeAsState("").value
    OutlinedTextField(
        value = urlState,
        onValueChange = { url.value = it },
        label = { Text(hint) },
        modifier = Modifier
            .padding(0f.dp, 36f.dp)
            .fillMaxWidth()
    )
}

@Composable
fun ProgressBar(progress: Float) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
    ) {
        LinearProgressIndicator(
            progress = progress / 100f, modifier = Modifier
                .fillMaxWidth(0.75f)
        )
        Text(
            text = "$progress%",
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(6f.dp, 0f.dp)
        )
    }
}

@Composable
fun TitleBar(title: String, onClick: () -> Unit) {
    LargeTopAppBar(title = { Text(title) }, actions = {
        IconButton(onClick = onClick) {
            Icon(
                imageVector = Icons.Outlined.Settings,
                contentDescription = "Localized description"
            )
        }
    })
}

@Composable
fun FABs(downloadCallback: () -> Unit, pasteCallback: () -> Unit) {
    FloatingActionButton(
        onClick = downloadCallback,
        content = {
            Icon(
                Icons.Outlined.FileDownload,
                contentDescription = "download"
            )
        }, modifier = Modifier
            .padding(vertical = 12f.dp)
    )

    FloatingActionButton(
        onClick = pasteCallback,
        content = {
            Icon(
                Icons.Outlined.ContentPaste,
                contentDescription = "download"
            )
        }, modifier = Modifier
            .padding(vertical = 12f.dp)
    )
}


