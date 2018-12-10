/*
 * Copyright (C) 2018 SoftBank Robotics Europe
 * See COPYING for the license
 */
@file:JvmName("FrameUtils")

package com.softbankrobotics.sample.attentionstatechecker.utils

import com.aldebaran.qi.sdk.`object`.actuation.Frame
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

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

/**
 * Provide an observable distance between the two specified frames, in meters.
 *
 * @return An observable of the distance between the two specified frames, in meters.
 */
fun Frame.distanceObservableFrom(frame: Frame): Observable<Double> {
    return Observable.interval(1, TimeUnit.SECONDS)
            .observeOn(Schedulers.io())
            .map { distanceFrom(frame) }
}
