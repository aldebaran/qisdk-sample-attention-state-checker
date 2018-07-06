/*
 * Copyright (C) 2018 Softbank Robotics Europe
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
    data class Instructions(val expectedDirection: Direction) : GameState()
    data class Playing(val expectedDirection: Direction) : GameState()
    data class NotMatching(val expectedDirection: Direction, val lookDirection: Direction) : GameState()
    data class Matching(val matchingDirection: Direction) : GameState()
    object Win : GameState()
    object End : GameState()
}
