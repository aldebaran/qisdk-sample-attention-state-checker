/*
 * Copyright (C) 2018 Softbank Robotics Europe
 * See COPYING for the license
 */

package com.softbankrobotics.sample.attentionstatechecker.game;

import android.support.annotation.NonNull;

import com.softbankrobotics.sample.attentionstatechecker.model.data.Direction;

/**
 * A game state.
 */
interface GameState {

    /**
     * The idle game state.
     */
    final class Idle implements GameState {
        private Idle() {}

        @NonNull
        static Idle getInstance() {
            return Holder.INSTANCE;
        }

        private static final class Holder {
            @NonNull
            private static final Idle INSTANCE = new Idle();
        }
    }

    /**
     * The intro state.
     */
    final class Intro implements GameState {
        private Intro() {}

        @NonNull
        static Intro getInstance() {
            return Holder.INSTANCE;
        }

        private static final class Holder {
            @NonNull
            private static final Intro INSTANCE = new Intro();
        }
    }

    /**
     * Game state representing the instruction phase.
     */
    final class Instructions implements GameState {
        @NonNull
        private final Direction expectedDirection;

        Instructions(@NonNull Direction expectedDirection) {
            this.expectedDirection = expectedDirection;
        }

        @NonNull
        public Direction getExpectedDirection() {
            return expectedDirection;
        }
    }

    /**
     * Game state representing the playing phase.
     */
    final class Playing implements GameState {
        @NonNull
        private final Direction expectedDirection;

        Playing(@NonNull Direction expectedDirection) {
            this.expectedDirection = expectedDirection;
        }

        @NonNull
        public Direction getExpectedDirection() {
            return expectedDirection;
        }
    }

    /**
     * Game state representing a not matching combination.
     */
    final class NotMatching implements GameState {
        @NonNull
        private final Direction expectedDirection;
        @NonNull
        private final Direction lookDirection;

        NotMatching(@NonNull Direction expectedDirection, @NonNull Direction lookDirection) {
            this.expectedDirection = expectedDirection;
            this.lookDirection = lookDirection;
        }

        @NonNull
        public Direction getExpectedDirection() {
            return expectedDirection;
        }

        @NonNull
        public Direction getLookDirection() {
            return lookDirection;
        }
    }

    /**
     * Game state representing a matching combination.
     */
    final class Matching implements GameState {
        @NonNull
        private final Direction matchingDirection;

        Matching(@NonNull Direction matchingDirection) {
            this.matchingDirection = matchingDirection;
        }

        @NonNull
        public Direction getMatchingDirection() {
            return matchingDirection;
        }
    }
}
