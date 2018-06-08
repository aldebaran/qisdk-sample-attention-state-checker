/*
 * Copyright (C) 2018 Softbank Robotics Europe
 * See COPYING for the license
 */

package com.softbankrobotics.sample.attentionstatechecker.game;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.aldebaran.qi.sdk.QiSDK;
import com.aldebaran.qi.sdk.design.activity.RobotActivity;
import com.aldebaran.qi.sdk.design.activity.conversationstatus.SpeechBarDisplayStrategy;
import com.softbankrobotics.sample.attentionstatechecker.R;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * The game activity.
 */
public class GameActivity extends RobotActivity {

    @NonNull
    private final GameRobot gameRobot = new GameRobot();

    @Nullable
    private Disposable gameStateDisposable;

    private TextView expectedDirectionTextView;
    private TextView lookDirectionTextView;
    private ImageView humanImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setSpeechBarDisplayStrategy(SpeechBarDisplayStrategy.OVERLAY);
        setContentView(R.layout.activity_game);

        expectedDirectionTextView = findViewById(R.id.expectedDirectionTextView);
        lookDirectionTextView = findViewById(R.id.lookDirectionTextView);
        humanImageView = findViewById(R.id.humanImageView);

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

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
    }

    private void handleGameState(@NonNull GameState gameState) {
        if (gameState instanceof GameState.Idle || gameState instanceof GameState.Intro) {
            expectedDirectionTextView.setText("");
            lookDirectionTextView.setText("");
            humanImageView.setVisibility(View.INVISIBLE);
        } else if (gameState instanceof GameState.Instructions) {
            GameState.Instructions instructions = (GameState.Instructions) gameState;
            expectedDirectionTextView.setText(getString(R.string.look_instruction, instructions.getExpectedDirection().toString()));
            lookDirectionTextView.setText("");
            humanImageView.setVisibility(View.INVISIBLE);
        } else if (gameState instanceof GameState.Playing) {
            GameState.Playing playing = (GameState.Playing) gameState;
            expectedDirectionTextView.setText(getString(R.string.look_instruction, playing.getExpectedDirection().toString()));
            lookDirectionTextView.setText(R.string.question_mark);
            humanImageView.setVisibility(View.VISIBLE);
        } else if (gameState instanceof GameState.NotMatching) {
            GameState.NotMatching notMatching = (GameState.NotMatching) gameState;
            expectedDirectionTextView.setText(getString(R.string.look_instruction, notMatching.getExpectedDirection().toString()));
            lookDirectionTextView.setText(notMatching.getLookDirection().toString());
            humanImageView.setVisibility(View.VISIBLE);
        } else if (gameState instanceof GameState.Matching) {
            GameState.Matching matching = (GameState.Matching) gameState;
            expectedDirectionTextView.setText(getString(R.string.look_instruction, matching.getMatchingDirection().toString()));
            lookDirectionTextView.setText(matching.getMatchingDirection().toString());
            humanImageView.setVisibility(View.VISIBLE);
        }
    }

    private void hideSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }
}
