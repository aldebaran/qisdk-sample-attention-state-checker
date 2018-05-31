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
import com.softbankrobotics.sample.attentionstatechecker.model.rx.observable.DirectionObservable;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

/**
 * A game machine.
 */
final class GameMachine implements RobotLifecycleCallbacks {

    @Nullable
    private Disposable disposable;

    @Override
    public void onRobotFocusGained(QiContext qiContext) {
        disposable = new DirectionObservable(qiContext)
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
