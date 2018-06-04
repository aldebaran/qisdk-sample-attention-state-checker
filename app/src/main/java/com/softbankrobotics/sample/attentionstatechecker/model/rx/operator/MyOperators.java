/*
 * Copyright (C) 2018 Softbank Robotics Europe
 * See COPYING for the license
 */

package com.softbankrobotics.sample.attentionstatechecker.model.rx.operator;

import android.support.annotation.NonNull;

import com.aldebaran.qi.sdk.object.human.AttentionState;
import com.softbankrobotics.sample.attentionstatechecker.model.data.Direction;
import com.softbankrobotics.sample.attentionstatechecker.model.data.HumanData;
import com.softbankrobotics.sample.attentionstatechecker.model.data.Wrapper;

import java.util.Collections;
import java.util.List;

import io.reactivex.Observable;

/**
 * Custom operators to transform observables.
 */
public final class MyOperators {

    private MyOperators() {}

    @NonNull
    public static Observable<Wrapper<HumanData>> closest(@NonNull Observable<List<HumanData>> observable) {
        return observable.map(humanDataList -> {
            if (humanDataList.isEmpty()) {
                return Wrapper.empty();
            }

            HumanData min = Collections.min(humanDataList,
                    (humanData1, humanData2) -> humanData1.getDistance().compareTo(humanData2.getDistance()));

            return Wrapper.of(min);
        });
    }

    @NonNull
    public static Observable<Wrapper<AttentionState>> attentionState(@NonNull Observable<Wrapper<HumanData>> observable) {
        return observable.map(wrapper -> {
            if (!wrapper.hasContent()) {
                return Wrapper.empty();
            }

            return Wrapper.of(wrapper.getContent().getAttentionState());
        });
    }

    @NonNull
    public static Observable<Direction> direction(@NonNull Observable<Wrapper<AttentionState>> observable) {
        return observable.map(wrapper -> {
            if (!wrapper.hasContent()) {
                return Direction.UNKNOWN;
            }

            AttentionState attentionState = wrapper.getContent();

            switch (attentionState) {
                case LOOKING_UP:
                    return Direction.UP;
                case LOOKING_DOWN:
                    return Direction.DOWN;
                case LOOKING_LEFT:
                    return Direction.LEFT;
                case LOOKING_RIGHT:
                    return Direction.RIGHT;
                case LOOKING_UP_LEFT:
                    return Direction.UP_LEFT;
                case LOOKING_UP_RIGHT:
                    return Direction.UP_RIGHT;
                case LOOKING_DOWN_LEFT:
                    return Direction.DOWN_LEFT;
                case LOOKING_DOWN_RIGHT:
                    return Direction.DOWN_RIGHT;
                default:
                    return Direction.UNKNOWN;
            }
        });
    }
}
