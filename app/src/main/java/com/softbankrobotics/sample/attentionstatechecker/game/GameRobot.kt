/*
 * Copyright (C) 2018 Softbank Robotics Europe
 * See COPYING for the license
 */

package com.softbankrobotics.sample.attentionstatechecker.game

import androidx.annotation.StringRes
import com.aldebaran.qi.Future
import com.aldebaran.qi.sdk.QiContext
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks
import com.aldebaran.qi.sdk.builder.SayBuilder
import com.softbankrobotics.sample.attentionstatechecker.R
import com.softbankrobotics.sample.attentionstatechecker.model.data.Direction
import com.softbankrobotics.sample.attentionstatechecker.utils.cancellation
import com.softbankrobotics.sample.attentionstatechecker.utils.directionObservable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.*

/**
 * The game robot.
 */
internal class GameRobot(private val gameMachine: GameMachine) : RobotLifecycleCallbacks {

    private val directionsMatcher = DirectionsMatcher()
    private val lock = Any()

    private var qiContext: QiContext? = null
    private var gameStateDisposable: Disposable? = null
    private var directionDisposable: Disposable? = null
    private var expectedDirection: Direction? = null

    private var speech: Future<Void>? = null

    private val matchingSentences =
            listOf(
                    R.string.matching_sentence_1,
                    R.string.matching_sentence_2,
                    R.string.matching_sentence_3,
                    R.string.matching_sentence_4
            )
    private val matchingSentenceRandom = Random()

    override fun onRobotFocusGained(qiContext: QiContext) {
        this.qiContext = qiContext
        subscribeToGameState()

        gameMachine.postEvent(GameEvent.FocusGained)
    }

    override fun onRobotFocusLost() {
        unSubscribeFromGameState()
        unSubscribeFromDirections()
        this.qiContext = null

        gameMachine.postEvent(GameEvent.FocusLost)
    }

    override fun onRobotFocusRefused(reason: String) {
        // Not used.
    }

    private fun subscribeToGameState() {
        gameStateDisposable = gameMachine.gameState()
                .subscribeOn(Schedulers.io())
                .distinctUntilChanged()
                .observeOn(Schedulers.io())
                .subscribe(this::handleGameState)
    }

    private fun unSubscribeFromGameState() {
        gameStateDisposable?.takeUnless { it.isDisposed }?.dispose()
    }

    private fun subscribeToDirections(expectedDirection: Direction) {
        synchronized(lock) {
            qiContext?.takeIf { directionDisposable == null }?.let { context ->
                this.expectedDirection = expectedDirection

                directionDisposable = directionObservable(context)
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.io())
                        .subscribe(this::handleDirection)
            }
        }
    }

    private fun unSubscribeFromDirections() {
        synchronized(lock) {
            directionDisposable?.takeUnless { it.isDisposed }?.dispose()
            directionDisposable = null
        }
    }

    private fun handleGameState(gameState: GameState) {
        if (gameState is GameState.Playing) {
            subscribeToDirections(gameState.expectedDirection)
        } else {
            unSubscribeFromDirections()
        }

        when (gameState) {
            is GameState.Briefing -> {
                say(R.string.briefing_sentence)
                        .andThenConsume { gameMachine.postEvent(GameEvent.BriefingFinished) }
            }
            is GameState.Instructions -> {
                say(R.string.instructions_sentence, gameState.expectedDirection)
                        .andThenConsume { gameMachine.postEvent(GameEvent.InstructionsFinished) }
            }
            is GameState.NotMatching -> {
                val resId = when (gameState.consecutiveErrors % 3) {
                    1 -> R.string.not_matching_sentence_1
                    2 -> R.string.not_matching_sentence_2
                    0 -> R.string.not_matching_sentence_3
                    else -> throw IllegalStateException("Missing case for NotMatching sentence.")
                }

                say(resId, gameState.lookDirection)
                        .andThenConsume { gameMachine.postEvent(GameEvent.NotMatchingFinished) }
            }
            is GameState.Matching -> {
                say(randomMatchingSentence())
                        .andThenConsume { gameMachine.postEvent(GameEvent.MatchingFinished) }
            }
            is GameState.Win -> {
                say(R.string.win_sentence)
                        .andThenConsume { gameMachine.postEvent(GameEvent.WinFinished) }
            }
            is GameState.Stopping -> {
                say(R.string.stopping_sentence)
                        .andThenConsume { gameMachine.postEvent(GameEvent.Stopped) }
            }
        }
    }

    private fun handleDirection(direction: Direction) {
        val expectedDirection = expectedDirection ?: throw IllegalStateException("Not expected direction!")

        if (directionsMatcher.matches(expectedDirection, direction)) {
            gameMachine.postEvent(GameEvent.Match)
        } else {
            gameMachine.postEvent(GameEvent.NotMatch(direction))
        }
    }

    private fun say(@StringRes resId: Int, vararg formatArgs: Any): Future<Void> {
        return speech.cancellation()
                .andThenCompose { _ ->
                    val newSpeech = SayBuilder.with(qiContext)
                            .withResource(resId, *formatArgs)
                            .buildAsync()
                            .andThenCompose { it.async().run() }

                    speech = newSpeech
                    newSpeech
                }
    }

    @StringRes
    private fun randomMatchingSentence(): Int {
        val i = matchingSentenceRandom.nextInt(matchingSentences.size)
        return matchingSentences[i]
    }
}
