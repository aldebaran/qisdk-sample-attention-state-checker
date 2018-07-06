/*
 * Copyright (C) 2018 Softbank Robotics Europe
 * See COPYING for the license
 */

package com.softbankrobotics.sample.attentionstatechecker.game

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.aldebaran.qi.sdk.QiSDK
import com.aldebaran.qi.sdk.design.activity.RobotActivity
import com.aldebaran.qi.sdk.design.activity.conversationstatus.SpeechBarDisplayStrategy
import com.softbankrobotics.sample.attentionstatechecker.R
import com.softbankrobotics.sample.attentionstatechecker.introduction.IntroductionActivity
import com.softbankrobotics.sample.attentionstatechecker.model.data.Direction
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_game.*

/**
 * The game activity.
 */
class GameActivity : RobotActivity() {

    private val gameMachine = GameMachine()

    private val gameRobot = GameRobot(gameMachine)

    private val disposables = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setSpeechBarDisplayStrategy(SpeechBarDisplayStrategy.OVERLAY)
        setContentView(R.layout.activity_game)

        QiSDK.register(this, gameRobot)
    }

    override fun onResume() {
        super.onResume()
        disposables.add(gameMachine.gameState()
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
            is GameState.Idle, is GameState.Briefing -> {
                hideExpectedDirection()
                hideLookDirection()
                hideHuman()
            }
            is GameState.Instructions -> {
                showExpectedDirection(gameState.expectedDirection)
                hideLookDirection()
                hideHuman()
            }
            is GameState.Playing -> {
                showExpectedDirection(gameState.expectedDirection)
                showWaitingForLookDirection()
                showHuman()
            }
            is GameState.NotMatching -> {
                showExpectedDirection(gameState.expectedDirection)
                showLookDirection(gameState.lookDirection)
                showHuman()
            }
            is GameState.Matching -> {
                showExpectedDirection(gameState.matchingDirection)
                showLookDirection(gameState.matchingDirection)
                showHuman()
            }
            is GameState.Win -> {
                hideExpectedDirection()
                hideLookDirection()
                hideHuman()
            }
            is GameState.End -> {
                val intent = Intent(this, IntroductionActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
            }
        }
    }

    private fun hideExpectedDirection() {
        expectedDirectionTextView.text = ""
    }

    private fun showExpectedDirection(direction: Direction) {
        expectedDirectionTextView.text = getString(R.string.look_instruction, direction.toString())
    }

    private fun hideLookDirection() {
        lookDirectionTextView.text = ""
    }

    private fun showLookDirection(direction: Direction) {
        lookDirectionTextView.text = direction.toString()
    }

    private fun showWaitingForLookDirection() {
        lookDirectionTextView.text = getString(R.string.question_mark)
    }

    private fun hideHuman() {
        humanImageView.visibility = View.INVISIBLE
    }

    private fun showHuman() {
        humanImageView.visibility = View.VISIBLE
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
