package com.kyant.monet

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import io.material.hct.Hct

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
        private val M3SurfaceTonalValues = doubleArrayOf(
            0.0,
            4.0,
            6.0,
            10.0,
            12.0,
            17.0,
            20.0,
            22.0,
            24.0,
            30.0,
            40.0,
            50.0,
            60.0,
            70.0,
            80.0,
            85.0,
            87.0,
            90.0,
            92.0,
            94.0,
            95.0,
            96.0,
            98.0,
            99.0,
            100.0
        )

        fun Color.toTonalPalettes(
            style: PaletteStyle = PaletteStyle.TonalSpot,
            tonalValues: DoubleArray = M3TonalValues
        ): TonalPalettes = TonalPalettes(
            keyColor = this,
            style = style,
            accent1 = tonalValues.associateWith { transform(it, style.accent1Spec) },
            accent2 = tonalValues.associateWith { transform(it, style.accent2Spec) },
            accent3 = tonalValues.associateWith { transform(it, style.accent3Spec) },
            neutral1 = M3SurfaceTonalValues.associateWith { transform(it, style.neutral1Spec) },
            neutral2 = tonalValues.associateWith { transform(it, style.neutral2Spec) }
        )


        private fun Color.toTonalPalette(
            tonalValues: DoubleArray = M3TonalValues
        ): TonalPalette =
            tonalValues.associateWith { transform(it, ColorSpec()) }


        /**
         * Convert an existing `ColorScheme` to an MD3 `TonalPalettes`
         *
         * Notice: This function is `PaletteStyle` independent
         *
         * @see ColorScheme
         * @see TonalPalettes
         */
        fun ColorScheme.toTonalPalettes(
            tonalValues: DoubleArray = M3TonalValues
        ): TonalPalettes = TonalPalettes(
            keyColor = primary,
            accent1 = primary.toTonalPalette(tonalValues),
            accent2 = secondary.toTonalPalette(tonalValues),
            accent3 = tertiary.toTonalPalette(tonalValues),
            neutral1 = surface.toTonalPalette(M3SurfaceTonalValues),
            neutral2 = surfaceVariant.toTonalPalette(tonalValues),
        )

        private fun Color.transform(tone: Double, spec: ColorSpec): Color {
            return Color(Hct.fromInt(this.toArgb()).apply {
                setTone(tone)
                setChroma(spec.chroma(this.chroma))
                setHue(spec.hueShift(this.hue) + this.hue)
            }.toInt())
        }

    }
}
