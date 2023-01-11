@file:Suppress("unused")

package com.kyant.monet

import com.kyant.monet.Cam16.Companion.toCam16
import com.kyant.monet.Cam16Ucs.Companion.toCam16Ucs
import com.kyant.monet.CieLab.Companion.toCieLab
import com.kyant.monet.Srgb.Companion.toSrgb
import kotlin.math.abs
import kotlin.math.roundToInt

data class Hct(
    val h: Double,
    val c: Double,
    val t: Double
) {
    fun toSrgb(): Srgb {
        if (c < 1.0 || t.roundToInt() <= 0.0 || t.roundToInt() >= 100.0) {
            return neutralColor()
        }
        var high = c
        var mid = high
        var low = 0.0
        var isFirstLoop = true
        var answer: Cam16? = null
        while (high - low >= 0.4) {
            val possibleAnswer = copy(c = mid).findCam16ByTone()
            if (isFirstLoop) {
                if (possibleAnswer != null) {
                    return possibleAnswer.toCieXyz().toSrgb()
                } else {
                    isFirstLoop = false
                    mid = low + (high - low) / 2
                    continue
                }
            }
            if (possibleAnswer == null) {
                high = mid
            } else {
                answer = possibleAnswer
                low = mid
            }
            mid = low + (high - low) / 2
        }
        return answer?.toCieXyz()?.toSrgb() ?: neutralColor()
    }

    private fun neutralColor(): Srgb {
        return when {
            t < 1.0 -> Srgb(0.0, 0.0, 0.0)
            t > 99.0 -> Srgb(1.0, 1.0, 1.0)
            else -> Cam16(CieLab(t, 0.0, 0.0).toCieXyz().toCam16().j, c, h).toCieXyz().toSrgb().clamp()
        }
    }

    private fun findCam16ByTone(): Cam16? {
        var low = 0.0
        var high = 100.0
        var mid: Double
        var bestdL = 1000.0
        var bestdE = 1000.0
        var bestCam: Cam16? = null

        while (high - low > 0.01) {
            mid = low + (high - low) / 2
            val camBeforeClip = Cam16(mid, c, h)
            val clipped = camBeforeClip.toCieXyz().toSrgb().clamp()
            val clippedTone = clipped.toCieXyz().toCieLab().l
            val dT = abs(t - clippedTone)
            if (dT < 0.2) {
                val camClipped = clipped.toCieXyz().toCam16()
                val dE = camClipped.toCam16Ucs() deltaE camClipped.copy(h = h).toCam16Ucs()
                if (dE <= 1.0) {
                    bestdL = dT
                    bestdE = dE
                    bestCam = camClipped
                }
            }
            if (bestdL == 0.0 && bestdE == 0.0) {
                break
            }
            if (clippedTone < t) {
                low = mid
            } else {
                high = mid
            }
        }
        return bestCam
    }

    companion object {
        fun Srgb.toHct(): Hct {
            val xyz = toCieXyz()
            val lab = xyz.toCieLab()
            val cam16 = xyz.toCam16()
            return Hct(cam16.h, cam16.c, lab.l)
        }
    }
}
