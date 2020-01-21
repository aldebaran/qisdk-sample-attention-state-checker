/*
 * Copyright (C) 2018 SoftBank Robotics Europe
 * See COPYING for the license
 */
package com.softbankrobotics.sample.attentionstatechecker.game

import com.softbankrobotics.sample.attentionstatechecker.game.GameEvent.FocusGained
import com.softbankrobotics.sample.attentionstatechecker.game.GameEvent.FocusLost
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test

class GameRobotTest {

    private val gameMachine = mockk<GameMachine>(relaxed = true)
    private val gameRobot = GameRobot(gameMachine)

    @Test
    fun posts_FocusGained_when_focus_is_gained() {
        gameRobot.onRobotFocusGained(mockk())

        verify { gameMachine.postEvent(FocusGained) }
    }

    @Test
    fun posts_FocusLost_when_focus_is_lost() {
        gameRobot.onRobotFocusLost()

        verify { gameMachine.postEvent(FocusLost) }
    }
}
