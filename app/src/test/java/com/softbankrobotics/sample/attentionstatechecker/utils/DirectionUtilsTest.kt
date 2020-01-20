/*
 * Copyright (C) 2018 SoftBank Robotics Europe
 * See COPYING for the license
 */
package com.softbankrobotics.sample.attentionstatechecker.utils

import com.aldebaran.qi.sdk.QiContext
import com.aldebaran.qi.sdk.`object`.human.AttentionState
import com.softbankrobotics.sample.attentionstatechecker.model.data.Direction
import com.softbankrobotics.sample.attentionstatechecker.model.data.HumanData
import com.softbankrobotics.sample.attentionstatechecker.model.rx.observable.humanDataListObservable
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.reactivex.Observable
import org.junit.After
import org.junit.Before
import org.junit.Test

class DirectionUtilsTest {

    private companion object {
        const val MY_OBSERVABLES_CLASSNAME = "com.softbankrobotics.sample.attentionstatechecker.model.rx.observable.MyObservables"
    }

    @Before
    fun setUp() {
        mockkStatic(MY_OBSERVABLES_CLASSNAME)
    }

    @After
    fun tearDown() {
        unmockkStatic(MY_OBSERVABLES_CLASSNAME)
    }

    @Test
    fun directionObservable_provides_direction_of_closest_human() {
        val qiContext = mockk<QiContext>(relaxed = true)
        val closestHumanData = HumanData(mockk(), AttentionState.LOOKING_UP, 12.0)
        val otherHumanData = HumanData(mockk(), AttentionState.LOOKING_DOWN, 42.0)
        every { humanDataListObservable(qiContext) } returns Observable.just(listOf(closestHumanData, otherHumanData))

        directionObservable(qiContext)
                .test()
                .assertValue(Direction.UP)
    }

    @Test
    fun directionObservable_does_not_emit_same_value_consecutively() {
        val qiContext = mockk<QiContext>(relaxed = true)
        val humanData1 = HumanData(mockk(), AttentionState.LOOKING_UP, 12.0)
        val humanData2 = HumanData(mockk(), AttentionState.LOOKING_UP, 42.0)
        every { humanDataListObservable(qiContext) } returns Observable.just(listOf(humanData1), listOf(humanData2))

        directionObservable(qiContext)
                .test()
                .assertValue(Direction.UP)
    }

    @Test
    fun directionObservable_filters_UNKNOWN() {
        val qiContext = mockk<QiContext>(relaxed = true)
        val humanData = HumanData(mockk(), AttentionState.UNKNOWN, 12.0)
        every { humanDataListObservable(qiContext) } returns Observable.just(listOf(humanData))

        directionObservable(qiContext)
                .test()
                .assertNoValues()
    }
}