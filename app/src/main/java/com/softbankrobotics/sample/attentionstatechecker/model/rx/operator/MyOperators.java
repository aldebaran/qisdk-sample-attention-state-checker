/*
 * Copyright (C) 2018 Softbank Robotics Europe
 * See COPYING for the license
 */

package com.softbankrobotics.sample.attentionstatechecker.model.rx.operator;

import android.support.annotation.NonNull;

import com.aldebaran.qi.sdk.object.human.AttentionState;
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
}
