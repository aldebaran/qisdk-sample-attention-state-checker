/*
 * Copyright (C) 2018 Softbank Robotics Europe
 * See COPYING for the license
 */

package com.softbankrobotics.sample.attentionstatechecker.model.rx.observable

import com.aldebaran.qi.sdk.QiContext
import com.softbankrobotics.sample.attentionstatechecker.model.data.Direction
import com.softbankrobotics.sample.attentionstatechecker.model.rx.operator.*

import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.schedulers.Schedulers

/**
 * Observable providing a [Direction] corresponding to the direction of the closest human attention.
 */
class DirectionObservable(private val qiContext: QiContext) : Observable<Direction>() {

    override fun subscribeActual(observer: Observer<in Direction>) {
        MyHumanDataListObservable(qiContext)
                .subscribeOn(Schedulers.io())
                .compose { closest(it) }
                .compose { attentionState(it) }
                .compose { direction(it) }
                .distinctUntilChanged()
                .filter { direction -> direction != Direction.UNKNOWN }
                .observeOn(Schedulers.io())
                .subscribe(observer)
    }
}
