/*
 * Copyright (C) 2018 SoftBank Robotics Europe
 * See COPYING for the license
 */
package com.softbankrobotics.sample.attentionstatechecker.game

import com.softbankrobotics.sample.attentionstatechecker.model.data.Direction

/**
 * A game state.
 */
internal sealed class GameState {
    object Idle : GameState()
    object Briefing : GameState()
    data class Instructions(val expectedDirection: Direction, val matched: Int, val total: Int) : GameState()
    data class Playing(val expectedDirection: Direction, val matched: Int, val total: Int) : GameState()
    data class NotMatching(val expectedDirection: Direction, val lookDirection: Direction, val consecutiveErrors: Int, val matched: Int, val total: Int) : GameState()
    data class Matching(val matchingDirection: Direction, val matched: Int, val total: Int) : GameState()
    object Win : GameState()
    object End : GameState()
    data class Stopping(val matched: Int, val total: Int) : GameState()
}
