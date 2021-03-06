/*
 * Copyright (C) 2018 SoftBank Robotics Europe
 * See COPYING for the license
 */
package com.softbankrobotics.sample.attentionstatechecker.model.rx.observable

import com.aldebaran.qi.sdk.QiContext
import com.aldebaran.qi.sdk.`object`.actuation.Actuation
import com.aldebaran.qi.sdk.`object`.actuation.Frame
import com.aldebaran.qi.sdk.`object`.human.AttentionState
import com.aldebaran.qi.sdk.`object`.human.Human
import com.aldebaran.qi.sdk.`object`.humanawareness.HumanAwareness
import com.softbankrobotics.sample.attentionstatechecker.assertLastValueIs
import com.softbankrobotics.sample.attentionstatechecker.model.data.HumanData
import com.softbankrobotics.sample.attentionstatechecker.utils.distanceFrom
import io.mockk.*
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.TestScheduler
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit

/**
 * Tests for [humanDataListObservable].
 */
class MyObservablesTest {

    // Used to control time.
    private val testScheduler = TestScheduler()

    private companion object {
        // Used to mock methods in this file.
        const val FRAME_UTILS_CLASSNAME = "com.softbankrobotics.sample.attentionstatechecker.utils.FrameUtils"
    }

    @Before
    fun setUp() {
        // Set all RxJava schedulers to testScheduler.
        RxJavaPlugins.setComputationSchedulerHandler { testScheduler }
        RxJavaPlugins.setIoSchedulerHandler { testScheduler }
        mockkStatic(FRAME_UTILS_CLASSNAME)
    }

    @After
    fun tearDown() {
        RxJavaPlugins.reset()
        unmockkStatic(FRAME_UTILS_CLASSNAME)
    }

    @Test
    fun humanDataListObservable_provides_list_of_human_data() {
        val robotFrame = mockk<Frame>(relaxed = true)
        val actuation = mockk<Actuation>(relaxed = true) {
            every { robotFrame() } returns robotFrame
        }
        val attentionState = AttentionState.LOOKING_UP
        val distance = 5.0
        val human = mockk<Human>(relaxed = true) {
            every { attention } returns attentionState
            every { headFrame.distanceFrom(robotFrame) } returns distance
        }
        val humanAwareness = mockk<HumanAwareness>(relaxed = true) {
            every { humansAround } returns listOf(human)
        }
        val qiContext = mockk<QiContext>(relaxed = true) {
            every { this@mockk.humanAwareness } returns humanAwareness
            every { this@mockk.actuation } returns actuation
        }

        val observable = humanDataListObservable(qiContext)
        val observer = observable.subscribeOn(testScheduler).test()

        // Wait for 2 seconds, because humanDataListObservable waits for humans list stabilisation for 1 second,
        // and distanceObservableFrom begins emitting after 1 second.
        testScheduler.advanceTimeBy(2L, TimeUnit.SECONDS)

        observer.assertValue(listOf(HumanData(human, attentionState, distance)))
    }

    @Test
    fun humanDataListObservable_notifies_when_humans_list_changes() {
        val listenerSlot = slot<HumanAwareness.OnHumansAroundChangedListener>()

        val robotFrame = mockk<Frame>(relaxed = true)
        val actuation = mockk<Actuation>(relaxed = true) {
            every { robotFrame() } returns robotFrame
        }
        val attentionState = AttentionState.LOOKING_UP
        val distance = 5.0
        val human = mockk<Human>(relaxed = true) {
            every { attention } returns attentionState
            every { headFrame.distanceFrom(robotFrame) } returns distance
        }
        val humanAwareness = mockk<HumanAwareness>(relaxed = true) {
            every { humansAround } returns listOf(human)
            every { addOnHumansAroundChangedListener(capture(listenerSlot)) } just runs
        }
        val qiContext = mockk<QiContext>(relaxed = true) {
            every { this@mockk.humanAwareness } returns humanAwareness
            every { this@mockk.actuation } returns actuation
        }

        val observable = humanDataListObservable(qiContext)
        val observer = observable.subscribeOn(testScheduler).test()

        // Wait for 2 seconds, because humanDataListObservable waits for humans list stabilisation for 1 second,
        // and distanceObservableFrom begins emitting after 1 second.
        testScheduler.advanceTimeBy(2L, TimeUnit.SECONDS)

        // Call the captured listener.
        listenerSlot.captured.onHumansAroundChanged(emptyList())

        // Wait for 1 second (humans list stabilisation)
        testScheduler.advanceTimeBy(1L, TimeUnit.SECONDS)

        observer.assertLastValueIs(emptyList())
    }

    @Test
    fun humanDataListObservable_notifies_when_human_attention_changes() {
        val listenerSlot = slot<Human.OnAttentionChangedListener>()

        val robotFrame = mockk<Frame>(relaxed = true)
        val actuation = mockk<Actuation>(relaxed = true) {
            every { robotFrame() } returns robotFrame
        }
        val attentionState = AttentionState.LOOKING_UP
        val distance = 5.0
        val human = mockk<Human>(relaxed = true) {
            every { attention } returns attentionState
            every { headFrame.distanceFrom(robotFrame) } returns distance
            every { addOnAttentionChangedListener(capture(listenerSlot)) } just runs
        }
        val humanAwareness = mockk<HumanAwareness>(relaxed = true) {
            every { humansAround } returns listOf(human)
        }
        val qiContext = mockk<QiContext>(relaxed = true) {
            every { this@mockk.humanAwareness } returns humanAwareness
            every { this@mockk.actuation } returns actuation
        }

        val observable = humanDataListObservable(qiContext)
        val observer = observable.subscribeOn(testScheduler).test()

        // Wait for 2 seconds, because humanDataListObservable waits for humans list stabilisation for 1 second,
        // and distanceObservableFrom begins emitting after 1 second.
        testScheduler.advanceTimeBy(2L, TimeUnit.SECONDS)

        val newAttentionState = AttentionState.LOOKING_DOWN
        // Call the captured listener.
        listenerSlot.captured.onAttentionChanged(newAttentionState)

        observer.assertLastValueIs(listOf(HumanData(human, newAttentionState, distance)))
    }

    @Test
    fun humanDataListObservable_notifies_when_human_distance_changes() {
        val robotFrame = mockk<Frame>(relaxed = true)
        val actuation = mockk<Actuation>(relaxed = true) {
            every { robotFrame() } returns robotFrame
        }
        val attentionState = AttentionState.LOOKING_UP
        val distance = 5.0
        val human = mockk<Human>(relaxed = true) {
            every { attention } returns attentionState
            every { headFrame.distanceFrom(robotFrame) } returns distance
        }
        val humanAwareness = mockk<HumanAwareness>(relaxed = true) {
            every { humansAround } returns listOf(human)
        }
        val qiContext = mockk<QiContext>(relaxed = true) {
            every { this@mockk.humanAwareness } returns humanAwareness
            every { this@mockk.actuation } returns actuation
        }

        val observable = humanDataListObservable(qiContext)
        val observer = observable.subscribeOn(testScheduler).test()

        // Wait for 2 seconds, because humanDataListObservable waits for humans list stabilisation for 1 second,
        // and distanceObservableFrom begins emitting after 1 second.
        testScheduler.advanceTimeBy(2L, TimeUnit.SECONDS)

        val newDistance = 3.0
        every { human.headFrame.distanceFrom(robotFrame) } returns newDistance

        // Wait for 1 second (distance emission time)
        testScheduler.advanceTimeBy(1L, TimeUnit.SECONDS)

        observer.assertLastValueIs(listOf(HumanData(human, attentionState, newDistance)))
    }
}