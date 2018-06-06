package com.softbankrobotics.sample.attentionstatechecker.game

import com.softbankrobotics.sample.attentionstatechecker.model.data.Direction

/**
 * A game state.
 */
internal sealed class GameState {

    object Idle : GameState()
    object Intro : GameState()
    data class Instructions(val expectedDirection: Direction) : GameState()
    data class Playing(val expectedDirection: Direction) : GameState()
    data class NotMatching(val expectedDirection: Direction, val lookDirection: Direction) : GameState()
    data class Matching(val matchingDirection: Direction) : GameState()
}
