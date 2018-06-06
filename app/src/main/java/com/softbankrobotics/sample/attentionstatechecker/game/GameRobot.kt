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
import com.softbankrobotics.sample.attentionstatechecker.model.rx.observable.DirectionObservable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

/**
 * The game robot.
 */
internal class GameRobot : RobotLifecycleCallbacks {

    private val directionsMatcher = DirectionsMatcher()
    private val lock = Any()

    private var qiContext: QiContext? = null
    private var gameStateDisposable: Disposable? = null
    private var directionDisposable: Disposable? = null
    private var expectedDirection: Direction? = null

    override fun onRobotFocusGained(qiContext: QiContext) {
        this.qiContext = qiContext
        subscribeToGameState()

        GameMachine.postEvent(GameEvent.FOCUS_GAINED)
    }

    override fun onRobotFocusLost() {
        unSubscribeFromGameState()
        unSubscribeFromDirections()
        this.qiContext = null

        GameMachine.postEvent(GameEvent.FOCUS_LOST)
    }

    override fun onRobotFocusRefused(reason: String) {
        // Not used.
    }

    private fun subscribeToGameState() {
        gameStateDisposable = GameMachine.gameState()
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

                directionDisposable = DirectionObservable(context)
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
            is GameState.Intro -> {
                say("Follow my instructions by looking where I tell you to").andThenConsume { GameMachine.postEvent(GameEvent.INTRO_FINISHED) }
            }
            is GameState.Instructions -> {
                val text = "Look ${gameState.expectedDirection}"
                say(text).andThenConsume { GameMachine.postEvent(GameEvent.INSTRUCTIONS_FINISHED) }
            }
            is GameState.NotMatching -> {
                val (expectedDirection, lookDirection) = gameState
                val text = "Don't look $lookDirection, look $expectedDirection"
                say(text).andThenConsume { GameMachine.postEvent(GameEvent.NOT_MATCHING_FINISHED) }
            }
            is GameState.Matching -> {
                say("Great!").andThenConsume { GameMachine.postEvent(GameEvent.MATCHING_FINISHED) }
            }
        }
    }

    private fun handleDirection(direction: Direction) {
        val expectedDirection = expectedDirection ?: throw IllegalStateException("No expected direction!")

        if (directionsMatcher.matches(expectedDirection, direction)) {
            GameMachine.postEvent(GameEvent.MATCH)
        } else {
            GameMachine.postNotMatchingEvent(direction)
        }
    }

    private fun say(text: String): Future<Void> {
        return SayBuilder.with(qiContext)
                .withText(text)
                .build()
                .async()
                .run()
    }
}
