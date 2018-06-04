/*
 * Copyright (C) 2018 Softbank Robotics Europe
 * See COPYING for the license
 */

package com.softbankrobotics.sample.attentionstatechecker.model.rx.observable;

import android.support.annotation.NonNull;

import com.aldebaran.qi.sdk.QiContext;
import com.softbankrobotics.sample.attentionstatechecker.model.data.Direction;
import com.softbankrobotics.sample.attentionstatechecker.model.rx.operator.MyOperators;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.schedulers.Schedulers;

/**
 * Observable providing a {@link Direction} corresponding to the direction of the closest human attention.
 */
public class DirectionObservable extends Observable<Direction> {

    @NonNull
    private final QiContext qiContext;

    public DirectionObservable(@NonNull QiContext qiContext) {
        this.qiContext = qiContext;
    }

    @Override
    protected void subscribeActual(Observer<? super Direction> observer) {
        new MyHumanDataListObservable(qiContext)
                .subscribeOn(Schedulers.io())
                .compose(MyOperators::closest)
                .compose(MyOperators::attentionState)
                .distinctUntilChanged()
                .debounce(2, TimeUnit.SECONDS)
                .compose(MyOperators::direction)
                .distinctUntilChanged()
                .filter(direction -> !direction.equals(Direction.UNKNOWN))
                .observeOn(Schedulers.io())
                .subscribe(observer);
    }
}
