/*
 * Copyright (C) 2018 SoftBank Robotics Europe
 * See COPYING for the license
 */
package com.softbankrobotics.sample.attentionstatechecker.game

import com.softbankrobotics.sample.attentionstatechecker.model.data.Direction

/**
 * A game event.
 */
internal sealed class GameEvent {
    object FocusGained : GameEvent()
    object FocusLost : GameEvent()
    object BriefingFinished : GameEvent()
    object InstructionsFinished : GameEvent()
    object Match : GameEvent()
    data class NotMatch(val lookDirection: Direction) : GameEvent()
    object MatchingFinished : GameEvent()
    object NotMatchingFinished : GameEvent()
    object WinFinished : GameEvent()
    object Stop : GameEvent()
    object Stopped : GameEvent()
}
