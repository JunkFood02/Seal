package com.junkfood.seal.ui.svg.drawablevectors

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType.Companion.NonZero
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap.Companion.Butt
import androidx.compose.ui.graphics.StrokeJoin.Companion.Miter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.junkfood.seal.ui.svg.DynamicColorImageVectors
import com.junkfood.seal.ui.svg.currentColorScheme
import com.junkfood.seal.ui.theme.FixedAccentColors
import com.junkfood.seal.ui.theme.autoDark
import com.kyant.monet.n1

public val DynamicColorImageVectors.Download: ImageVector
    @Composable
    get() {
        if (_download != null && currentColorScheme == MaterialTheme.colorScheme) {
            return _download!!
        }
        currentColorScheme = MaterialTheme.colorScheme
        val color = MaterialTheme.colorScheme.primaryContainer
        _download = Builder(
            name = "Download", defaultWidth = 765.59973.dp, defaultHeight =
            667.7441.dp, viewportWidth = 765.59973f, viewportHeight = 667.7441f
        ).apply {
            path(
                fill = SolidColor(90.autoDark().n1), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(713.462f, 666.985f)
                verticalLineToRelative(-72.34f)
                reflectiveCurveTo(741.654f, 645.931f, 713.462f, 666.985f)
                close()
            }
            path(
                fill = SolidColor(90.autoDark().n1), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(715.203f, 666.972f)
                lineToRelative(-53.29f, -48.921f)
                reflectiveCurveTo(718.759f, 631.966f, 715.203f, 666.972f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF3f3d56)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(270.217f, 128.781f)
                horizontalLineToRelative(-2.978f)
                lineTo(267.239f, 47.211f)
                arcTo(47.211f, 47.211f, 0.0f, false, false, 220.029f, 0.0f)
                lineTo(47.211f, 0.0f)
                arcToRelative(47.211f, 47.211f, 0.0f, false, false, -47.211f, 47.211f)
                lineTo(0.0f, 494.712f)
                arcToRelative(47.211f, 47.211f, 0.0f, false, false, 47.211f, 47.211f)
                lineTo(220.028f, 541.923f)
                arcToRelative(47.211f, 47.211f, 0.0f, false, false, 47.211f, -47.211f)
                verticalLineToRelative(-307.868f)
                horizontalLineToRelative(2.978f)
                close()
            }
            path(
                fill = SolidColor(MaterialTheme.colorScheme.surface), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(221.934f, 12.283f)
                lineTo(199.375f, 12.283f)
                arcToRelative(16.75f, 16.75f, 0.0f, false, true, -15.508f, 23.076f)
                lineTo(84.861f, 35.359f)
                arcToRelative(16.75f, 16.75f, 0.0f, false, true, -15.508f, -23.076f)
                lineTo(48.283f, 12.283f)
                arcTo(35.256f, 35.256f, 0.0f, false, false, 13.027f, 47.539f)
                lineTo(13.027f, 494.384f)
                arcToRelative(35.256f, 35.256f, 0.0f, false, false, 35.256f, 35.256f)
                lineTo(221.934f, 529.64f)
                arcToRelative(35.256f, 35.256f, 0.0f, false, false, 35.256f, -35.256f)
                horizontalLineToRelative(0.0f)
                lineTo(257.19f, 47.539f)
                arcTo(35.256f, 35.256f, 0.0f, false, false, 221.934f, 12.283f)
                close()
            }
            path(
                fill = SolidColor(color), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(135.108f, 261.234f)
                moveToRelative(-84.446f, 0.0f)
                arcToRelative(84.446f, 84.446f, 0.0f, true, true, 168.892f, 0.0f)
                arcToRelative(84.446f, 84.446f, 0.0f, true, true, -168.892f, 0.0f)
            }
            path(
                fill = SolidColor(FixedAccentColors.onPrimaryFixed), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(160.132f, 267.004f)
                lineToRelative(-21.0f, 21.63f)
                arcToRelative(4.774f, 4.774f, 0.0f, false, true, -3.5f, 1.41f)
                horizontalLineToRelative(-0.07f)
                arcToRelative(4.814f, 4.814f, 0.0f, false, true, -3.51f, -1.41f)
                lineToRelative(-20.99f, -21.63f)
                curveToRelative(-0.07f, -0.08f, -0.15f, -0.15f, -0.22f, -0.22f)
                arcToRelative(4.641f, 4.641f, 0.0f, false, true, 0.22f, -6.55f)
                arcToRelative(5.169f, 5.169f, 0.0f, false, true, 7.08f, 0.0f)
                lineToRelative(12.44f, 13.0f)
                verticalLineToRelative(-33.52f)
                arcToRelative(5.02f, 5.02f, 0.0f, false, true, 10.03f, 0.0f)
                verticalLineToRelative(33.52f)
                lineToRelative(12.43f, -13.0f)
                arcToRelative(5.181f, 5.181f, 0.0f, false, true, 7.09f, 0.0f)
                curveToRelative(0.07f, 0.07f, 0.14f, 0.14f, 0.22f, 0.22f)
                arcTo(4.65f, 4.65f, 0.0f, false, true, 160.132f, 267.004f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF9f616a)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(590.258f, 653.281f)
                lineToRelative(-12.26f, -0.001f)
                lineToRelative(-5.832f, -47.288f)
                lineToRelative(18.094f, 0.001f)
                lineToRelative(-0.002f, 47.288f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF2f2e41)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(593.384f, 665.165f)
                lineToRelative(-39.531f, -0.001f)
                verticalLineToRelative(-0.5f)
                arcToRelative(15.387f, 15.387f, 0.0f, false, true, 15.386f, -15.386f)
                horizontalLineToRelative(0.001f)
                lineToRelative(24.144f, 0.001f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF9f616a)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(497.121f, 652.877f)
                lineToRelative(-11.844f, -3.167f)
                lineToRelative(6.58f, -47.19f)
                lineToRelative(17.48f, 4.674f)
                lineToRelative(-12.216f, 45.683f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF2f2e41)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(497.072f, 665.165f)
                lineToRelative(-38.189f, -10.212f)
                lineToRelative(0.129f, -0.483f)
                arcTo(15.387f, 15.387f, 0.0f, false, true, 477.851f, 643.58f)
                lineToRelative(0.001f, 0.0f)
                lineToRelative(23.324f, 6.237f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF2f2e41)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(597.311f, 388.269f)
                lineToRelative(10.269f, 11.93f)
                lineToRelative(-13.281f, 242.443f)
                lineToRelative(-28.368f, 0.0f)
                lineToRelative(-14.303f, -186.551f)
                lineToRelative(-44.346f, 191.554f)
                lineToRelative(-29.492f, -7.233f)
                lineToRelative(26.816f, -243.299f)
                lineToRelative(92.705f, -8.844f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFFcbcbcb)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(511.462f, 231.744f)
                lineToRelative(28.246f, -14.167f)
                lineToRelative(43.437f, 0.764f)
                lineToRelative(37.38f, 19.127f)
                lineTo(599.257f, 343.681f)
                lineToRelative(9.187f, 55.38f)
                lineToRelative(-0.0f, 0.0f)
                arcToRelative(226.532f, 226.532f, 0.0f, false, true, -108.335f, 0.892f)
                lineToRelative(-0.284f, -0.068f)
                reflectiveCurveToRelative(21.114f, -74.916f, 12.126f, -97.779f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF9f616a)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(593.177f, 175.136f)
                arcToRelative(32.002f, 32.002f, 0.0f, true, false, 0.0f, 0.237f)
                quadTo(593.178f, 175.254f, 593.177f, 175.136f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF2f2e41)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(546.845f, 156.85f)
                arcToRelative(9.079f, 9.079f, 0.0f, false, true, 6.117f, -2.468f)
                curveToRelative(3.419f, -0.111f, 8.542f, 1.103f, 10.722f, 3.883f)
                curveToRelative(1.752f, 2.234f, 1.836f, 5.307f, 1.843f, 8.147f)
                lineToRelative(0.019f, 7.836f)
                curveToRelative(0.005f, 2.318f, 0.034f, 4.735f, 1.087f, 6.801f)
                reflectiveCurveToRelative(3.471f, 3.634f, 5.68f, 2.93f)
                curveToRelative(2.62f, -0.835f, 3.772f, -4.318f, 6.45f, -4.944f)
                curveToRelative(2.011f, -0.47f, 4.077f, 0.999f, 4.956f, 2.868f)
                arcToRelative(12.439f, 12.439f, 0.0f, false, true, 0.716f, 6.086f)
                curveToRelative(-0.253f, 4.103f, -6.138f, 5.731f, -6.971f, 9.756f)
                curveToRelative(-0.482f, 2.329f, -2.169f, 6.973f, 0.0f, 6.0f)
                curveToRelative(10.0f, -1.0f, 14.389f, -6.841f, 18.966f, -12.339f)
                lineToRelative(10.323f, -12.401f)
                arcToRelative(12.121f, 12.121f, 0.0f, false, false, 2.659f, -4.302f)
                arcToRelative(11.951f, 11.951f, 0.0f, false, false, 0.273f, -3.431f)
                quadToRelative(-0.084f, -4.623f, -0.251f, -9.244f)
                curveToRelative(-0.097f, -2.692f, -0.774f, -6.088f, -3.41f, -6.641f)
                curveToRelative(-1.371f, -0.288f, -3.185f, 0.214f, -3.91f, -0.985f)
                arcToRelative(2.721f, 2.721f, 0.0f, false, true, -0.124f, -1.911f)
                curveToRelative(0.582f, -3.076f, 1.564f, -6.294f, 0.551f, -9.257f)
                curveToRelative(-1.527f, -4.468f, -6.785f, -6.302f, -11.431f, -7.145f)
                reflectiveCurveToRelative(-9.862f, -1.568f, -12.778f, -5.282f)
                arcToRelative(40.501f, 40.501f, 0.0f, false, false, -2.536f, -3.565f)
                arcToRelative(9.956f, 9.956f, 0.0f, false, false, -4.755f, -2.398f)
                arcToRelative(26.279f, 26.279f, 0.0f, false, false, -17.28f, 1.534f)
                curveToRelative(-2.247f, 1.026f, -4.505f, 2.407f, -6.97f, 2.25f)
                curveToRelative(-2.561f, -0.162f, -4.748f, -1.965f, -7.276f, -2.403f)
                curveToRelative(-4.084f, -0.708f, -7.988f, 2.351f, -10.0f, 5.975f)
                curveToRelative(-2.494f, 4.49f, -2.722f, 10.638f, 0.8f, 14.375f)
                curveToRelative(1.757f, 1.864f, 4.38f, 3.145f, 5.109f, 5.601f)
                curveToRelative(0.297f, 1.0f, 0.228f, 2.076f, 0.491f, 3.086f)
                arcToRelative(5.57f, 5.57f, 0.0f, false, false, 4.489f, 3.884f)
                curveTo(542.898f, 159.551f, 545.01f, 158.407f, 546.845f, 156.85f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF9f616a)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(507.563f, 309.185f)
                arcToRelative(11.462f, 11.462f, 0.0f, false, false, 16.65f, 5.627f)
                lineToRelative(57.353f, 30.318f)
                lineToRelative(1.857f, -13.971f)
                lineTo(527.695f, 298.296f)
                arcToRelative(11.524f, 11.524f, 0.0f, false, false, -20.131f, 10.889f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF9f616a)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(583.992f, 311.6f)
                arcToRelative(11.462f, 11.462f, 0.0f, false, true, -17.478f, 1.848f)
                lineTo(503.917f, 330.483f)
                lineToRelative(0.545f, -17.738f)
                lineToRelative(62.269f, -16.174f)
                arcToRelative(11.524f, 11.524f, 0.0f, false, true, 17.261f, 15.03f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFFcbcbcb)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(608.319f, 238.232f)
                lineToRelative(12.205f, -0.765f)
                reflectiveCurveToRelative(14.29f, 18.855f, 6.364f, 39.316f)
                curveToRelative(0.0f, 0.0f, 1.373f, 73.499f, -30.276f, 70.48f)
                reflectiveCurveToRelative(-41.65f, -3.019f, -41.65f, -3.019f)
                lineToRelative(9.5f, -26.5f)
                lineToRelative(21.253f, -6.562f)
                reflectiveCurveToRelative(-6.55f, -28.894f, 5.849f, -40.916f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFFcbcbcb)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(520.187f, 237.587f)
                lineToRelative(-1.725f, -8.843f)
                reflectiveCurveToRelative(-25.44f, -0.598f, -30.47f, 37.951f)
                curveToRelative(0.0f, 0.0f, -22.877f, 57.692f, -0.454f, 65.121f)
                reflectiveCurveToRelative(47.089f, 0.0f, 47.089f, 0.0f)
                lineToRelative(-1.858f, -25.442f)
                lineToRelative(-24.673f, -5.035f)
                reflectiveCurveToRelative(12.745f, -16.489f, 5.805f, -30.792f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFFcbcbcb)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(764.6f, 667.744f)
                horizontalLineToRelative(-381.0f)
                arcToRelative(1.0f, 1.0f, 0.0f, false, true, 0.0f, -2.0f)
                horizontalLineToRelative(381.0f)
                arcToRelative(1.0f, 1.0f, 0.0f, false, true, 0.0f, 2.0f)
                close()
            }
        }
            .build()
        return _download!!
    }

private var _download: ImageVector? = null
