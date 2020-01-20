/*
 * Copyright (C) 2018 SoftBank Robotics Europe
 * See COPYING for the license
 */
package com.softbankrobotics.sample.attentionstatechecker.utils

import com.aldebaran.qi.sdk.`object`.actuation.Frame
import com.aldebaran.qi.sdk.`object`.geometry.Vector3
import io.mockk.every
import io.mockk.mockk
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.TestScheduler
import org.junit.After
import org.junit.Test

import org.junit.Assert.*
import org.junit.Before
import java.util.concurrent.TimeUnit

class FrameUtilsTest {

    private val delta = 0.001
    private val testScheduler = TestScheduler()

    @Before
    fun setUp() {
        RxJavaPlugins.setComputationSchedulerHandler { testScheduler }
        RxJavaPlugins.setIoSchedulerHandler { testScheduler }
    }

    @After
    fun tearDown() {
        RxJavaPlugins.reset()
    }

    @Test
    fun distanceFrom_gives_distance_between_two_frames() {
        val baseFrame = mockk<Frame>()
        val frame = mockk<Frame> {
            every { computeTransform(baseFrame).transform.translation } returns Vector3(3.0, 4.0, 0.0)
        }

        val distance = frame.distanceFrom(baseFrame)

        assertEquals(5.0, distance, delta)
    }

    @Test
    fun distanceObservableFrom_gives_distance_between_two_frames_every_second() {
        val baseFrame = mockk<Frame>()
        val frame = mockk<Frame> {
            every { computeTransform(baseFrame).transform.translation } returns Vector3(3.0, 4.0, 0.0)
        }

        val observable = frame.distanceObservableFrom(baseFrame)
        val observer = observable.subscribeOn(testScheduler).test()

        testScheduler.advanceTimeBy(1L, TimeUnit.SECONDS)
        observer.assertValue(5.0)

        every { frame.computeTransform(baseFrame).transform.translation } returns Vector3(2.0, 0.0, 0.0)

        testScheduler.advanceTimeBy(1L, TimeUnit.SECONDS)
        observer.assertValueAt(1, 2.0)
    }
}