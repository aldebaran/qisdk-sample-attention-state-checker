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
        val currentState = subject.value ?: throw IllegalStateException("GameMachine must have a GameState to be able to handle a GameEvent.")
        val nextState = reduce(currentState, gameEvent)
        subject.onNext(nextState)
    }

    private fun reduce(currentState: GameState, gameEvent: GameEvent): GameState {
        when (gameEvent) {
            is GameEvent.FocusGained -> if (currentState === GameState.Idle)
                return GameState.Briefing
            is GameEvent.FocusLost ->
                return GameState.Idle
            is GameEvent.BriefingFinished -> if (currentState === GameState.Briefing) {
                matchedDirections = 0
                computeDirections()
                return nextInstruction()
            }
            is GameEvent.InstructionsFinished -> if (currentState is GameState.Instructions)
                return GameState.Playing(currentState.expectedDirection, matchedDirections, totalDirections)
            is GameEvent.Match -> if (currentState is GameState.Playing) {
                matchedDirections++
                return GameState.Matching(currentState.expectedDirection, matchedDirections, totalDirections)
            }
            is GameEvent.NotMatch -> if (currentState is GameState.Playing) {
                consecutiveErrors++
                return GameState.NotMatching(currentState.expectedDirection, gameEvent.lookDirection, consecutiveErrors, matchedDirections, totalDirections)
            }
            is GameEvent.MatchingFinished -> if (currentState is GameState.Matching)
                return nextInstruction()
            is GameEvent.NotMatchingFinished -> if (currentState is GameState.NotMatching)
                return GameState.Instructions(currentState.expectedDirection, matchedDirections, totalDirections)
            is GameEvent.WinFinished -> if (currentState === GameState.Win)
                return GameState.End
            is GameEvent.Stop ->
                return GameState.Stopping(matchedDirections, totalDirections)
            is GameEvent.Stopped -> if (currentState is GameState.Stopping)
                return GameState.End
        }

        return currentState
    }

    private fun nextInstruction(): GameState =
        if (directions.isNotEmpty()) {
            consecutiveErrors = 0
            GameState.Instructions(directions.poll(), matchedDirections, totalDirections)
        } else {
            GameState.Win
        }

    private fun computeDirections() {
        directions = LinkedList(listOf(Direction.UP, Direction.DOWN, Direction.LEFT, Direction.RIGHT).shuffled())
        totalDirections = directions.size
    }
}
