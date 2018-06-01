/*
 * Copyright (C) 2018 Softbank Robotics Europe
 * See COPYING for the license
 */

package com.softbankrobotics.sample.attentionstatechecker.game;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.aldebaran.qi.Future;
import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;
import com.aldebaran.qi.sdk.builder.SayBuilder;
import com.softbankrobotics.sample.attentionstatechecker.model.data.Direction;
import com.softbankrobotics.sample.attentionstatechecker.model.rx.observable.DirectionObservable;

import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

class GameRobot implements RobotLifecycleCallbacks {

    private static final String TAG = "GameRobot";

    @Nullable
    private QiContext qiContext;
    @Nullable
    private Disposable gameStateDisposable;
    @Nullable
    private Disposable directionDisposable;
    @Nullable
    private Direction expectedDirection;

    @Override
    public void onRobotFocusGained(QiContext qiContext) {
        this.qiContext = qiContext;
        subscribeToGameState();

        GameMachine.getInstance().postEvent(GameEvent.FOCUS_GAINED);
    }

    @Override
    public void onRobotFocusLost() {
        unSubscribeFromGameState();
        unSubscribeFromDirections();
        this.qiContext = null;

        GameMachine.getInstance().postEvent(GameEvent.FOCUS_LOST);
    }

    @Override
    public void onRobotFocusRefused(String reason) {
        // Not used.
    }

    private void subscribeToGameState() {
        gameStateDisposable = GameMachine.getInstance().gameState()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(this::handleGameState);
    }

    private void unSubscribeFromGameState() {
        if (gameStateDisposable != null && !gameStateDisposable.isDisposed()) {
            gameStateDisposable.dispose();
        }
    }

    private void subscribeToDirections(@NonNull Direction expectedDirection) {
        if (qiContext != null) {
            this.expectedDirection = expectedDirection;

            directionDisposable = new DirectionObservable(qiContext)
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.io())
                    .subscribe(this::handleDirection);
        }
    }

    private void unSubscribeFromDirections() {
        if (directionDisposable != null && !directionDisposable.isDisposed()) {
            directionDisposable.dispose();
        }
    }

    private void handleGameState(@NonNull GameState gameState) {
        if (gameState instanceof GameState.Playing) {
            GameState.Playing playing = (GameState.Playing) gameState;
            subscribeToDirections(playing.getExpectedDirection());
        } else {
            unSubscribeFromDirections();
        }

        if (gameState instanceof GameState.Instructions) {
            GameState.Instructions instructions = (GameState.Instructions) gameState;
            String text = "Look " + instructions.getExpectedDirection();
            say(text).andThenConsume(ignored -> GameMachine.getInstance().postEvent(GameEvent.INSTRUCTIONS_FINISHED));
        } else if (gameState instanceof GameState.Matching) {
            say("Great!").andThenConsume(ignored -> GameMachine.getInstance().postEvent(GameEvent.MATCHING_FINISHED));
        }
    }

    private void handleDirection(@NonNull Direction direction) {
        Log.i(TAG, "Look direction: " + direction);

        if (direction.equals(expectedDirection)) {
            GameMachine.getInstance().postEvent(GameEvent.MATCH);
        }
    }

    private Future<Void> say(@NonNull String text) {
        return SayBuilder.with(qiContext)
                .withText(text)
                .build()
                .async()
                .run();
    }
}
