@file:Suppress("unused")

package com.kyant.monet

class PaletteStyle(
    val accent1Spec: ColorSpec,
    val accent2Spec: ColorSpec,
    val accent3Spec: ColorSpec,
    val neutral1Spec: ColorSpec,
    val neutral2Spec: ColorSpec
) {
    companion object {
        private val VibrantSecondaryHueRotation = arrayOf(
            0 to 18,
            41 to 15,
            61 to 10,
            101 to 12,
            131 to 15,
            181 to 18,
            251 to 15,
            301 to 12,
            360 to 12
        )
        private val VibrantTertiaryHueRotation = arrayOf(
            0 to 35,
            41 to 30,
            61 to 20,
            101 to 25,
            131 to 30,
            181 to 35,
            251 to 30,
            301 to 25,
            360 to 25
        )
        private val ExpressiveSecondaryHueRotation = arrayOf(
            0 to 45,
            21 to 95,
            51 to 45,
            121 to 20,
            151 to 45,
            191 to 90,
            271 to 45,
            321 to 45,
            360 to 45
        )
        private val ExpressiveTertiaryHueRotation = arrayOf(
            0 to 120,
            21 to 120,
            51 to 120,
            121 to 45,
            151 to 20,
            191 to 15,
            271 to 20,
            321 to 120,
            360 to 120
        )
        val TonalSpot: PaletteStyle = PaletteStyle(
            accent1Spec = ColorSpec({ 36.0 }) { 0.0 },
            accent2Spec = ColorSpec({ 16.0 }) { 0.0 },
            accent3Spec = ColorSpec({ 24.0 }) { 60.0 },
            neutral1Spec = ColorSpec({ 6.0 }) { 0.0 },
            neutral2Spec = ColorSpec({ 8.0 }) { 0.0 }
        )
        val Spritz: PaletteStyle = PaletteStyle(
            accent1Spec = ColorSpec({ 12.0 }) { 0.0 },
            accent2Spec = ColorSpec({ 8.0 }) { 0.0 },
            accent3Spec = ColorSpec({ 16.0 }) { 30.0 },
            neutral1Spec = ColorSpec({ 2.0 }) { 0.0 },
            neutral2Spec = ColorSpec({ 2.0 }) { 0.0 }
        )
        val Vibrant: PaletteStyle = PaletteStyle(
            accent1Spec = ColorSpec({ 48.0 }) { 0.0 },
            accent2Spec = ColorSpec({ 24.0 }) { it.hueRotation(VibrantSecondaryHueRotation) },
            accent3Spec = ColorSpec({ 32.0 }) { it.hueRotation(VibrantTertiaryHueRotation) },
            neutral1Spec = ColorSpec({ 10.0 }) { 0.0 },
            neutral2Spec = ColorSpec({ 12.0 }) { 0.0 }
        )
        val Expressive: PaletteStyle = PaletteStyle(
            accent1Spec = ColorSpec({ 40.0 }) { 240.0 },
            accent2Spec = ColorSpec({ 24.0 }) { it.hueRotation(ExpressiveSecondaryHueRotation) },
            accent3Spec = ColorSpec({ 32.0 }) { it.hueRotation(ExpressiveTertiaryHueRotation) },
            neutral1Spec = ColorSpec({ 15.0 }) { 15.0 },
            neutral2Spec = ColorSpec({ 12.0 }) { 15.0 }
        )
        val Rainbow: PaletteStyle = PaletteStyle(
            accent1Spec = ColorSpec({ 48.0 }) { 0.0 },
            accent2Spec = ColorSpec({ 16.0 }) { 0.0 },
            accent3Spec = ColorSpec({ 24.0 }) { -60.0 },
            neutral1Spec = ColorSpec({ 0.0 }) { 0.0 },
            neutral2Spec = ColorSpec({ 0.0 }) { 0.0 }
        )
        val FruitSalad: PaletteStyle = PaletteStyle(
            accent1Spec = ColorSpec({ 48.0 }) { -50.0 },
            accent2Spec = ColorSpec({ 36.0 }) { -30.0 },
            accent3Spec = ColorSpec({ 36.0 }) { 0.0 },
            neutral1Spec = ColorSpec({ 10.0 }) { 0.0 },
            neutral2Spec = ColorSpec({ 16.0 }) { 0.0 }
        )
        val Content: PaletteStyle = PaletteStyle(
            accent1Spec = ColorSpec({ it * 1 }) { 0.0 },
            accent2Spec = ColorSpec({ it / 3 }) { 0.0 },
            accent3Spec = ColorSpec({ it * 2 / 3 }) { 60.0 },
            neutral1Spec = ColorSpec({ it / 12 }) { 0.0 },
            neutral2Spec = ColorSpec({ it / 6 }) { 0.0 }
        )
        val Monochrome: PaletteStyle = PaletteStyle(
            accent1Spec = ColorSpec({ 0.0 }) { 0.0 },
            accent2Spec = ColorSpec({ 0.0 }) { 0.0 },
            accent3Spec = ColorSpec({ 0.0 }) { 0.0 },
            neutral1Spec = ColorSpec({ 0.0 }) { 0.0 },
            neutral2Spec = ColorSpec({ 0.0 }) { 0.0 },
        )


        private fun Double.hueRotation(list: Array<Pair<Int, Int>>): Double {
            var i = 0
            val size = list.size - 2
            if (size >= 0) {
                while (true) {
                    val i2 = i + 1
                    val intValue = (list[i2]).first.toFloat()
                    when {
                        list[i].first <= this && this < intValue -> {
                            return (this + list[i].second.toDouble()).mod(360.0)
                        }

                        i == size -> break
                        else -> i = i2
                    }
                }
            }
            return this
        }
    }
}
