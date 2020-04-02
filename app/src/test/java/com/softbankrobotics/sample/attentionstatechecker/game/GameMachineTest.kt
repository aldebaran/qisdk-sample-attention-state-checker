/*
 * Copyright (C) 2018 SoftBank Robotics Europe
 * See COPYING for the license
 */
package com.softbankrobotics.sample.attentionstatechecker.game

import com.softbankrobotics.sample.attentionstatechecker.assertLastValueIs
import com.softbankrobotics.sample.attentionstatechecker.game.GameEvent.*
import com.softbankrobotics.sample.attentionstatechecker.game.GameState.*
import com.softbankrobotics.sample.attentionstatechecker.model.data.Direction.*
import io.mockk.every
import io.mockk.mockk
import org.junit.Test
import java.util.*

/**
 * Tests for [GameMachine].
 */
class GameMachineTest {

    // Mocked dependency.
    private val directionsProvider = mockk<DirectionsProvider> {
        every { provideDirections() } returns LinkedList(listOf(UP, DOWN, LEFT, RIGHT))
    }
    // System under tests.
    private val machine = GameMachine(directionsProvider)
    // TestObserver, observing the game state.
    private val observer = machine.gameState().test()

    @Test
    fun initial_state_is_Idle() {
        observer.assertValue(Idle)
    }

    @Test
    fun Idle_then_FocusGained_gives_Briefing() {
        machine.postEvent(FocusGained)
        observer.assertLastValueIs(Briefing)
    }

    @Test
    fun FocusLost_gives_Idle() {
        machine.postAll(
                FocusGained,
                FocusLost
        )
        observer.assertLastValueIs(Idle)
    }

    @Test
    fun Briefing_then_BriefingFinished_gives_Instructions_when_directions_is_not_empty() {
        machine.postAll(
                FocusGained,
                BriefingFinished
        )
        observer.assertLastValueIs(Instructions(UP, 0, 0, 4))
    }

    @Test
    fun Briefing_then_BriefingFinished_gives_Win_when_directions_is_empty() {
        every { directionsProvider.provideDirections() } returns LinkedList()

        machine.postAll(
                FocusGained,
                BriefingFinished
        )
        observer.assertLastValueIs(Win)
    }

    @Test
    fun Instructions_then_InstructionsFinished_gives_Playing() {
        machine.postAll(
                FocusGained,
                BriefingFinished,
                InstructionsFinished
        )
        observer.assertLastValueIs(Playing(UP, 0, 0, 4))
    }

    @Test
    fun Playing_then_Match_gives_Matching() {
        machine.postAll(
                FocusGained,
                BriefingFinished,
                InstructionsFinished,
                Match
        )
        observer.assertLastValueIs(Matching(UP, 1, 4))
    }

    @Test
    fun Playing_then_NotMatch_gives_NotMatching() {
        machine.postAll(
                FocusGained,
                BriefingFinished,
                InstructionsFinished,
                NotMatch(DOWN)
        )
        observer.assertLastValueIs(NotMatching(UP, DOWN, 1, 0, 4))
    }

    @Test
    fun Matching_then_MatchingFinished_gives_Instructions() {
        machine.postAll(
                FocusGained,
                BriefingFinished,
                InstructionsFinished,
                Match,
                MatchingFinished
        )
        observer.assertLastValueIs(Instructions(DOWN, 0, 1, 4))
    }

    @Test
    fun four_Matching_then_MatchingFinished_gives_Win() {
        machine.postAll(
                FocusGained,
                BriefingFinished,
                InstructionsFinished,
                Match,
                MatchingFinished,
                InstructionsFinished,
                Match,
                MatchingFinished,
                InstructionsFinished,
                Match,
                MatchingFinished,
                InstructionsFinished,
                Match,
                MatchingFinished
        )
        observer.assertLastValueIs(Win)
    }

    @Test
    fun NotMatching_then_NotMatchingFinished_gives_Instructions() {
        machine.postAll(
                FocusGained,
                BriefingFinished,
                InstructionsFinished,
                NotMatch(DOWN),
                NotMatchingFinished
        )
        observer.assertLastValueIs(Instructions(UP, 1, 0, 4))
    }

    @Test
    fun Win_then_WinFinished_gives_End() {
        machine.postAll(
                FocusGained,
                BriefingFinished,
                InstructionsFinished,
                Match,
                MatchingFinished,
                InstructionsFinished,
                Match,
                MatchingFinished,
                InstructionsFinished,
                Match,
                MatchingFinished,
                InstructionsFinished,
                Match,
                MatchingFinished,
                WinFinished
        )
        observer.assertLastValueIs(End)
    }

    @Test
    fun Stop_gives_Stopping() {
        machine.postAll(
                FocusGained,
                BriefingFinished,
                Stop
        )
        observer.assertLastValueIs(Stopping)
    }

    @Test
    fun Stopping_then_Stopped_gives_End() {
        machine.postAll(
                FocusGained,
                BriefingFinished,
                Stop,
                Stopped
        )
        observer.assertLastValueIs(End)
    }

    @Test
    fun invalid_transition_is_ignored() {
        machine.postAll(
                FocusGained,
                FocusGained
        )
        observer.assertLastValueIs(Briefing)
    }

    /**
     * Convenience method to post all [events], one after another.
     */
    private fun GameMachine.postAll(vararg events: GameEvent) {
        events.forEach { postEvent(it) }
    }
}