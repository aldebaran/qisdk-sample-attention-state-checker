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

    private var directions: Queue<Direction> = LinkedList()
    private var totalDirections = directions.size

    private val subject = BehaviorSubject.createDefault<GameState>(GameState.Idle)

    private var consecutiveErrors = 0
    private var matchedDirections = 0

    fun gameState(): Observable<GameState> = subject

    fun postEvent(gameEvent: GameEvent) {
        val currentState = subject.value

        when (gameEvent) {
            is GameEvent.FocusGained -> if (currentState === GameState.Idle) {
                subject.onNext(GameState.Briefing)
            }
            is GameEvent.FocusLost -> subject.onNext(GameState.Idle)
            is GameEvent.BriefingFinished -> if (currentState === GameState.Briefing) {
                matchedDirections = 0
                computeDirections()
                publishNextInstruction()
            }
            is GameEvent.InstructionsFinished -> if (currentState is GameState.Instructions) {
                subject.onNext(GameState.Playing(currentState.expectedDirection, matchedDirections, totalDirections))
            }
            is GameEvent.Match -> if (currentState is GameState.Playing) {
                matchedDirections++
                subject.onNext(GameState.Matching(currentState.expectedDirection, matchedDirections, totalDirections))
            }
            is GameEvent.NotMatch -> if (currentState is GameState.Playing) {
                consecutiveErrors++
                subject.onNext(GameState.NotMatching(currentState.expectedDirection, gameEvent.lookDirection, consecutiveErrors, matchedDirections, totalDirections))
            }
            is GameEvent.MatchingFinished -> if (currentState is GameState.Matching) {
                publishNextInstruction()
            }
            is GameEvent.NotMatchingFinished -> if (currentState is GameState.NotMatching) {
                subject.onNext(GameState.Instructions(currentState.expectedDirection, matchedDirections, totalDirections))
            }
            is GameEvent.WinFinished -> if (currentState === GameState.Win) {
                subject.onNext(GameState.End)
            }
            is GameEvent.Stop -> subject.onNext(GameState.Stopping(matchedDirections, totalDirections))
            is GameEvent.Stopped -> if (currentState is GameState.Stopping) {
                subject.onNext(GameState.End)
            }
        }
    }

    private fun publishNextInstruction() {
        if (directions.isNotEmpty()) {
            consecutiveErrors = 0
            subject.onNext(GameState.Instructions(directions.poll(), matchedDirections, totalDirections))
        } else {
            subject.onNext(GameState.Win)
        }
    }

    private fun computeDirections() {
        directions = LinkedList(listOf(Direction.UP, Direction.DOWN, Direction.LEFT, Direction.RIGHT).shuffled())
        totalDirections = directions.size
    }
}
