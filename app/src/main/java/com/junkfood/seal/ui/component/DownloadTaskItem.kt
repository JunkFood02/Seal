package com.junkfood.seal.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.junkfood.seal.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun DownloadTaskItem(
    modifier: Modifier = Modifier.padding(12.dp),
    imageModel: Any = R.drawable.ic_launcher_foreground,
    title: String = "sample title sample title sample title ",
    author: String = "author sample author sample author sample ",
    isExpanded: Boolean = false,
) {
    var isExpanded by remember { mutableStateOf(isExpanded) }
    ElevatedCard(modifier = modifier, onClick = { isExpanded = !isExpanded }) {
        Column() {


            Box() {
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
                        painter = painterResource(id = R.drawable.sample),
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth(0.5f)
                            .clip(MaterialTheme.shapes.small)
                            .aspectRatio(16f / 10f, matchHeightConstraintsFirst = true),
                        contentDescription = stringResource(R.string.thumbnail),
                        contentScale = ContentScale.Crop,
                    )
                    Column(
                        modifier = Modifier
                            .padding(vertical = 12.dp)
                            .padding(end = 12.dp)
                            .fillMaxWidth()
                            .height(IntrinsicSize.Max),
                        verticalArrangement = Arrangement.Top
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleSmall,
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
                FilledTonalIconButton(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp)
                        .size(24.dp),
                    onClick = { isExpanded = !isExpanded }) {
                    Icon(
                        if (isExpanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                        null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            val animatedDp = animateDpAsState(targetValue = if (isExpanded) 12.dp else 0.dp)
            AnimatedVisibility(visible = isExpanded) {
                Divider(
                    Modifier
                        .fillMaxWidth()
                        .height(0.5.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Max)
                        .padding(horizontal = 12.dp)
                        .padding(top = 4.dp, bottom = 4.dp),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(4f),
                    ) {
                        Text(
                            text =
                            "this is sample text of a very long progress text." +
                                    "this is sample text of a very long progress text",
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    val containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(15.dp)
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
                            )
                        }
                    }
                }
            }
            LinearProgressIndicator(
                Modifier
//                    .clip(MaterialTheme.shapes.extraLarge)
                    .fillMaxWidth()
                    .padding(),
            )
        }
    }
}

@Composable
@Preview
fun ExpandedCard() {
    DownloadTaskItem(isExpanded = true)
}