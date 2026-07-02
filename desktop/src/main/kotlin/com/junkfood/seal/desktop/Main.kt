package com.junkfood.seal.desktop

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import com.junkfood.seal.desktop.platform.SharedLinks
import com.junkfood.seal.desktop.platform.SingleInstance
import com.junkfood.seal.desktop.theme.SealDesktopTheme
import com.junkfood.seal.desktop.ui.SealApp
import java.awt.Dimension
import java.awt.Frame

fun main(args: Array<String>) {
    // Links shared from other apps arrive as process arguments: `seal://` protocol activations,
    // Windows "Open with"/default-apps launches, or a plain `Seal.exe <url>` from the CLI.
    val sharedLink = SharedLinks.fromArgs(args)

    // If Seal is already running, hand the link (or just a focus request) to that instance and
    // exit — shared links should open in the existing window, not spawn a second app.
    val instance = SingleInstance.acquire(sharedLink) ?: return

    application {
        var sharedUrl by remember { mutableStateOf<String?>(null) }

        Window(
            onCloseRequest = ::exitApplication,
            title = "Seal",
            state =
                WindowState(
                    size = DpSize(1100.dp, 760.dp),
                    position = WindowPosition(Alignment.Center),
                ),
        ) {
            // Don't let the window shrink below a usable desktop layout.
            window.minimumSize = Dimension(720, 540)

            // Surface links forwarded by later launches: un-minimise, focus, then hand the URL
            // to the UI so the download dialog opens prefilled.
            LaunchedEffect(Unit) {
                for (incoming in instance.incomingUrls) {
                    if ((window.extendedState and Frame.ICONIFIED) != 0) {
                        window.extendedState = window.extendedState and Frame.ICONIFIED.inv()
                    }
                    window.toFront()
                    window.requestFocus()
                    if (incoming.isNotBlank()) sharedUrl = incoming
                }
            }

            SealDesktopTheme {
                SealApp(sharedUrl = sharedUrl, onSharedUrlConsumed = { sharedUrl = null })
            }
        }
    }
}
