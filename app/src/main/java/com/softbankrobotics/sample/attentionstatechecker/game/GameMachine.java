/*
 * Copyright (C) 2018 Softbank Robotics Europe
 * See COPYING for the license
 */

package com.softbankrobotics.sample.attentionstatechecker.game;

import android.support.annotation.NonNull;

import com.softbankrobotics.sample.attentionstatechecker.model.data.Direction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;

/**
 * The game machine.
 */
final class GameMachine {

    @NonNull
    private final Random random;
    @NonNull
    private final List<Direction> directions;
    private final int directionsSize;

    @NonNull
    private final BehaviorSubject<GameState> subject = BehaviorSubject.createDefault(GameState.Idle.getInstance());

    private GameMachine() {
        random = new Random();
        directions = new ArrayList<>(Arrays.asList(Direction.values()));
        directions.remove(Direction.UNKNOWN);
        directionsSize = directions.size();
    }

    @NonNull
    static GameMachine getInstance() {
        return Holder.INSTANCE;
    }

    @NonNull
    Observable<GameState> gameState() {
        return subject;
    }

    void postEvent(@NonNull GameEvent gameEvent) {
        GameState currentState = subject.getValue();

        switch (gameEvent) {
            case FOCUS_GAINED:
                if (currentState instanceof GameState.Idle) {
                    subject.onNext(new GameState.Instructions(computeRandomDirection()));
                }
                break;
            case FOCUS_LOST:
                subject.onNext(GameState.Idle.getInstance());
                break;
            case INSTRUCTIONS_FINISHED:
                if (currentState instanceof GameState.Instructions) {
                    GameState.Instructions instructions = (GameState.Instructions) currentState;
                    Direction expectedDirection = instructions.getExpectedDirection();
                    subject.onNext(new GameState.Playing(expectedDirection));
                }
                break;
            case MATCH:
                if (currentState instanceof GameState.Playing) {
                    GameState.Playing playing = (GameState.Playing) currentState;
                    subject.onNext(new GameState.Matching(playing.getExpectedDirection()));
                }
                break;
            case MATCHING_FINISHED:
                if (currentState instanceof GameState.Matching) {
                    subject.onNext(new GameState.Instructions(computeRandomDirection()));
                }
                break;
            case NOT_MATCHING_FINISHED:
                if (currentState instanceof GameState.NotMatching) {
                    GameState.NotMatching notMatching = (GameState.NotMatching) currentState;
                    subject.onNext(new GameState.Playing(notMatching.getExpectedDirection()));
                }
                break;
        }
    }

    void postNotMatchingEvent(@NonNull Direction lookDirection) {
        GameState currentState = subject.getValue();

        if (currentState instanceof GameState.Playing) {
            GameState.Playing playing = (GameState.Playing) currentState;
            subject.onNext(new GameState.NotMatching(playing.getExpectedDirection(), lookDirection));
        }
    }

    @NonNull
    private Direction computeRandomDirection() {
        return directions.get(random.nextInt(directionsSize));
    }

    private static final class Holder {
        @NonNull
        private static final GameMachine INSTANCE = new GameMachine();
    }
}
