/*
 * Copyright 2021 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.material.hct

import io.material.utils.ColorUtils
import io.material.utils.MathUtils
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * In traditional color spaces, a color can be identified solely by the observer's measurement of
 * the color. Color appearance models such as CAM16 also use information about the environment where
 * the color was observed, known as the viewing conditions.
 *
 *
 * For example, white under the traditional assumption of a midday sun white point is accurately
 * measured as a slightly chromatic blue by CAM16. (roughly, hue 203, chroma 3, lightness 100)
 *
 *
 * This class caches intermediate values of the CAM16 conversion process that depend only on
 * viewing conditions, enabling speed ups.
 */
class ViewingConditions
/**
 * Parameters are intermediate values of the CAM16 conversion process. Their names are shorthand
 * for technical color science terminology, this class would not benefit from documenting them
 * individually. A brief overview is available in the CAM16 specification, and a complete overview
 * requires a color science textbook, such as Fairchild's Color Appearance Models.
 */ private constructor(
    val n: Double,
    val aw: Double,
    val nbb: Double,
    val ncb: Double,
    val c: Double,
    val nc: Double,
    val rgbD: DoubleArray,
    val fl: Double,
    val flRoot: Double,
    val z: Double
) {

    companion object {
        /** sRGB-like viewing conditions.  */
        val DEFAULT = defaultWithBackgroundLstar(50.0)

        /**
         * Create ViewingConditions from a simple, physically relevant, set of parameters.
         *
         * @param whitePoint White point, measured in the XYZ color space. default = D65, or sunny day
         * afternoon
         * @param adaptingLuminance The luminance of the adapting field. Informally, how bright it is in
         * the room where the color is viewed. Can be calculated from lux by multiplying lux by
         * 0.0586. default = 11.72, or 200 lux.
         * @param backgroundLstar The lightness of the area surrounding the color. measured by L* in
         * L*a*b*. default = 50.0
         * @param surround A general description of the lighting surrounding the color. 0 is pitch dark,
         * like watching a movie in a theater. 1.0 is a dimly light room, like watching TV at home at
         * night. 2.0 means there is no difference between the lighting on the color and around it.
         * default = 2.0
         * @param discountingIlluminant Whether the eye accounts for the tint of the ambient lighting,
         * such as knowing an apple is still red in green light. default = false, the eye does not
         * perform this process on self-luminous objects like displays.
         */
        fun make(
            whitePoint: DoubleArray?,
            adaptingLuminance: Double,
            backgroundLstar: Double,
            surround: Double,
            discountingIlluminant: Boolean
        ): ViewingConditions {
            // A background of pure black is non-physical and leads to infinities that represent the idea
            // that any color viewed in pure black can't be seen.
            var backgroundLstar = backgroundLstar
            backgroundLstar = max(0.1, backgroundLstar)
            // Transform white point XYZ to 'cone'/'rgb' responses
            val matrix: Array<DoubleArray> = Cam16.Companion.XYZ_TO_CAM16RGB
            val rW =
                whitePoint!![0] * matrix[0][0] + whitePoint[1] * matrix[0][1] + whitePoint[2] * matrix[0][2]
            val gW =
                whitePoint[0] * matrix[1][0] + whitePoint[1] * matrix[1][1] + whitePoint[2] * matrix[1][2]
            val bW =
                whitePoint[0] * matrix[2][0] + whitePoint[1] * matrix[2][1] + whitePoint[2] * matrix[2][2]
            val f = 0.8 + surround / 10.0
            val c = if (f >= 0.9) MathUtils.lerp(
                0.59,
                0.69,
                (f - 0.9) * 10.0
            ) else MathUtils.lerp(0.525, 0.59, (f - 0.8) * 10.0)
            var d =
                if (discountingIlluminant) 1.0 else f * (1.0 - 1.0 / 3.6 * exp((-adaptingLuminance - 42.0) / 92.0))
            d = MathUtils.clampDouble(0.0, 1.0, d)
            val rgbD = doubleArrayOf(
                d * (100.0 / rW) + 1.0 - d, d * (100.0 / gW) + 1.0 - d, d * (100.0 / bW) + 1.0 - d
            )
            val k = 1.0 / (5.0 * adaptingLuminance + 1.0)
            val k4 = k * k * k * k
            val k4F = 1.0 - k4
            val fl = k4 * adaptingLuminance + 0.1 * k4F * k4F * kotlin.math.cbrt(5.0 * adaptingLuminance)
            val n = ColorUtils.yFromLstar(backgroundLstar) / whitePoint[1]
            val z = 1.48 + sqrt(n)
            val nbb = 0.725 / n.pow(0.2)
            val rgbAFactors = doubleArrayOf(
                (fl * rgbD[0] * rW / 100.0).pow(0.42),
                (fl * rgbD[1] * gW / 100.0).pow(0.42),
                (fl * rgbD[2] * bW / 100.0).pow(0.42)
            )
            val rgbA = doubleArrayOf(
                400.0 * rgbAFactors[0] / (rgbAFactors[0] + 27.13),
                400.0 * rgbAFactors[1] / (rgbAFactors[1] + 27.13),
                400.0 * rgbAFactors[2] / (rgbAFactors[2] + 27.13)
            )
            val aw = (2.0 * rgbA[0] + rgbA[1] + 0.05 * rgbA[2]) * nbb
            return ViewingConditions(
                n,
                aw,
                nbb,
                nbb,
                c,
                f,
                rgbD,
                fl,
                fl.pow(0.25),
                z
            )
        }

        /**
         * Create sRGB-like viewing conditions with a custom background lstar.
         *
         *
         * Default viewing conditions have a lstar of 50, midgray.
         */
        fun defaultWithBackgroundLstar(lstar: Double): ViewingConditions {
            return make(
                ColorUtils.whitePointD65(),
                200.0 / kotlin.math.PI * ColorUtils.yFromLstar(50.0) / 100f,
                lstar,
                2.0,
                false
            )
        }
    }
}