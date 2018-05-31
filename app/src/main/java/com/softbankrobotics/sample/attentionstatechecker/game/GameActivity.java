/*
 * Copyright (C) 2018 Softbank Robotics Europe
 * See COPYING for the license
 */

package com.softbankrobotics.sample.attentionstatechecker.game;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import com.aldebaran.qi.sdk.QiSDK;
import com.softbankrobotics.sample.attentionstatechecker.R;

public class GameActivity extends AppCompatActivity {

    @NonNull
    private final GameMachine gameMachine = new GameMachine();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        QiSDK.register(this, gameMachine);
    }

    @Override
    protected void onDestroy() {
        QiSDK.unregister(this);
        super.onDestroy();
    }
}
