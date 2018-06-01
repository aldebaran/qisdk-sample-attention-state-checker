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

    private TextView directionTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        directionTextView = findViewById(R.id.directionTextView);

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
            directionTextView.setText("");
        } else if (gameState instanceof GameState.Instructions) {
            GameState.Instructions instructions = (GameState.Instructions) gameState;
            directionTextView.setText(instructions.getExpectedDirection().toString());
        } else if (gameState instanceof GameState.Playing) {
            GameState.Playing playing = (GameState.Playing) gameState;
            directionTextView.setText(playing.getExpectedDirection().toString());
        } else if (gameState instanceof GameState.Matching) {
            directionTextView.setText("");
        }
    }
}
