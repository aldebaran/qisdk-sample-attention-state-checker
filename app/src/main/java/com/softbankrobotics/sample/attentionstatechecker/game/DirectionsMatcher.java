/*
 * Copyright (C) 2018 Softbank Robotics Europe
 * See COPYING for the license
 */

package com.softbankrobotics.sample.attentionstatechecker.game;

import android.support.annotation.NonNull;

import com.softbankrobotics.sample.attentionstatechecker.model.data.Direction;

/**
 * A {@link Direction} matcher.
 */
class DirectionsMatcher {

    /**
     * Returns {@code true} if the lookDirection matches the expectedDirection.
     * @param expectedDirection The expected direction
     * @param lookDirection The look direction
     * @return Returns {@code true} if the lookDirection matches the expectedDirection.
     */
    boolean matches(@NonNull Direction expectedDirection, @NonNull Direction lookDirection) {
        switch (expectedDirection) {
            case UP:
                return (lookDirection.equals(Direction.UP) ||
                        lookDirection.equals(Direction.UP_LEFT) ||
                        lookDirection.equals(Direction.UP_RIGHT));
            case DOWN:
                return (lookDirection.equals(Direction.DOWN) ||
                        lookDirection.equals(Direction.DOWN_LEFT) ||
                        lookDirection.equals(Direction.DOWN_RIGHT));
            case LEFT:
                return (lookDirection.equals(Direction.LEFT) ||
                        lookDirection.equals(Direction.UP_LEFT) ||
                        lookDirection.equals(Direction.DOWN_LEFT));
            case RIGHT:
                return (lookDirection.equals(Direction.RIGHT) ||
                        lookDirection.equals(Direction.UP_RIGHT) ||
                        lookDirection.equals(Direction.DOWN_RIGHT));
            default:
                return false;
        }
    }
}
