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
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.expm1
import kotlin.math.hypot
import kotlin.math.ln1p
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sign
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * CAM16, a color appearance model. Colors are not just defined by their hex code, but rather, a hex
 * code and viewing conditions.
 *
 *
 * CAM16 instances also have coordinates in the CAM16-UCS space, called J*, a*, b*, or jstar,
 * astar, bstar in code. CAM16-UCS is included in the CAM16 specification, and should be used when
 * measuring distances between colors.
 *
 *
 * In traditional color spaces, a color can be identified solely by the observer's measurement of
 * the color. Color appearance models such as CAM16 also use information about the environment where
 * the color was observed, known as the viewing conditions.
 *
 *
 * For example, white under the traditional assumption of a midday sun white point is accurately
 * measured as a slightly chromatic blue by CAM16. (roughly, hue 203, chroma 3, lightness 100)
 */
class Cam16
/**
 * All of the CAM16 dimensions can be calculated from 3 of the dimensions, in the following
 * combinations: - {j or q} and {c, m, or s} and hue - jstar, astar, bstar Prefer using a static
 * method that constructs from 3 of those dimensions. This constructor is intended for those
 * methods to use to return all possible dimensions.
 *
 * @param hue for example, red, orange, yellow, green, etc.
 * @param chroma informally, colorfulness / color intensity. like saturation in HSL, except
 * perceptually accurate.
 * @param j lightness
 * @param q brightness; ratio of lightness to white point's lightness
 * @param m colorfulness
 * @param s saturation; ratio of chroma to white point's chroma
 * @param jstar CAM16-UCS J coordinate
 * @param astar CAM16-UCS a coordinate
 * @param bstar CAM16-UCS b coordinate
 */ private constructor(
    /** Hue in CAM16  */
    // CAM16 color dimensions, see getters for documentation.
    val hue: Double,
    /** Chroma in CAM16  */
    val chroma: Double,
    /** Lightness in CAM16  */
    val j: Double,
    /**
     * Brightness in CAM16.
     *
     *
     * Prefer lightness, brightness is an absolute quantity. For example, a sheet of white paper is
     * much brighter viewed in sunlight than in indoor light, but it is the lightest object under any
     * lighting.
     */
    val q: Double,
    /**
     * Colorfulness in CAM16.
     *
     *
     * Prefer chroma, colorfulness is an absolute quantity. For example, a yellow toy car is much
     * more colorful outside than inside, but it has the same chroma in both environments.
     */
    val m: Double,
    /**
     * Saturation in CAM16.
     *
     *
     * Colorfulness in proportion to brightness. Prefer chroma, saturation measures colorfulness
     * relative to the color's own brightness, where chroma is colorfulness relative to white.
     */
    val s: Double,
    /** Lightness coordinate in CAM16-UCS  */
    // Coordinates in UCS space. Used to determine color distance, like delta E equations in L*a*b*.
    val jstar: Double,
    /** a* coordinate in CAM16-UCS  */
    val astar: Double,
    /** b* coordinate in CAM16-UCS  */
    val bstar: Double
) {


    // Avoid allocations during conversion by pre-allocating an array.
    private val tempArray = doubleArrayOf(0.0, 0.0, 0.0)

    /**
     * CAM16 instances also have coordinates in the CAM16-UCS space, called J*, a*, b*, or jstar,
     * astar, bstar in code. CAM16-UCS is included in the CAM16 specification, and is used to measure
     * distances between colors.
     */
    fun distance(other: Cam16): Double {
        val dJ = jstar - other.jstar
        val dA = astar - other.astar
        val dB = bstar - other.bstar
        val dEPrime = sqrt(dJ * dJ + dA * dA + dB * dB)
        return 1.41 * dEPrime.pow(0.63)
    }

    /**
     * ARGB representation of the color. Assumes the color was viewed in default viewing conditions,
     * which are near-identical to the default viewing conditions for sRGB.
     */
    fun toInt(): Int {
        return viewed(ViewingConditions.Companion.DEFAULT)
    }

    /**
     * ARGB representation of the color, in defined viewing conditions.
     *
     * @param viewingConditions Information about the environment where the color will be viewed.
     * @return ARGB representation of color
     */
    fun viewed(viewingConditions: ViewingConditions): Int {
        val xyz = xyzInViewingConditions(viewingConditions, tempArray)
        return ColorUtils.argbFromXyz(xyz[0], xyz[1], xyz[2])
    }

    fun xyzInViewingConditions(
        viewingConditions: ViewingConditions,
        returnArray: DoubleArray?
    ): DoubleArray {
        val alpha = if (chroma == 0.0 || j == 0.0) 0.0 else chroma / sqrt(
            j / 100.0
        )
        val t = (alpha / (1.64 - 0.29.pow(viewingConditions.n)).pow(0.73)).pow(1.0 / 0.9)
        val hRad = hue.toRadians()

        val eHue = 0.25 * (cos(hRad + 2.0) + 3.8)
        val ac = (viewingConditions.aw
                * (j / 100.0).pow(1.0 / viewingConditions.c / viewingConditions.z))
        val p1 = eHue * (50000.0 / 13.0) * viewingConditions.nc * viewingConditions.ncb
        val p2 = ac / viewingConditions.nbb
        val hSin = sin(hRad)
        val hCos = cos(hRad)
        val gamma = 23.0 * (p2 + 0.305) * t / (23.0 * p1 + 11.0 * t * hCos + 108.0 * t * hSin)
        val a = gamma * hCos
        val b = gamma * hSin
        val rA = (460.0 * p2 + 451.0 * a + 288.0 * b) / 1403.0
        val gA = (460.0 * p2 - 891.0 * a - 261.0 * b) / 1403.0
        val bA = (460.0 * p2 - 220.0 * a - 6300.0 * b) / 1403.0
        val rCBase = max(0.0, 27.13 * abs(rA) / (400.0 - abs(rA)))
        val rC = sign(rA) * (100.0 / viewingConditions.fl) * rCBase.pow(1.0 / 0.42)
        val gCBase = max(0.0, 27.13 * abs(gA) / (400.0 - abs(gA)))
        val gC = sign(gA) * (100.0 / viewingConditions.fl) * gCBase.pow(1.0 / 0.42)
        val bCBase = max(0.0, 27.13 * abs(bA) / (400.0 - abs(bA)))
        val bC = sign(bA) * (100.0 / viewingConditions.fl) * bCBase.pow(1.0 / 0.42)
        val rF = rC / viewingConditions.rgbD[0]
        val gF = gC / viewingConditions.rgbD[1]
        val bF = bC / viewingConditions.rgbD[2]
        val matrix = CAM16RGB_TO_XYZ
        val x = rF * matrix[0][0] + gF * matrix[0][1] + bF * matrix[0][2]
        val y = rF * matrix[1][0] + gF * matrix[1][1] + bF * matrix[1][2]
        val z = rF * matrix[2][0] + gF * matrix[2][1] + bF * matrix[2][2]
        return if (returnArray != null) {
            returnArray[0] = x
            returnArray[1] = y
            returnArray[2] = z
            returnArray
        } else {
            doubleArrayOf(x, y, z)
        }
    }

    companion object {
        // Transforms XYZ color space coordinates to 'cone'/'RGB' responses in CAM16.
        val XYZ_TO_CAM16RGB = arrayOf(
            doubleArrayOf(0.401288, 0.650173, -0.051461),
            doubleArrayOf(-0.250268, 1.204414, 0.045854),
            doubleArrayOf(-0.002079, 0.048952, 0.953127)
        )

        // Transforms 'cone'/'RGB' responses in CAM16 to XYZ color space coordinates.
        val CAM16RGB_TO_XYZ = arrayOf(
            doubleArrayOf(1.8620678, -1.0112547, 0.14918678),
            doubleArrayOf(0.38752654, 0.62144744, -0.00897398),
            doubleArrayOf(-0.01584150, -0.03412294, 1.0499644)
        )

        /**
         * Create a CAM16 color from a color, assuming the color was viewed in default viewing conditions.
         *
         * @param argb ARGB representation of a color.
         */
        fun fromInt(argb: Int): Cam16 {
            return fromIntInViewingConditions(argb, ViewingConditions.Companion.DEFAULT)
        }

        /**
         * Create a CAM16 color from a color in defined viewing conditions.
         *
         * @param argb ARGB representation of a color.
         * @param viewingConditions Information about the environment where the color was observed.
         */
        // The RGB => XYZ conversion matrix elements are derived scientific constants. While the values
        // may differ at runtime due to floating point imprecision, keeping the values the same, and
        // accurate, across implementations takes precedence.
        fun fromIntInViewingConditions(argb: Int, viewingConditions: ViewingConditions): Cam16 {
            // Transform ARGB int to XYZ
            val red = argb and 0x00ff0000 shr 16
            val green = argb and 0x0000ff00 shr 8
            val blue = argb and 0x000000ff
            val redL = ColorUtils.linearized(red)
            val greenL = ColorUtils.linearized(green)
            val blueL = ColorUtils.linearized(blue)
            val x = 0.41233895 * redL + 0.35762064 * greenL + 0.18051042 * blueL
            val y = 0.2126 * redL + 0.7152 * greenL + 0.0722 * blueL
            val z = 0.01932141 * redL + 0.11916382 * greenL + 0.95034478 * blueL
            return fromXyzInViewingConditions(x, y, z, viewingConditions)
        }

        fun fromXyzInViewingConditions(
            x: Double, y: Double, z: Double, viewingConditions: ViewingConditions
        ): Cam16 {
            // Transform XYZ to 'cone'/'rgb' responses
            val matrix = XYZ_TO_CAM16RGB
            val rT = x * matrix[0][0] + y * matrix[0][1] + z * matrix[0][2]
            val gT = x * matrix[1][0] + y * matrix[1][1] + z * matrix[1][2]
            val bT = x * matrix[2][0] + y * matrix[2][1] + z * matrix[2][2]

            // Discount illuminant
            val rD = viewingConditions.rgbD[0] * rT
            val gD = viewingConditions.rgbD[1] * gT
            val bD = viewingConditions.rgbD[2] * bT

            // Chromatic adaptation
            val rAF = (viewingConditions.fl * abs(rD) / 100.0).pow(0.42)
            val gAF = (viewingConditions.fl * abs(gD) / 100.0).pow(0.42)
            val bAF = (viewingConditions.fl * abs(bD) / 100.0).pow(0.42)
            val rA = sign(rD) * 400.0 * rAF / (rAF + 27.13)
            val gA = sign(gD) * 400.0 * gAF / (gAF + 27.13)
            val bA = sign(bD) * 400.0 * bAF / (bAF + 27.13)

            // redness-greenness
            val a = (11.0 * rA + -12.0 * gA + bA) / 11.0
            // yellowness-blueness
            val b = (rA + gA - 2.0 * bA) / 9.0

            // auxiliary components
            val u = (20.0 * rA + 20.0 * gA + 21.0 * bA) / 20.0
            val p2 = (40.0 * rA + 20.0 * gA + bA) / 20.0

            // hue
            val atan2 = atan2(b, a)
            val atanDegrees = atan2.toDegrees()
            val hue =
                if (atanDegrees < 0) atanDegrees + 360.0 else if (atanDegrees >= 360) atanDegrees - 360.0 else atanDegrees
            val hueRadians = hue.toRadians()

            // achromatic response to color
            val ac = p2 * viewingConditions.nbb

            // CAM16 lightness and brightness
            val j = (100.0
                    * (ac / viewingConditions.aw).pow(viewingConditions.c * viewingConditions.z))
            val q = ((4.0
                    / viewingConditions.c) * sqrt(j / 100.0)
                    * (viewingConditions.aw + 4.0)
                    * viewingConditions.flRoot)

            // CAM16 chroma, colorfulness, and saturation.
            val huePrime = if (hue < 20.14) hue + 360 else hue
            val eHue = 0.25 * (cos(huePrime.toRadians() + 2.0) + 3.8)
            val p1 = 50000.0 / 13.0 * eHue * viewingConditions.nc * viewingConditions.ncb
            val t = p1 * hypot(a, b) / (u + 0.305)
            val alpha =
                (1.64 - 0.29.pow(viewingConditions.n)).pow(0.73) * t.pow(0.9)
            // CAM16 chroma, colorfulness, saturation
            val c = alpha * sqrt(j / 100.0)
            val m = c * viewingConditions.flRoot
            val s = 50.0 * sqrt(alpha * viewingConditions.c / (viewingConditions.aw + 4.0))

            // CAM16-UCS components
            val jstar = (1.0 + 100.0 * 0.007) * j / (1.0 + 0.007 * j)
            val mstar = 1.0 / 0.0228 * ln1p(0.0228 * m)
            val astar = mstar * cos(hueRadians)
            val bstar = mstar * sin(hueRadians)
            return Cam16(hue, c, j, q, m, s, jstar, astar, bstar)
        }

        /**
         * @param j CAM16 lightness
         * @param c CAM16 chroma
         * @param h CAM16 hue
         */
        fun fromJch(j: Double, c: Double, h: Double): Cam16 {
            return fromJchInViewingConditions(j, c, h, ViewingConditions.Companion.DEFAULT)
        }

        /**
         * @param j CAM16 lightness
         * @param c CAM16 chroma
         * @param h CAM16 hue
         * @param viewingConditions Information about the environment where the color was observed.
         */
        private fun fromJchInViewingConditions(
            j: Double, c: Double, h: Double, viewingConditions: ViewingConditions
        ): Cam16 {
            val q = ((4.0
                    / viewingConditions.c) * sqrt(j / 100.0)
                    * (viewingConditions.aw + 4.0)
                    * viewingConditions.flRoot)
            val m = c * viewingConditions.flRoot
            val alpha = c / sqrt(j / 100.0)
            val s = 50.0 * sqrt(alpha * viewingConditions.c / (viewingConditions.aw + 4.0))
            val hueRadians = h.toRadians()
            val jstar = (1.0 + 100.0 * 0.007) * j / (1.0 + 0.007 * j)
            val mstar = 1.0 / 0.0228 * ln1p(0.0228 * m)
            val astar = mstar * cos(hueRadians)
            val bstar = mstar * sin(hueRadians)
            return Cam16(h, c, j, q, m, s, jstar, astar, bstar)
        }

        /**
         * Create a CAM16 color from CAM16-UCS coordinates.
         *
         * @param jstar CAM16-UCS lightness.
         * @param astar CAM16-UCS a dimension. Like a* in L*a*b*, it is a Cartesian coordinate on the Y
         * axis.
         * @param bstar CAM16-UCS b dimension. Like a* in L*a*b*, it is a Cartesian coordinate on the X
         * axis.
         */
        fun fromUcs(jstar: Double, astar: Double, bstar: Double): Cam16 {
            return fromUcsInViewingConditions(
                jstar,
                astar,
                bstar,
                ViewingConditions.Companion.DEFAULT
            )
        }

        /**
         * Create a CAM16 color from CAM16-UCS coordinates in defined viewing conditions.
         *
         * @param jstar CAM16-UCS lightness.
         * @param astar CAM16-UCS a dimension. Like a* in L*a*b*, it is a Cartesian coordinate on the Y
         * axis.
         * @param bstar CAM16-UCS b dimension. Like a* in L*a*b*, it is a Cartesian coordinate on the X
         * axis.
         * @param viewingConditions Information about the environment where the color was observed.
         */
        fun fromUcsInViewingConditions(
            jstar: Double, astar: Double, bstar: Double, viewingConditions: ViewingConditions
        ): Cam16 {
            val m = hypot(astar, bstar)
            val m2 = expm1(m * 0.0228) / 0.0228
            val c = m2 / viewingConditions.flRoot
            var h = atan2(bstar, astar) * (180.0 / kotlin.math.PI)
            if (h < 0.0) {
                h += 360.0
            }
            val j = jstar / (1.0 - (jstar - 100.0) * 0.007)
            return fromJchInViewingConditions(j, c, h, viewingConditions)
        }

        private inline fun Double.toRadians() = this * kotlin.math.PI / 180.0

        private inline fun Double.toDegrees() = this * 180.0 / kotlin.math.PI
    }
}