package com.junkfood.seal.ui.svg

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.junkfood.seal.ui.svg.drawablevectors.Coder
import com.junkfood.seal.ui.svg.drawablevectors.Download
import com.junkfood.seal.ui.svg.drawablevectors.VideoFiles
import com.junkfood.seal.ui.svg.drawablevectors.VideoSteaming
import com.junkfood.seal.ui.theme.SealTheme

@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Night", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun Download() {
    SealTheme {
        Surface {
            Column {
                Image(
                    imageVector = DynamicColorImageVectors.Download,
                    contentDescription = null,
                    modifier = Modifier.aspectRatio(16 / 9f)
                )
                Image(
                    imageVector = Coder(),
                    contentDescription = null,
                    modifier = Modifier.aspectRatio(16 / 9f)
                )
                Image(
                    imageVector = DynamicColorImageVectors.VideoFiles,
                    contentDescription = null,
                    modifier = Modifier.aspectRatio(16 / 9f)
                )
                Image(
                    imageVector = DynamicColorImageVectors.VideoSteaming,
                    contentDescription = null,
                    modifier = Modifier.aspectRatio(16 / 9f)
                )
            }


        }

    }

}