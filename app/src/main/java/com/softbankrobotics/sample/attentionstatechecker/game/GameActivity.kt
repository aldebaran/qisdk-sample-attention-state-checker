/*
 * Copyright (C) 2018 Softbank Robotics Europe
 * See COPYING for the license
 */

package com.softbankrobotics.sample.attentionstatechecker.game

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import androidx.annotation.RawRes
import android.view.View
import com.aldebaran.qi.sdk.QiSDK
import com.aldebaran.qi.sdk.design.activity.RobotActivity
import com.aldebaran.qi.sdk.design.activity.conversationstatus.SpeechBarDisplayStrategy
import com.softbankrobotics.sample.attentionstatechecker.R
import com.softbankrobotics.sample.attentionstatechecker.introduction.IntroductionActivity
import com.softbankrobotics.sample.attentionstatechecker.model.data.Direction
import com.softbankrobotics.sample.attentionstatechecker.model.data.Direction.*
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

    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setSpeechBarDisplayStrategy(SpeechBarDisplayStrategy.OVERLAY)
        setContentView(R.layout.activity_game)

        homeButton.setOnClickListener { goToHome() }
        closeButton.setOnClickListener { finishAffinity() }
        stopButton.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                gameMachine.postEvent(GameEvent.Stop)
            }
        }

        QiSDK.register(this, gameRobot)
    }

    override fun onResume() {
        super.onResume()
        stopButton.isChecked = false

        disposables.add(gameMachine.gameState()
                            .subscribeOn(Schedulers.io())
                            .distinctUntilChanged()
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(this::handleGameState))
    }

    override fun onPause() {
        disposables.clear()
        mediaPlayer?.release()
        mediaPlayer = null
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

    override fun onBackPressed() {
        goToHome()
    }

    private fun handleGameState(gameState: GameState) {
        when (gameState) {
            is GameState.Idle,
            is GameState.Briefing,
            is GameState.Stopping -> {
                showStop()
                hideExpectedDirection()
                hideProgress()
                showNeutralHuman()
                hideTrophy()
            }
            is GameState.Instructions -> {
                showStop()
                showExpectedDirection(gameState.expectedDirection)
                showProgress(gameState.matched, gameState.total)
                hideTrophy()
            }
            is GameState.Playing -> {
                showStop()
                showExpectedDirection(gameState.expectedDirection)
                showProgress(gameState.matched, gameState.total)
                hideTrophy()
            }
            is GameState.NotMatching -> {
                showStop()
                showExpectedDirection(gameState.expectedDirection)
                showProgress(gameState.matched, gameState.total)
                hideTrophy()
                playSound(R.raw.error)
            }
            is GameState.Matching -> {
                showStop()
                showExpectedDirection(gameState.matchingDirection)
                showProgress(gameState.matched, gameState.total)
                hideTrophy()
                playSound(R.raw.success)
            }
            is GameState.Win -> {
                hideStop()
                hideExpectedDirection()
                hideProgress()
                showTrophy()
                playSound(R.raw.big_success)
            }
            is GameState.End -> {
                goToHome()
            }
        }
    }

    private fun hideExpectedDirection() {
        expectedDirectionTextView.text = ""
        humanImageView.visibility = View.INVISIBLE
    }

    private fun showExpectedDirection(direction: Direction) {
        expectedDirectionTextView.text = getString(R.string.look_instruction, direction.toString())
        humanImageView.visibility = View.VISIBLE
        humanImageView.setImageResource(humanImageFromDirection(direction))
    }

    private fun showNeutralHuman() {
        humanImageView.visibility = View.VISIBLE
        humanImageView.setImageResource(R.drawable.ic_user_face)
    }

    private fun hideTrophy() {
        trophyImageView.visibility = View.INVISIBLE
    }

    private fun showTrophy() {
        trophyImageView.visibility = View.VISIBLE
    }

    private fun humanImageFromDirection(direction: Direction) =
        when (direction) {
            UP -> R.drawable.ic_user_face_up
            DOWN -> R.drawable.ic_user_face_down
            LEFT -> R.drawable.ic_user_face_left
            RIGHT -> R.drawable.ic_user_face_right
            else -> throw IllegalStateException("Unknown direction $direction")
        }

    private fun hideProgress() {
        progressTextView.visibility = View.INVISIBLE
    }

    private fun showProgress(matched: Int, total: Int) {
        progressTextView.apply {
            visibility = View.VISIBLE
            text = getString(R.string.progress, matched, total)
        }
    }

    private fun hideStop() {
        stopButton.visibility = View.INVISIBLE
    }

    private fun showStop() {
        stopButton.visibility = View.VISIBLE
    }

    private fun goToHome() {
        val intent = Intent(this, IntroductionActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        intent.putExtra("startAtHome", true)
        startActivity(intent)
    }

    private fun playSound(@RawRes resource: Int) {
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer.create(this, resource).apply { start() }
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
