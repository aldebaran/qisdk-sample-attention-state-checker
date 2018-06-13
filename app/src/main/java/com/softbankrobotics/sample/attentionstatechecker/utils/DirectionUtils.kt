/*
 * Copyright (C) 2018 Softbank Robotics Europe
 * See COPYING for the license
 */

@file:JvmName("DirectionUtils")

package com.softbankrobotics.sample.attentionstatechecker.utils

import com.aldebaran.qi.sdk.QiContext
import com.softbankrobotics.sample.attentionstatechecker.model.data.Direction
import com.softbankrobotics.sample.attentionstatechecker.model.rx.observable.humanDataListObservable
import com.softbankrobotics.sample.attentionstatechecker.model.rx.operator.attentionState
import com.softbankrobotics.sample.attentionstatechecker.model.rx.operator.closest
import com.softbankrobotics.sample.attentionstatechecker.model.rx.operator.direction
import io.reactivex.Observable

/**
 * Provide an observable of the [Direction] corresponding to the direction of the closest human attention.
 *
 * @return An observable of the [Direction] corresponding to the direction of the closest human attention.
 */
fun directionObservable(qiContext: QiContext): Observable<Direction> {
    return humanDataListObservable(qiContext)
            .closest()
            .attentionState()
            .direction()
            .distinctUntilChanged()
            .filter { direction -> direction != Direction.UNKNOWN }
}

