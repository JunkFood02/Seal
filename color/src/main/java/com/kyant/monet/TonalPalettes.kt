package com.kyant.monet

import androidx.compose.ui.graphics.Color
import com.kyant.monet.Cam16.Companion.toCam16

typealias TonalPalette = Map<Double, Color>

class TonalPalettes(
    val keyColor: Color,
    val style: PaletteStyle = PaletteStyle.TonalSpot,
    private val accent1: TonalPalette,
    private val accent2: TonalPalette,
    private val accent3: TonalPalette,
    private val neutral1: TonalPalette,
    private val neutral2: TonalPalette
) {
    infix fun accent1(tone: Double): Color = accent1.getOrElse(tone) {
        keyColor.transform(tone, style.accent1Spec)
    }

    infix fun accent2(tone: Double): Color = accent2.getOrElse(tone) {
        keyColor.transform(tone, style.accent2Spec)
    }

    infix fun accent3(tone: Double): Color = accent3.getOrElse(tone) {
        keyColor.transform(tone, style.accent3Spec)
    }

    infix fun neutral1(tone: Double): Color = neutral1.getOrElse(tone) {
        keyColor.transform(tone, style.neutral1Spec)
    }

    infix fun neutral2(tone: Double): Color = neutral2.getOrElse(tone) {
        keyColor.transform(tone, style.neutral2Spec)
    }

    companion object {
        private val M3TonalValues = doubleArrayOf(
            0.0, 10.0, 20.0, 30.0, 40.0, 50.0, 60.0, 70.0, 80.0, 85.0, 90.0, 95.0, 99.0, 100.0
        )

        fun Color.toTonalPalettes(
            style: PaletteStyle = PaletteStyle.TonalSpot,
            tonalValues: DoubleArray = M3TonalValues
        ): TonalPalettes {
            return TonalPalettes(
                keyColor = this,
                style = style,
                accent1 = tonalValues.associateWith { transform(it, style.accent1Spec) },
                accent2 = tonalValues.associateWith { transform(it, style.accent2Spec) },
                accent3 = tonalValues.associateWith { transform(it, style.accent3Spec) },
                neutral1 = tonalValues.associateWith { transform(it, style.neutral1Spec) },
                neutral2 = tonalValues.associateWith { transform(it, style.neutral2Spec) }
            )
        }

        private fun Color.transform(tone: Double, spec: ColorSpec): Color {
            val cam = toSrgb().toCieXyz().toCam16()
            return Hct(
                h = cam.h + spec.hueShift(cam.h),
                c = (
                    if (tone >= 90.0) spec.chroma(cam.c).coerceAtMost(40.0)
                    else spec.chroma(cam.c)
                    ) * 2.0 / 3.0,
                t = tone
            ).toSrgb().toColor()
        }
    }
}
