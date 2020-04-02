/*
 * Copyright (C) 2018 SoftBank Robotics Europe
 * See COPYING for the license
 */
package com.softbankrobotics.sample.attentionstatechecker.model.rx.operator

import com.aldebaran.qi.sdk.`object`.human.AttentionState
import com.softbankrobotics.sample.attentionstatechecker.model.data.Direction
import com.softbankrobotics.sample.attentionstatechecker.model.data.HumanData
import com.softbankrobotics.sample.attentionstatechecker.model.data.Wrapper
import io.mockk.mockk
import io.reactivex.Observable
import org.junit.Test

/**
 * Tests for RxJava operators in MyOperators.kt.
 */
class MyOperatorsTest {

    @Test
    fun closest_gives_empty_wrapper_when_no_HumanData() {
        val observable = Observable.just(emptyList<HumanData>())
        observable.closest()
                .test()
                .assertValue(Wrapper.empty())
    }

    @Test
    fun closest_gives_closest_HumanData_if_any() {
        val closestHumanData = HumanData(mockk(), mockk(), 12.0)
        val otherHumanData = HumanData(mockk(), mockk(), 42.0)
        val observable = Observable.just(listOf(closestHumanData, otherHumanData))
        observable.closest()
                .test()
                .assertValue(Wrapper.of(closestHumanData))
    }

    @Test
    fun attentionState_gives_empty_wrapper_when_no_HumanData() {
        val observable = Observable.just(Wrapper.empty())
        observable.attentionState()
                .test()
                .assertValue(Wrapper.empty())
    }

    @Test
    fun attentionState_gives_attention_state_if_any() {
        val attentionState = AttentionState.LOOKING_AT_ROBOT
        val humanData = HumanData(mockk(), attentionState, mockk(relaxed = true))
        val observable = Observable.just(Wrapper.of(humanData))
        observable.attentionState()
                .test()
                .assertValue(Wrapper.of(attentionState))
    }

    @Test
    fun direction_gives_UNKNOWN_when_no_AttentionState() {
        val observable = Observable.just(Wrapper.empty())
        observable.direction()
                .test()
                .assertValue(Direction.UNKNOWN)
    }

    @Test
    fun direction_gives_UNKNOWN_when_UNKNOWN() {
        val observable = Observable.just(Wrapper.of(AttentionState.UNKNOWN))
        observable.direction()
                .test()
                .assertValue(Direction.UNKNOWN)
    }

    @Test
    fun direction_gives_UNKNOWN_when_LOOKING_AT_ROBOT() {
        val observable = Observable.just(Wrapper.of(AttentionState.LOOKING_AT_ROBOT))
        observable.direction()
                .test()
                .assertValue(Direction.UNKNOWN)
    }

    @Test
    fun direction_maps_AttentionState_to_Direction_for_cardinal_and_intercardinal() {
        val conversionMap = mapOf(
                AttentionState.LOOKING_UP to Direction.UP,
                AttentionState.LOOKING_UP_LEFT to Direction.UP_LEFT,
                AttentionState.LOOKING_LEFT to Direction.LEFT,
                AttentionState.LOOKING_DOWN_LEFT to Direction.DOWN_LEFT,
                AttentionState.LOOKING_DOWN to Direction.DOWN,
                AttentionState.LOOKING_DOWN_RIGHT to Direction.DOWN_RIGHT,
                AttentionState.LOOKING_RIGHT to Direction.RIGHT,
                AttentionState.LOOKING_UP_RIGHT to Direction.UP_RIGHT
        )

        conversionMap.forEach { (attentionState, direction) ->
            val observable = Observable.just(Wrapper.of(attentionState))
            observable.direction()
                    .test()
                    .assertValue(direction)
        }
    }
}