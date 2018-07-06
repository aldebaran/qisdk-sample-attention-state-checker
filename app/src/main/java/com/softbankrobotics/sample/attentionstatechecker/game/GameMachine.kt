/*
 * Copyright (C) 2018 Softbank Robotics Europe
 * See COPYING for the license
 */

package com.softbankrobotics.sample.attentionstatechecker.game

import com.softbankrobotics.sample.attentionstatechecker.model.data.Direction
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import java.util.*

/**
 * A game machine.
 */
internal class GameMachine {

    private val random = Random()
    private val directions = Arrays.asList(Direction.UP, Direction.DOWN, Direction.LEFT, Direction.RIGHT)
    private val directionsSize = directions.size

    private val subject = BehaviorSubject.createDefault<GameState>(GameState.Idle)

    fun gameState(): Observable<GameState> = subject

    fun postEvent(gameEvent: GameEvent) {
        val currentState = subject.value

        when (gameEvent) {
            is GameEvent.FocusGained -> if (currentState === GameState.Idle) {
                subject.onNext(GameState.Briefing)
            }
            is GameEvent.FocusLost -> subject.onNext(GameState.Idle)
            is GameEvent.BriefingFinished -> if (currentState === GameState.Briefing) {
                subject.onNext(GameState.Instructions(computeRandomDirection()))
            }
            is GameEvent.InstructionsFinished -> if (currentState is GameState.Instructions) {
                subject.onNext(GameState.Playing(currentState.expectedDirection))
            }
            is GameEvent.Match -> if (currentState is GameState.Playing) {
                subject.onNext(GameState.Matching(currentState.expectedDirection))
            }
            is GameEvent.NotMatch -> if (currentState is GameState.Playing) {
                subject.onNext(GameState.NotMatching(currentState.expectedDirection, gameEvent.lookDirection))
            }
            is GameEvent.MatchingFinished -> if (currentState is GameState.Matching) {
                subject.onNext(GameState.Instructions(computeRandomDirection()))
            }
            is GameEvent.NotMatchingFinished -> if (currentState is GameState.NotMatching) {
                subject.onNext(GameState.Playing(currentState.expectedDirection))
            }
        }
    }

    private fun computeRandomDirection() = directions[random.nextInt(directionsSize)]
}
