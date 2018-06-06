/*
 * Copyright (C) 2018 Softbank Robotics Europe
 * See COPYING for the license
 */

package com.softbankrobotics.sample.attentionstatechecker.game

import com.softbankrobotics.sample.attentionstatechecker.model.data.Direction

import java.util.ArrayList
import java.util.Arrays
import java.util.Random

import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject

/**
 * The game machine.
 */
internal object GameMachine {

    private val random = Random()
    private val directions = Arrays.asList(Direction.UP, Direction.DOWN, Direction.LEFT, Direction.RIGHT)
    private val directionsSize = directions.size

    private val subject = BehaviorSubject.createDefault<GameState>(GameState.Idle)

    fun gameState(): Observable<GameState> = subject

    fun postEvent(gameEvent: GameEvent) {
        val currentState = subject.value

        when (gameEvent) {
            GameEvent.FOCUS_GAINED -> if (currentState === GameState.Idle) {
                subject.onNext(GameState.Intro)
            }
            GameEvent.INTRO_FINISHED -> if (currentState === GameState.Intro) {
                subject.onNext(GameState.Instructions(computeRandomDirection()))
            }
            GameEvent.FOCUS_LOST -> subject.onNext(GameState.Idle)
            GameEvent.INSTRUCTIONS_FINISHED -> if (currentState is GameState.Instructions) {
                subject.onNext(GameState.Playing(currentState.expectedDirection))
            }
            GameEvent.MATCH -> if (currentState is GameState.Playing) {
                subject.onNext(GameState.Matching(currentState.expectedDirection))
            }
            GameEvent.MATCHING_FINISHED -> if (currentState is GameState.Matching) {
                subject.onNext(GameState.Instructions(computeRandomDirection()))
            }
            GameEvent.NOT_MATCHING_FINISHED -> if (currentState is GameState.NotMatching) {
                subject.onNext(GameState.Playing(currentState.expectedDirection))
            }
        }
    }

    fun postNotMatchingEvent(lookDirection: Direction) {
        val currentState = subject.value

        if (currentState is GameState.Playing) {
            subject.onNext(GameState.NotMatching(currentState.expectedDirection, lookDirection))
        }
    }

    private fun computeRandomDirection() = directions[random.nextInt(directionsSize)]
}
