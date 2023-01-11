@file:Suppress("unused")

package com.kyant.monet

import com.kyant.monet.CieXyz.Companion.asCieXyz
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sign
import kotlin.math.sin
import kotlin.math.sqrt

data class CieXyz(
    val x: Double,
    val y: Double,
    val z: Double
) {
    internal fun asVec3(): Vec3 = Vec3(x, y, z)

    companion object {
        internal fun Vec3.asCieXyz(): CieXyz = CieXyz(a, b, c)
    }
}

data class Srgb(
    val r: Double,
    val g: Double,
    val b: Double
) {
    private fun asVec3(): Vec3 = Vec3(r, g, b)

    internal fun isInGamut(): Boolean = r in 0.0..1.0 && g in 0.0..1.0 && b in 0.0..1.0

    internal fun clamp(): Srgb = Srgb(r.coerceIn(0.0, 1.0), g.coerceIn(0.0, 1.0), b.coerceIn(0.0, 1.0))

    fun toHex(): String {
        val r = (r * 255).roundToInt()
        val g = (g * 255).roundToInt()
        val b = (b * 255).roundToInt()
        return "#${r.toString(16).padStart(2, '0')}${g.toString(16).padStart(2, '0')}${
        b.toString(
            16
        ).padStart(2, '0')
        }"
            .uppercase()
    }

    fun toCieXyz(): CieXyz {
        return (
            SrgbToCieXyzMatrix * asVec3().map {
                if (it > 11.0 / 280) ((it + 0.055) / 1.055).pow(2.4)
                else it / 12.92321018078786
            }
            ).asCieXyz()
    }

    companion object {
        private val SrgbToCieXyzMatrix = Mat3x3(
            0.41245744558236513, 0.35757586524551643, 0.18043724782640036,
            0.21267337037840703, 0.7151517304910329, 0.07217489913056015,
            0.019333942761673367, 0.11919195508183882, 0.95030283855237520
        ) * 100.0

        private val CieXyzToSrgbMatrix = Mat3x3(
            3.240446254647756, -1.5371347618200895, -0.4985301930227317,
            -0.9692666062446783, 1.8760119597883673, 0.04155604221443001,
            0.055643503564352596, -0.20402617973595952, 1.0572265677226993
        ) / 100.0

        private fun Vec3.asSrgb(): Srgb = Srgb(a, b, c)

        fun String.toSrgb(): Srgb {
            val r = substring(1, 3).toInt(16)
            val g = substring(3, 5).toInt(16)
            val b = substring(5, 7).toInt(16)
            return Srgb(r / 255.0, g / 255.0, b / 255.0)
        }

        fun CieXyz.toSrgb(): Srgb {
            return (CieXyzToSrgbMatrix * asVec3()).map {
                if (it > 0.0030399346397784300) 1.055 * it.pow(1 / 2.4) - 0.055
                else 12.92321018078786 * it
            }.asSrgb()
        }
    }
}

data class CieLab(
    val l: Double,
    val a: Double,
    val b: Double
) {
    internal fun asVec3(): Vec3 = Vec3(l, a, b)

    fun toCieXyz(): CieXyz {
        val lp = (l + 16) / 116
        return (
            D65.asVec3().diag() * Vec3(lp + (a / 500), lp, lp - (b / 200)).map {
                if (it > 6.0 / 29) it.pow(3)
                else 108.0 / 841 * (it - 4.0 / 29)
            }
            ).asCieXyz()
    }

    companion object {
        private val D65 = CieXyz(95.04705586542819, 100.0, 108.88287363958874)

        private fun Vec3.asCieLab(): CieLab = CieLab(a, b, c)

        private fun f(x: Double): Double {
            return if (x > 216.0 / 24389) x.pow(1 / 3.0)
            else x / (108.0 / 841) + 4.0 / 29
        }

        fun CieXyz.toCieLab(): CieLab {
            return Vec3(
                116 * f(y / D65.y) - 16,
                500 * (f(x / D65.x) - f(y / D65.y)),
                200 * (f(y / D65.y) - f(z / D65.z))
            ).asCieLab()
        }
    }
}

data class Cam16(
    val j: Double,
    val c: Double,
    val h: Double
) {
    internal fun asVec3(): Vec3 = Vec3(j, c, h)

    fun toCieXyz(): CieXyz {
        val hRad = h.toRadians()
        val g = c / e(hRad) / 1505
        return (
            M_16_inv * D.hadamardInverse().diag() * (
                R_inv * r * Vec3(
                    (j / 100).pow(1 / d),
                    g * cos(hRad),
                    g * sin(hRad)
                )
                ).map {
                it.sign / F * (27.13 * abs(it) / (400 - abs(it))).pow(1 / 0.42)
            }
            ).asCieXyz()
    }

    companion object {
        private const val F = 0.0038848145378003528

        internal const val r = 29.4821830213423

        private const val d = 1.3173270022537198

        private val M_16 = Mat3x3(
            0.401288, 0.650173, -0.051461,
            -0.250268, 1.204414, 0.045854,
            -0.002079, 0.048952, 0.953127
        )

        private val M_16_inv = Mat3x3(
            1.8620678550872327, -1.0112546305316843, 0.14918677544445172,
            0.38752654323613717, 0.6214474419314754, -0.008973985167612518,
            -0.015841498849333856, -0.03412293802851556, 1.0499644368778493
        )

        private val D = Vec3(
            1.0211774459482703,
            0.98630789117685210,
            0.9339613740630106
        )

        private val R = Mat3x3(
            2.0, 1.0, 1.0 / 20,
            1.0, -12.0 / 11, 1.0 / 11,
            1.0 / 9, 1.0 / 9, -2.0 / 9
        )

        private val R_inv = Mat3x3(
            20.0 / 61, 451.0 / 1403, 288.0 / 1403,
            20.0 / 61, -891.0 / 1403, -261.0 / 1403,
            20.0 / 61, -220.0 / 1403, -6300.0 / 1403
        )

        private fun e(h: Double) = 1.0 -
            0.0582 * cos(h) -
            0.0258 * cos(2.0 * h) -
            0.1347 * cos(3.0 * h) +
            0.0289 * cos(4.0 * h) -
            0.1475 * sin(h) -
            0.0308 * sin(2.0 * h) +
            0.0385 * sin(3.0 * h) +
            0.0096 * sin(4.0 * h)

        private fun Vec3.asCam16(): Cam16 = Cam16(a, b, c)

        fun CieXyz.toCam16(): Cam16 {
            val (A, ac, bc) = R * (D.diag() * M_16 * asVec3()).map {
                400 * it.sign * (1 - 27.13 / ((F * it).pow(0.42) + 27.13))
            }
            val hRad = atan2(bc, ac).mod(2 * PI)
            return Vec3(
                100 * (A / r).pow(d),
                1505 / r * e(hRad) * sqrt(ac.pow(2) + bc.pow(2)),
                hRad.toDegrees()
            ).asCam16()
        }
    }
}

data class Cam16Ucs(
    val j: Double,
    val a: Double,
    val b: Double
) {
    internal fun asVec3(): Vec3 = Vec3(j, a, b)

    internal infix fun deltaE(other: Cam16Ucs): Double {
        return 1.41 * sqrt((j - other.j).pow(2) + (a - other.a).pow(2) + (b - other.b).pow(2)).pow(
            0.63
        )
    }

    companion object {
        internal fun Vec3.asCam16Ucs(): Cam16Ucs = Cam16Ucs(a, b, c)

        fun Cam16.toCam16Ucs(): Cam16Ucs {
            val m = ln(1 + 0.0228 * c * Cam16.r / 35) / 0.0228
            val hRad = h.toRadians()

            return Cam16Ucs(
                1.7 * j / (1 + 0.007 * j),
                m * cos(hRad),
                m * sin(hRad)
            )
        }
    }
}
