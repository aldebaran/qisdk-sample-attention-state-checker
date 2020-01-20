/*
 * Copyright (C) 2018 SoftBank Robotics Europe
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

    fun gameState(): Observable<GameState> = subject

    fun postEvent(gameEvent: GameEvent) {
        val currentState = subject.value
                ?: throw IllegalStateException("GameMachine must have a GameState to be able to handle a GameEvent.")
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
                directions = LinkedList(listOf(Direction.UP, Direction.DOWN, Direction.LEFT, Direction.RIGHT).shuffled())
                totalDirections = directions.size
                return if (directions.isNotEmpty()) {
                    GameState.Instructions(directions.poll(), 0, 0, totalDirections)
                } else {
                    GameState.Win
                }
            }
            is GameEvent.InstructionsFinished -> if (currentState is GameState.Instructions)
                return GameState.Playing(currentState.expectedDirection, currentState.consecutiveErrors, currentState.matched, totalDirections)
            is GameEvent.Match -> if (currentState is GameState.Playing) {
                return GameState.Matching(currentState.expectedDirection, currentState.matched + 1, totalDirections)
            }
            is GameEvent.NotMatch -> if (currentState is GameState.Playing) {
                return GameState.NotMatching(currentState.expectedDirection, gameEvent.lookDirection, currentState.consecutiveErrors + 1, currentState.matched, totalDirections)
            }
            is GameEvent.MatchingFinished -> if (currentState is GameState.Matching)
                return if (directions.isNotEmpty()) {
                    GameState.Instructions(directions.poll(), 0, currentState.matched, totalDirections)
                } else {
                    GameState.Win
                }
            is GameEvent.NotMatchingFinished -> if (currentState is GameState.NotMatching)
                return GameState.Instructions(currentState.expectedDirection, currentState.consecutiveErrors, currentState.matched, totalDirections)
            is GameEvent.WinFinished -> if (currentState === GameState.Win)
                return GameState.End
            is GameEvent.Stop ->
                return GameState.Stopping
            is GameEvent.Stopped -> if (currentState is GameState.Stopping)
                return GameState.End
        }

        return currentState
    }
}
