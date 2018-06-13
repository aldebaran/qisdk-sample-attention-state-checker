@file:JvmName("FrameUtils")

package com.softbankrobotics.sample.attentionstatechecker.utils

import com.aldebaran.qi.sdk.`object`.actuation.Frame

/**
 * Provide the distance between the two specified frames, in meters.
 *
 * @return The distance between the two specified frames, in meters.
 */
fun Frame.distanceFrom(frame: Frame): Double {
    val translation = computeTransform(frame).transform.translation
    val x = translation.x
    val y = translation.y
    return Math.sqrt(x * x + y * y)
}
