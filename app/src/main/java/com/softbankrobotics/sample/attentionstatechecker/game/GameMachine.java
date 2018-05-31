/*
 * Copyright (C) 2018 Softbank Robotics Europe
 * See COPYING for the license
 */

package com.softbankrobotics.sample.attentionstatechecker.game;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;
import com.softbankrobotics.sample.attentionstatechecker.model.data.Direction;
import com.softbankrobotics.sample.attentionstatechecker.model.rx.observable.MyHumanDataListObservable;
import com.softbankrobotics.sample.attentionstatechecker.model.rx.operator.MyOperators;

import java.util.concurrent.TimeUnit;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * A game machine.
 */
final class GameMachine implements RobotLifecycleCallbacks {

    @Nullable
    private Disposable disposable;

    @Override
    public void onRobotFocusGained(QiContext qiContext) {
        disposable = new MyHumanDataListObservable(qiContext)
                .subscribeOn(Schedulers.io())
                .compose(MyOperators::closest)
                .compose(MyOperators::attentionState)
                .distinctUntilChanged()
                .debounce(2, TimeUnit.SECONDS)
                .compose(MyOperators::direction)
                .distinctUntilChanged()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleDirection);
    }

    @Override
    public void onRobotFocusLost() {
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
    }

    @Override
    public void onRobotFocusRefused(String reason) {
        // Not used.
    }

    private void handleDirection(@NonNull Direction direction) {
        Log.i("Test", "Direction: " + direction);
    }
}
