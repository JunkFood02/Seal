package com.junkfood.seal.ui.page.settings.about

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.junkfood.seal.R
import com.junkfood.seal.ui.common.SVGImage
import com.junkfood.seal.ui.component.BackButton
import com.junkfood.seal.ui.component.LargeTopAppBar
import com.junkfood.seal.ui.component.SponsorItem
import com.junkfood.seal.ui.svg.SponsorSVG
import com.junkfood.seal.ui.theme.autoDark
import com.kyant.monet.n1

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DonatePage(onBackPressed: () -> Unit) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        rememberTopAppBarState(),
        canScroll = { true }
    )
    val uriHandler = LocalUriHandler.current

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        modifier = Modifier,
                        text = stringResource(id = R.string.sponsors),
                    )
                }, navigationIcon = {
                    BackButton {
                        onBackPressed()
                    }
                }, scrollBehavior = scrollBehavior
            )
        }, content = {
            LazyColumn(modifier = Modifier.padding(it)) {
                item {
                    Surface(
                        modifier = Modifier
                            .fillParentMaxWidth()
                            .padding(16.dp),
                        shape = MaterialTheme.shapes.extraLarge,
                        color = 95.autoDark().n1,
                        onClick = {
                            uriHandler.openUri("https://github.com/sponsors/JunkFood02")
                        }
                    ) {
                        SVGImage(
                            SVGString = SponsorSVG,
                            contentDescription = null,
                            modifier = Modifier.padding(horizontal = 72.dp, vertical = 60.dp)
                        )
                    }
                }
                //todo: use GraphQL to query the sponsors
                item {
                    SponsorItem(
                        userName = "mohammed_9456",
                        avatarUrl = "https://github.com/Marco-9456.png",
                        userLogin = "@Marco-9456",
                        profileUrl = "https://github.com/Marco-9456"
                    ) {
                        uriHandler.openUri("https://github.com/Marco-9456")
                    }
                }
            }
        })
}