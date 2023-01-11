package com.kyant.monet

import kotlin.math.PI

internal fun Double.toRadians(): Double = this * PI / 180
internal fun Double.toDegrees(): Double = this * 180 / PI

internal class Vec3(
    val a: Double,
    val b: Double,
    val c: Double
) {
    inline fun map(transform: (Double) -> Double): Vec3 {
        val mapped = doubleArrayOf(a, b, c).map(transform)
        return Vec3(mapped[0], mapped[1], mapped[2])
    }

    fun diag(): Mat3x3 {
        return Mat3x3(
            a, 0.0, 0.0,
            0.0, b, 0.0,
            0.0, 0.0, c
        )
    }

    fun hadamardInverse(): Vec3 {
        return Vec3(1 / a, 1 / b, 1 / c)
    }

    operator fun component1(): Double = a
    operator fun component2(): Double = b
    operator fun component3(): Double = c
}

internal class Mat3x3(
    private val a: Double,
    private val b: Double,
    private val c: Double,
    private val d: Double,
    private val e: Double,
    private val f: Double,
    private val g: Double,
    private val h: Double,
    private val i: Double
) {
    operator fun times(x: Double): Mat3x3 {
        return Mat3x3(
            a * x, b * x, c * x,
            d * x, e * x, f * x,
            g * x, h * x, i * x
        )
    }

    operator fun div(x: Double): Mat3x3 {
        return Mat3x3(
            a / x, b / x, c / x,
            d / x, e / x, f / x,
            g / x, h / x, i / x
        )
    }

    operator fun times(vec: Vec3): Vec3 {
        return Vec3(
            a * vec.a + b * vec.b + c * vec.c,
            d * vec.a + e * vec.b + f * vec.c,
            g * vec.a + h * vec.b + i * vec.c
        )
    }

    operator fun times(mat: Mat3x3): Mat3x3 {
        return Mat3x3(
            a * mat.a + b * mat.d + c * mat.g,
            a * mat.b + b * mat.e + c * mat.h,
            a * mat.c + b * mat.f + c * mat.i,
            d * mat.a + e * mat.d + f * mat.g,
            d * mat.b + e * mat.e + f * mat.h,
            d * mat.c + e * mat.f + f * mat.i,
            g * mat.a + h * mat.d + i * mat.g,
            g * mat.b + h * mat.e + i * mat.h,
            g * mat.c + h * mat.f + i * mat.i
        )
    }
}
