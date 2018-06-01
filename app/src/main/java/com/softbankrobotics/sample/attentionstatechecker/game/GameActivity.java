/*
 * Copyright (C) 2018 Softbank Robotics Europe
 * See COPYING for the license
 */

package com.softbankrobotics.sample.attentionstatechecker.game;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.aldebaran.qi.sdk.QiSDK;
import com.softbankrobotics.sample.attentionstatechecker.R;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class GameActivity extends AppCompatActivity {

    @NonNull
    private final GameRobot gameRobot = new GameRobot();

    @Nullable
    private Disposable gameStateDisposable;

    private TextView expectedDirectionTextView;
    private TextView lookDirectionTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        expectedDirectionTextView = findViewById(R.id.expectedDirectionTextView);
        lookDirectionTextView = findViewById(R.id.lookDirectionTextView);

        QiSDK.register(this, gameRobot);
    }

    @Override
    protected void onResume() {
        super.onResume();

        gameStateDisposable = GameMachine.getInstance().gameState()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleGameState);
    }

    @Override
    protected void onPause() {
        if (gameStateDisposable != null && !gameStateDisposable.isDisposed()) {
            gameStateDisposable.dispose();
        }

        super.onPause();
    }

    @Override
    protected void onDestroy() {
        QiSDK.unregister(this);
        super.onDestroy();
    }

    private void handleGameState(@NonNull GameState gameState) {
        if (gameState instanceof GameState.Idle) {
            expectedDirectionTextView.setText("");
            lookDirectionTextView.setText("");
        } else if (gameState instanceof GameState.Instructions) {
            GameState.Instructions instructions = (GameState.Instructions) gameState;
            expectedDirectionTextView.setText(instructions.getExpectedDirection().toString());
            lookDirectionTextView.setText("");
        } else if (gameState instanceof GameState.ReadyToPlay) {
            GameState.ReadyToPlay readyToPlay = (GameState.ReadyToPlay) gameState;
            expectedDirectionTextView.setText(readyToPlay.getExpectedDirection().toString());
            lookDirectionTextView.setText("");
        } else if (gameState instanceof GameState.Playing) {
            GameState.Playing playing = (GameState.Playing) gameState;
            expectedDirectionTextView.setText(playing.getExpectedDirection().toString());
            lookDirectionTextView.setText(playing.getLookDirection().toString());
        } else if (gameState instanceof GameState.Matching) {
            expectedDirectionTextView.setText("");
            lookDirectionTextView.setText("");
        }
    }
}
