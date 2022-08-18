package com.junkfood.seal.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.junkfood.seal.R

@Composable
@Preview
fun DownloadTaskItem(
    imageModel: Any = R.drawable.sample,
    title: String = "sample title",
    author: String = "author sample"
) {
    Card() {
        Row(modifier = Modifier.fillMaxWidth()) {
            /*AsyncImage(
                modifier = Modifier
                    .fillMaxWidth(0.45f)
                    .aspectRatio(16f / 9f, matchHeightConstraintsFirst = true),
                model = imageModel,
                contentDescription = stringResource(R.string.thumbnail),
                contentScale = ContentScale.Crop,
            )*/
            Image(
                painter = painterResource(imageModel as Int),
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .padding(8.dp)
                    .clip(MaterialTheme.shapes.small)
                    .aspectRatio(16f / 9f, matchHeightConstraintsFirst = true),
                contentDescription = stringResource(R.string.thumbnail),
                contentScale = ContentScale.Crop,
            )
            Column(
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxWidth()
                    .height(IntrinsicSize.Max),
                verticalArrangement = Arrangement.Top
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2, overflow = TextOverflow.Ellipsis
                )
                Text(
                    modifier = Modifier.padding(top = 3.dp),
                    text = author,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
        ) {
            Column(modifier = Modifier.weight(4f)) {
                Text(
                    "this is sample text of a very long progress text." +
                            "this is sample text of a very long progress text"
                )
            }
            val containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(0.dp)
            val contentColor = contentColorFor(backgroundColor = containerColor)
            Row(modifier = Modifier.weight(2f), horizontalArrangement = Arrangement.End) {
                FilledIconButton(
                    onClick = { },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = containerColor, contentColor = contentColor
                    )
                ) {
                    Icon(
                        Icons.Rounded.Stop,
                        null,
                    )
                }
                FilledTonalIconButton(
                    onClick = { }, colors = IconButtonDefaults.iconButtonColors(
                        containerColor = containerColor, contentColor = contentColor
                    )
                ) {
                    Icon(
                        Icons.Outlined.MoreHoriz,
                        null,
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}