/*
 * Copyright (C) 2018 Softbank Robotics Europe
 * See COPYING for the license
 */

package com.softbankrobotics.sample.attentionstatechecker.game

import com.aldebaran.qi.Future
import com.aldebaran.qi.sdk.QiContext
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks
import com.aldebaran.qi.sdk.builder.SayBuilder
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

    private val matchingSentences = listOf("Great!", "Awesome!", "Nice!", "Excellent!")
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
                say("I will give you some directions, and you will have to move your eyes toward them. Let's do this 4 times!").andThenConsume { gameMachine.postEvent(GameEvent.BriefingFinished) }
            }
            is GameState.Instructions -> {
                val text = "Look ${gameState.expectedDirection}"
                say(text).andThenConsume { gameMachine.postEvent(GameEvent.InstructionsFinished) }
            }
            is GameState.NotMatching -> {
                val text = if (gameState.consecutiveErrors < 3) {
                    "Not ${gameState.lookDirection}."
                } else {
                    "Not ${gameState.lookDirection}. Little tips: move your eyes, not your head."
                }

                say(text).andThenConsume { gameMachine.postEvent(GameEvent.NotMatchingFinished) }
            }
            is GameState.Matching -> {
                say(randomMatchingSentence()).andThenConsume { gameMachine.postEvent(GameEvent.MatchingFinished) }
            }
            is GameState.Win -> {
                say("Impressive! We’ve done it! If you want we can start again.").andThenConsume { gameMachine.postEvent(GameEvent.WinFinished) }
            }
            is GameState.Stopping -> {
                say("Ok, let's have a break.").andThenConsume { gameMachine.postEvent(GameEvent.Stopped) }
            }
        }
    }

    private fun handleDirection(direction: Direction) {
        val expectedDirection = expectedDirection ?: throw IllegalStateException("No expected direction!")

        if (directionsMatcher.matches(expectedDirection, direction)) {
            gameMachine.postEvent(GameEvent.Match)
        } else {
            gameMachine.postEvent(GameEvent.NotMatch(direction))
        }
    }

    private fun say(text: String): Future<Void> {
        return speech.cancellation()
                .andThenCompose {
                    val newSpeech = SayBuilder.with(qiContext)
                            .withText(text)
                            .buildAsync()
                            .andThenCompose { it.async().run() }

                    speech = newSpeech
                    newSpeech
                }
    }

    private fun randomMatchingSentence(): String {
        val i = matchingSentenceRandom.nextInt(matchingSentences.size)
        return matchingSentences[i]
    }
}
