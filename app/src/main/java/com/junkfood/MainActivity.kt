package com.junkfood

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import com.junkfood.seal.R
import com.junkfood.seal.ui.home.HomeViewModel
import com.junkfood.ui.theme.SealTheme
import com.junkfood.ui.theme.Shapes

class MainActivity : ComponentActivity() {
    private lateinit var homeViewModel: HomeViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        homeViewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        setContent {

            SealTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Box(
                        modifier = Modifier
                            .padding(18f.dp, 36f.dp)
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
                        FloatingActionButton(
                            onClick = {
                                Toast.makeText(
                                    this@MainActivity,
                                    "Download",
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                            },
                            content = {
                                Icon(
                                    Icons.Outlined.FileDownload,
                                    contentDescription = "download"
                                )
                            }, modifier = Modifier
                                .padding(21f.dp)
                                .align(Alignment.BottomEnd)
                        )

                        FloatingActionButton(
                            onClick = {
                                Toast.makeText(this@MainActivity, "Paste", Toast.LENGTH_SHORT)
                                    .show()
                            },
                            content = {
                                Icon(
                                    Icons.Outlined.ContentPaste,
                                    contentDescription = "download"
                                )
                            }, modifier = Modifier
                                .padding(21f.dp)
                                .align(Alignment.BottomEnd)
                        )

                    }
                }

            }
        }
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
            progress = progress, modifier = Modifier
                .fillMaxWidth(0.79f)
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



