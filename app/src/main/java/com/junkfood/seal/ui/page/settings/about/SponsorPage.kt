package com.junkfood.seal.ui.page.settings.about

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.House
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.VolunteerActivism
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.junkfood.seal.R
import com.junkfood.seal.ui.common.AsyncImageImpl
import com.junkfood.seal.ui.component.BackButton
import com.junkfood.seal.ui.component.HorizontalDivider
import com.junkfood.seal.ui.component.PreferenceSubtitle
import com.junkfood.seal.ui.component.SealModalBottomSheet
import com.junkfood.seal.ui.component.SponsorItem
import com.junkfood.seal.ui.component.gitHubAvatar
import com.junkfood.seal.ui.component.gitHubProfile
import com.junkfood.seal.ui.theme.SealTheme
import com.junkfood.seal.util.PreferenceUtil.updateInt
import com.junkfood.seal.util.SHOW_SPONSOR_MSG
import com.junkfood.seal.util.SocialAccount
import com.junkfood.seal.util.SocialAccounts
import com.junkfood.seal.util.SponsorEntity
import com.junkfood.seal.util.SponsorShip
import com.junkfood.seal.util.SponsorUtil
import com.junkfood.seal.util.Tier
import com.junkfood.seal.util.ToastUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val TAG = "SponsorPage"

private const val SPONSORS = "Sponsors ☕️"
private const val BACKERS = "Backers ❤️"
private const val SUPPORTERS = "Supporters 💖"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DonatePage(onNavigateBack: () -> Unit) {
    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState(),
            canScroll = { true })
    val uriHandler = LocalUriHandler.current
    val sponsorList = remember { mutableStateListOf<SponsorShip>() }
    val backerList = remember { mutableStateListOf<SponsorShip>() }
    val supporterList = remember { mutableStateListOf<SponsorShip>() }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var viewingSponsorShip by remember { mutableStateOf(SponsorShip(sponsorEntity = SponsorEntity("login"))) }
    var showSheet by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val onSponsorClick: (SponsorShip) -> Unit = {
        viewingSponsorShip = it
        showSheet = true
        scope.launch {
            delay(80)
            sheetState.show()
        }
    }

    LaunchedEffect(Unit) {
        launch(Dispatchers.IO) {
            SHOW_SPONSOR_MSG.updateInt(0)
            SponsorUtil.getSponsors().onFailure { Log.e(TAG, "DonatePage: ", it) }.onSuccess {
                it.data.viewer.sponsorshipsAsMaintainer.nodes.run {
                    sponsorList.addAll(filter { node ->
                        (node.tier?.monthlyPriceInDollars ?: 0) in 5 until 10
                    })

                    backerList.addAll(filter { node ->
                        (node.tier?.monthlyPriceInDollars ?: 0) in 10 until 25
                    })

                    supporterList.addAll(filter { node ->
                        (node.tier?.monthlyPriceInDollars ?: 0) >= 25
                    })

                }

            }
        }
    }
    Scaffold(modifier = Modifier
        .fillMaxSize()
        .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(title = {
                Text(
                    modifier = Modifier,
                    text = stringResource(id = R.string.sponsors),
                )
            }, navigationIcon = {
                BackButton {
                    onNavigateBack()
                }
            }, scrollBehavior = scrollBehavior
            )
        },
        content = { values ->
            LazyVerticalGrid(
                modifier = Modifier
                    .padding(values)
                    .padding(horizontal = 12.dp),
                columns = GridCells.Fixed(12),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                if (supporterList.isNotEmpty()) {
                    item(
                        span = { GridItemSpan(maxLineSpan) }, key = SUPPORTERS
                    ) {
                        PreferenceSubtitle(
                            text = SUPPORTERS, contentPadding = PaddingValues(
                                start = 12.dp, top = 24.dp, bottom = 12.dp
                            )
                        )
                    }

                    items(items = supporterList,
                        span = { GridItemSpan(maxLineSpan / 3) },
                        key = { it.sponsorEntity.login }) { sponsorShip ->
                        SponsorItem(sponsorShip = sponsorShip) {
                            onSponsorClick(sponsorShip)
                        }
                    }
                }

                if (backerList.isNotEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }, key = BACKERS) {
                        PreferenceSubtitle(
                            text = BACKERS, contentPadding = PaddingValues(
                                start = 12.dp, top = 12.dp, bottom = 12.dp
                            )
                        )
                    }

                    items(items = backerList,
                        span = { GridItemSpan(maxLineSpan / 3) },
                        key = { it.sponsorEntity.login }) { sponsorShip ->
                        SponsorItem(sponsorShip = sponsorShip) {
                            onSponsorClick(sponsorShip)
                        }
                    }
                }

                if (sponsorList.isNotEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }, key = SPONSORS) {
                        PreferenceSubtitle(
                            text = SPONSORS, contentPadding = PaddingValues(
                                start = 12.dp, top = 12.dp, bottom = 12.dp
                            )
                        )
                    }

                    items(items = sponsorList,
                        span = { GridItemSpan(maxLineSpan / 4) },
                        key = { it.sponsorEntity.login }) { sponsorShip ->
                        SponsorItem(sponsorShip = sponsorShip) {
                            onSponsorClick(sponsorShip)
                        }
                    }
                }


                item(span = { GridItemSpan(maxLineSpan) }) {
                    Surface(
                        shape = CardDefaults.shape,
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLow
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

                            Button(
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
            if (showSheet) {
                SponsorDialog(sponsorShip = viewingSponsorShip, sheetState = sheetState) {
                    scope.launch {
                        sheetState.hide()
                    }.invokeOnCompletion { showSheet = false }
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
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Text(
            text = text, style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
fun LazyGridItemScope.SponsorItem(sponsorShip: SponsorShip, onClick: () -> Unit) {
    SponsorItem(
        modifier = Modifier,
        userName = sponsorShip.sponsorEntity.name,
        userLogin = sponsorShip.sponsorEntity.login,
        onClick = onClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SponsorDialog(sponsorShip: SponsorShip, sheetState: SheetState, onDismissRequest: () -> Unit) {
    val amount = sponsorShip.tier?.monthlyPriceInDollars ?: 0
    val tierText = if (amount in 5 until 10) {
        SPONSORS
    } else if (amount in 10 until 25) {
        BACKERS
    } else if (amount > 25) {
        SUPPORTERS
    } else {
        null
    }

    SealModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        horizontalPadding = PaddingValues(0.dp)
    ) {
        SponsorDialogContent(userLogin = sponsorShip.sponsorEntity.login,
            userName = sponsorShip.sponsorEntity.name,
            avatarUrl = gitHubAvatar(sponsorShip.sponsorEntity.login),
            tierText = tierText,
            website = sponsorShip.sponsorEntity.websiteUrl,
            socialLinks = sponsorShip.sponsorEntity.socialAccounts?.nodes?.map { it.url.toString() })
    }
}

@Composable
fun SponsorDialogContent(
    userLogin: String,
    userName: String?,
    avatarUrl: String,
    tierText: String? = null,
    website: String? = null,
    socialLinks: List<String>? = null
) {
    Column {


        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .padding(bottom = 16.dp)
                .height(IntrinsicSize.Min)
        ) {
            AsyncImageImpl(
                modifier = Modifier
                    .aspectRatio(1f, true)
                    .clip(CircleShape),
                model = avatarUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop
            )
            Column(
                modifier = Modifier
                    .padding(vertical = 20.dp)
                    .padding(start = 12.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = userName ?: "@$userLogin",
                    maxLines = 1,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = tierText.toString(),
                    maxLines = 1,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

        }
        Column(modifier = Modifier.fillMaxWidth()) {
            HorizontalDivider()
            LinkItem(icon = Icons.Outlined.House, link = website ?: gitHubProfile(userLogin))
            socialLinks?.forEach {
                LinkItem(icon = Icons.Outlined.Link, link = it)
            }
        }
    }
}


@Composable
private fun LinkItem(modifier: Modifier = Modifier, icon: ImageVector, link: String) {
    val uriHandler = LocalUriHandler.current
    val clipboardManager = LocalClipboardManager.current
    val linkCopiedText = stringResource(id = R.string.link_copied)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                uriHandler
                    .runCatching { openUri(link) }
                    .onFailure {
                        clipboardManager.setText(AnnotatedString(link))
                        ToastUtil.makeToast(linkCopiedText)
                    }
            }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.padding(horizontal = 16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(text = link, style = MaterialTheme.typography.titleSmall)
    }
}

@Preview
@Composable
private fun SponsorDialogContentPreview() {

    val sponsorShip = SponsorShip(
        sponsorEntity = SponsorEntity(
            "example",
            "example",
            "https://www.example.com",
            socialAccounts = SocialAccounts(buildList {
                repeat(4) {
                    add(SocialAccount(displayName = "Example", url = "https://www.example.com"))
                }
            })
        ), tier = Tier(10)
    )

    SealTheme {
        Surface {
            SponsorDialogContent(userLogin = sponsorShip.sponsorEntity.login,
                userName = sponsorShip.sponsorEntity.name,
                avatarUrl = gitHubAvatar(sponsorShip.sponsorEntity.login),
                website = sponsorShip.sponsorEntity.websiteUrl,
                socialLinks = sponsorShip.sponsorEntity.socialAccounts?.nodes?.map { it.url.toString() })
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
private fun SponsorDialogPreview() {
    val density = LocalDensity.current
    val sheetState = remember {
        SheetState(
            skipPartiallyExpanded = true, density = density, initialValue = SheetValue.Expanded
        )
    }
    val sponsorShip = SponsorShip(
        sponsorEntity = SponsorEntity(
            "example",
            "example",
            "https://www.example.com",
            socialAccounts = SocialAccounts(buildList {
                repeat(4) {
                    add(SocialAccount(displayName = "Example", url = "https://www.example.com"))
                }
            })
        ), tier = Tier(10)
    )


    SponsorDialog(sponsorShip = sponsorShip, onDismissRequest = {}, sheetState = sheetState)
}