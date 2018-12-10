/*
 * Copyright (C) 2018 SoftBank Robotics Europe
 * See COPYING for the license
 */
@file:JvmName("FutureCancellations")

package com.softbankrobotics.sample.attentionstatechecker.utils

import com.aldebaran.qi.Future

/**
 * Cancel the provided [Future].
 * @return A [Future] that can only end in a success state, when the provided [Future] is cancelled.
 * If the [Future] to cancel is already done, this method returns immediately.
 */
fun Future<*>?.cancellation(): Future<Void> {
    if (this == null) {
        return Future.of(null)
    }

    requestCancellation()
    return thenApply { _ -> null }
}
