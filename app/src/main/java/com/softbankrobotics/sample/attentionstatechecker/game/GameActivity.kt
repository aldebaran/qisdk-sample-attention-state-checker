/*
 * Copyright (C) 2018 Softbank Robotics Europe
 * See COPYING for the license
 */

package com.softbankrobotics.sample.attentionstatechecker.game

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.aldebaran.qi.sdk.QiSDK
import com.aldebaran.qi.sdk.design.activity.RobotActivity
import com.aldebaran.qi.sdk.design.activity.conversationstatus.SpeechBarDisplayStrategy
import com.softbankrobotics.sample.attentionstatechecker.R
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

/**
 * The game activity.
 */
class GameActivity : RobotActivity() {

    private val gameRobot = GameRobot()

    private val disposables = CompositeDisposable()

    private lateinit var expectedDirectionTextView: TextView
    private lateinit var lookDirectionTextView: TextView
    private lateinit var humanImageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setSpeechBarDisplayStrategy(SpeechBarDisplayStrategy.OVERLAY)
        setContentView(R.layout.activity_game)

        expectedDirectionTextView = findViewById(R.id.expectedDirectionTextView)
        lookDirectionTextView = findViewById(R.id.lookDirectionTextView)
        humanImageView = findViewById(R.id.humanImageView)

        QiSDK.register(this, gameRobot)
    }

    override fun onResume() {
        super.onResume()
        disposables.add(GameMachine.gameState()
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(this::handleGameState))
    }

    override fun onPause() {
        disposables.clear()
        super.onPause()
    }

    override fun onDestroy() {
        QiSDK.unregister(this)
        super.onDestroy()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemUI()
        }
    }

    private fun handleGameState(gameState: GameState) {
        when (gameState) {
            is GameState.Idle, is GameState.Intro -> {
                expectedDirectionTextView.text = ""
                lookDirectionTextView.text = ""
                humanImageView.visibility = View.INVISIBLE
            }
            is GameState.Instructions -> {
                expectedDirectionTextView.text = getString(R.string.look_instruction, gameState.expectedDirection.toString())
                lookDirectionTextView.text = ""
                humanImageView.visibility = View.INVISIBLE
            }
            is GameState.Playing -> {
                expectedDirectionTextView.text = getString(R.string.look_instruction, gameState.expectedDirection.toString())
                lookDirectionTextView.text = getString(R.string.question_mark)
                humanImageView.visibility = View.VISIBLE
            }
            is GameState.NotMatching -> {
                expectedDirectionTextView.text = getString(R.string.look_instruction, gameState.expectedDirection.toString())
                lookDirectionTextView.text = gameState.lookDirection.toString()
                humanImageView.visibility = View.VISIBLE
            }
            is GameState.Matching -> {
                expectedDirectionTextView.text = getString(R.string.look_instruction, gameState.matchingDirection.toString())
                lookDirectionTextView.text = gameState.matchingDirection.toString()
                humanImageView.visibility = View.VISIBLE
            }
        }
    }

    private fun hideSystemUI() {
        val decorView = window.decorView
        decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
    }
}
