package com.softbankrobotics.sample.attentionstatechecker.game

import com.softbankrobotics.sample.attentionstatechecker.assertLastValue
import com.softbankrobotics.sample.attentionstatechecker.assertLastValueIs
import com.softbankrobotics.sample.attentionstatechecker.game.GameEvent.*
import com.softbankrobotics.sample.attentionstatechecker.game.GameState.*
import com.softbankrobotics.sample.attentionstatechecker.model.data.Direction.*
import org.junit.Test

class GameMachineTest {

    private val machine = GameMachine()
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
    fun Briefing_then_BriefingFinished_gives_Instructions() {
        machine.postAll(
                FocusGained,
                BriefingFinished
        )
        observer.assertLastValue {
            return@assertLastValue (it is Instructions) &&
                    (it.matched == 0) &&
                    (it.total == 4)
        }
    }

    @Test
    fun Instructions_then_InstructionsFinished_gives_Playing() {
        machine.postAll(
                FocusGained,
                BriefingFinished,
                InstructionsFinished
        )
        observer.assertLastValue {
            return@assertLastValue (it is Playing) &&
                    (it.matched == 0) &&
                    (it.total == 4)
        }
    }

    @Test
    fun Playing_then_Match_gives_Matching() {
        machine.postAll(
                FocusGained,
                BriefingFinished,
                InstructionsFinished,
                Match
        )
        observer.assertLastValue {
            return@assertLastValue (it is Matching) &&
                    (it.matched == 1) &&
                    (it.total == 4)
        }
    }

    @Test
    fun Playing_then_NotMatch_gives_NotMatching() {
        machine.postAll(
                FocusGained,
                BriefingFinished,
                InstructionsFinished,
                NotMatch(UP)
        )
        observer.assertLastValue {
            return@assertLastValue (it is NotMatching) &&
                    (it.lookDirection == UP) &&
                    (it.consecutiveErrors == 1) &&
                    (it.matched == 0) &&
                    (it.total == 4)
        }
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
        observer.assertLastValue {
            return@assertLastValue (it is Instructions) &&
                    (it.matched == 1) &&
                    (it.total == 4)
        }
    }

    @Test
    fun NotMatching_then_NotMatchingFinished_gives_Instructions() {
        machine.postAll(
                FocusGained,
                BriefingFinished,
                InstructionsFinished,
                NotMatch(UP),
                NotMatchingFinished
        )
        observer.assertLastValue {
            return@assertLastValue (it is Instructions) &&
                    (it.matched == 0) &&
                    (it.total == 4)
        }
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

    private fun GameMachine.postAll(vararg events: GameEvent) {
        events.forEach { postEvent(it) }
    }
}