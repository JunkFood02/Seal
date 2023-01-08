package com.junkfood.seal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import com.junkfood.seal.ui.common.LocalDarkTheme
import com.junkfood.seal.ui.common.LocalDynamicColorSwitch
import com.junkfood.seal.ui.common.LocalSeedColor
import com.junkfood.seal.ui.component.ConfirmButton
import com.junkfood.seal.ui.theme.SealTheme

class ShareActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        ViewCompat.setOnApplyWindowInsetsListener(window.decorView) { v, insets ->
            v.setPadding(0, 0, 0, 0)
            insets
        }

        setContent {
            SealTheme(
                darkTheme = LocalDarkTheme.current.isDarkTheme(),
                isHighContrastModeEnabled = LocalDarkTheme.current.isHighContrastModeEnabled,
                seedColor = LocalSeedColor.current,
                isDynamicColorEnabled = LocalDynamicColorSwitch.current,
            ) {
                var showDialog by remember { mutableStateOf(true) }
                LaunchedEffect(showDialog) {
                    if (!showDialog) finish()
                }
                if (showDialog)
                    AlertDialog(onDismissRequest = { showDialog = false },
                        title = { Text(text = "Title") },
                        confirmButton = { ConfirmButton { showDialog = false } })

            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    SealTheme {
        Greeting("Android")
    }
}