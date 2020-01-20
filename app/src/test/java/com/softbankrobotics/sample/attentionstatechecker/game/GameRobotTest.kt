/*
 * Copyright (C) 2018 SoftBank Robotics Europe
 * See COPYING for the license
 */
package com.softbankrobotics.sample.attentionstatechecker.game

import com.softbankrobotics.sample.attentionstatechecker.game.GameEvent.*
import com.softbankrobotics.sample.attentionstatechecker.game.GameState.*
import com.softbankrobotics.sample.attentionstatechecker.model.data.Direction.*
import com.softbankrobotics.sample.attentionstatechecker.utils.directionObservable
import io.mockk.*
import io.reactivex.Observable
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import org.junit.After
import org.junit.Before
import org.junit.Test

class GameRobotTest {

    private companion object {
        const val DIRECTION_UTILS_CLASSNAME = "com.softbankrobotics.sample.attentionstatechecker.utils.DirectionUtils"
    }

    private val gameMachine = mockk<GameMachine>(relaxed = true)
    private val gameRobot = GameRobot(gameMachine)

    @Before
    fun setUp() {
        RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
        mockkStatic(DIRECTION_UTILS_CLASSNAME)
    }

    @After
    fun tearDown() {
        RxJavaPlugins.reset()
        unmockkStatic(DIRECTION_UTILS_CLASSNAME)
    }

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

    @Test
    fun posts_Match_when_direction_is_matching() {
        val lookDirection = UP
        every { directionObservable(any()) } returns Observable.just(lookDirection)

        val expectedDirection = UP
        val playing = Playing(expectedDirection, mockk(relaxed = true), mockk(relaxed = true), mockk(relaxed = true))
        machineStateIs(playing)

        gameRobot.onRobotFocusGained(mockk())

        verify { gameMachine.postEvent(Match) }
    }

    @Test
    fun posts_NotMatch_when_direction_is_not_matching() {
        val lookDirection = DOWN
        every { directionObservable(any()) } returns Observable.just(lookDirection)

        val expectedDirection = UP
        val playing = Playing(expectedDirection, mockk(relaxed = true), mockk(relaxed = true), mockk(relaxed = true))
        machineStateIs(playing)

        gameRobot.onRobotFocusGained(mockk())

        verify { gameMachine.postEvent(NotMatch(lookDirection)) }
    }

    private fun machineStateIs(gameState: GameState) {
        every { gameMachine.gameState() } returns Observable.just(gameState)
    }
}
