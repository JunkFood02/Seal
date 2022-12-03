package com.junkfood.seal.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.junkfood.seal.ui.theme.PreviewThemeLight

@Composable
fun FormatItem(
    id: String = "248",
    note: String = "1080p",
    resolution: String = "1920x1080",
    vcodec: String = "h264",
    acodec: String = "aac",
    ext: String = "",
    fileSize: Long = 1024 * 1024 * 69,
    selected: Boolean = false,
    onClick: () -> Unit = {}
) {
    val outlineColor by animateColorAsState(
        targetValue = MaterialTheme.colorScheme.run {
            if (selected) primary else outlineVariant
        })

    val elevation by animateDpAsState(if (selected) 15.dp else 0.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(MaterialTheme.shapes.medium)
            .clickable { onClick() }
            .border(
                width = 1f.dp,
                color = outlineColor,
                shape = MaterialTheme.shapes.medium
            )
            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(elevation))
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("$note ($resolution)", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.weight(1f))
            Text(
                text = "%.2f M".format(fileSize.toFloat() / 1024 / 1024),
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(end = 8.dp)
            )
            RadioButton(selected = selected, onClick = null)
        }
    }

}

@Composable
@Preview
fun PreviewFormat() {
    PreviewThemeLight {
        var selected by remember { mutableStateOf(-1) }
        Column() {


            for (i in 0..4) {
                FormatItem(selected = selected == i) { selected = i }
            }
        }
    }
}
