package com.junkfood.seal.ui.page.settings.about

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.VolunteerActivism
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.junkfood.seal.R
import com.junkfood.seal.ui.common.AsyncImageImpl
import com.junkfood.seal.ui.component.BackButton
import com.junkfood.seal.ui.component.LargeTopAppBar
import com.junkfood.seal.ui.component.PreferenceSubtitle
import com.junkfood.seal.ui.component.SponsorItem
import com.junkfood.seal.ui.component.gitHubAvatar
import com.junkfood.seal.ui.theme.autoDark
import com.junkfood.seal.util.Node
import com.junkfood.seal.util.PreferenceUtil.updateInt
import com.junkfood.seal.util.SHOW_SPONSOR_MSG
import com.junkfood.seal.util.SponsorUtil
import com.kyant.monet.n1
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val TAG = "SponsorPage"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DonatePage(onBackPressed: () -> Unit) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        rememberTopAppBarState(),
        canScroll = { true }
    )
    val uriHandler = LocalUriHandler.current
    val sponsorList = remember { mutableStateListOf<Node>() }
    LaunchedEffect(Unit) {
        launch(Dispatchers.IO) {
            SHOW_SPONSOR_MSG.updateInt(0)
            SponsorUtil.getSponsors().onFailure { Log.e(TAG, "DonatePage: ", it) }.onSuccess {
                sponsorList.addAll(it.data.user.sponsorshipsAsMaintainer.nodes.filter { node -> node.tier.monthlyPriceInDollars >= 10 })
            }
        }
    }
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        modifier = Modifier,
                        text = stringResource(id = R.string.sponsor),
                    )
                }, navigationIcon = {
                    BackButton {
                        onBackPressed()
                    }
                }, scrollBehavior = scrollBehavior
            )
        }, content = {
            LazyVerticalGrid(
                modifier = Modifier
                    .padding(it)
                    .padding(horizontal = 12.dp),
                columns = GridCells.Adaptive(100.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    PreferenceSubtitle(
                        text = stringResource(id = R.string.sponsors),
                        contentPadding = PaddingValues(start = 12.dp, top = 24.dp, bottom = 12.dp)
                    )
                }

                items(sponsorList) { user ->
                    SponsorItem(
                        userName = user.sponsorEntity.name,
                        userLogin = user.sponsorEntity.login,
                    ) {
                        uriHandler.openUri("https://github.com/${user.sponsorEntity.login}")
                    }
                }
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Surface(
                        shape = CardDefaults.shape,
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = 95.autoDark().n1
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(12.dp)
                                .fillMaxWidth(),
                        ) {
                            Text(
                                modifier = Modifier
                                    .padding(bottom = 4.dp)
                                    .align(Alignment.CenterHorizontally),
                                text = stringResource(id = R.string.msg_from_developer),
                                style = MaterialTheme.typography.labelLarge
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                verticalAlignment = Alignment.Bottom
                            ) {
                                AsyncImageImpl(
                                    model = gitHubAvatar("JunkFood02"),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .aspectRatio(1f, true)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                                Column {
                                    Conversation(
                                        modifier = Modifier.padding(bottom = 12.dp),
                                        text = stringResource(id = R.string.sponsor_msg)
                                    )
                                    Conversation(
                                        modifier = Modifier,
                                        text = stringResource(R.string.sponsor_msg2)
                                    )
                                }
                            }

                            FilledTonalButton(
                                onClick = {
                                    uriHandler.openUri("https://github.com/sponsors/JunkFood02")
                                },
                                contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Icon(
                                    modifier = Modifier
                                        .padding(end = 8.dp)
                                        .size(ButtonDefaults.IconSize),
                                    imageVector = Icons.Outlined.VolunteerActivism,
                                    contentDescription = null
                                )

                                Text(text = stringResource(id = R.string.sponsor))
                            }
                        }

                    }
                }

            }
        })
}

@Composable
@Preview
fun SponsorPagePreview() {
    DonatePage {}
}

@Composable
fun Conversation(modifier: Modifier = Modifier, text: String) {
    Row(
        modifier = modifier
            .padding(horizontal = 12.dp)
            .clip(MaterialTheme.shapes.extraLarge)
            .background(90.autoDark().n1)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}