package com.junkfood.seal.ui.common

import android.graphics.drawable.PictureDrawable
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImagePainter
import coil.imageLoader
import coil.request.ImageRequest
import com.caverock.androidsvg.SVG
import com.junkfood.seal.R
import com.junkfood.seal.ui.component.parseDynamicColor
import com.kyant.monet.LocalTonalPalettes
import com.kyant.monet.TonalPalettes


@Composable
fun SVGImage(
    SVGString: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    transform: (AsyncImagePainter.State) -> AsyncImagePainter.State = AsyncImagePainter.DefaultTransform,
    onState: ((AsyncImagePainter.State) -> Unit)? = null,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null,
    filterQuality: FilterQuality = DrawScope.DefaultFilterQuality,
    tonalPalettes: TonalPalettes = LocalTonalPalettes.current,
    isDarkTheme: Boolean = LocalDarkTheme.current.isDarkTheme()
) {
    val horizontalPadding =
        PaddingValues(horizontal = if (LocalWindowWidthState.current != WindowWidthSizeClass.Compact) 100.dp else 0.dp)
    var size by remember { mutableStateOf(IntSize.Zero) }

    val pi by remember(tonalPalettes, isDarkTheme, size) {
        mutableStateOf(
            PictureDrawable(
                SVG.getFromString(
                    SVGString.parseDynamicColor(
                        tonalPalettes = tonalPalettes,
                        isDarkTheme = isDarkTheme
                    )
                ).renderToPicture(size.width, size.height)
            )
        )
    }
    Row(
        modifier = modifier
            .padding(horizontalPadding)
            .aspectRatio(1.38f)
            .onGloballyPositioned {
                if (it.size != IntSize.Zero) {
                    size = it.size
                }
            },
    ) {
        Crossfade(targetState = pi) {
            AsyncImageImpl(
                it,
                contentDescription,
                Modifier,
                transform,
                onState,
                alignment,
                contentScale,
                alpha,
                colorFilter,
                filterQuality
            )
        }
    }

}

@Composable
fun AsyncImageImpl(
    model: Any,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    transform: (AsyncImagePainter.State) -> AsyncImagePainter.State = AsyncImagePainter.DefaultTransform,
    onState: ((AsyncImagePainter.State) -> Unit)? = null,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null,
    filterQuality: FilterQuality = DrawScope.DefaultFilterQuality,
    isPreview: Boolean = false
) {
    if (isPreview) Image(
        painter = painterResource(R.drawable.sample),
        contentDescription = contentDescription,
        modifier = modifier,
        alignment = alignment,
        contentScale = contentScale,
        alpha = alpha,
        colorFilter = colorFilter,
    )
    else coil.compose.AsyncImage(
        model = ImageRequest.Builder(LocalContext.current).data(model).crossfade(true).build(),
        contentDescription = contentDescription,
        imageLoader = LocalContext.current.imageLoader,
        modifier = modifier,
        transform = transform,
        onState = onState,
        alignment = alignment,
        contentScale = contentScale,
        alpha = alpha,
        colorFilter = colorFilter,
        filterQuality = filterQuality
    )
}