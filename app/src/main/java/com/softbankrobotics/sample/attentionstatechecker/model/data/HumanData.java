/*
 * Copyright (C) 2018 Softbank Robotics Europe
 * See COPYING for the license
 */

package com.softbankrobotics.sample.attentionstatechecker.model.data;

import android.support.annotation.NonNull;

import com.aldebaran.qi.sdk.object.human.AttentionState;
import com.aldebaran.qi.sdk.object.human.Human;

/**
 * Hold a {@link Human} data.
 */
public class HumanData {

    @NonNull
    private final Human human;
    @NonNull
    private final AttentionState attentionState;
    @NonNull
    private final Double distance;

    public HumanData(@NonNull Human human, @NonNull AttentionState attentionState, @NonNull Double distance) {
        this.human = human;
        this.attentionState = attentionState;
        this.distance = distance;
    }

    @NonNull
    public Human getHuman() {
        return human;
    }

    @NonNull
    public AttentionState getAttentionState() {
        return attentionState;
    }

    @NonNull
    public Double getDistance() {
        return distance;
    }

    @Override
    public String toString() {
        return "HumanData{" +
                "human=" + human +
                ", attentionState=" + attentionState +
                ", distance=" + distance +
                '}';
    }
}
