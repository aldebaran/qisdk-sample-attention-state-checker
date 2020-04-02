/*
 * Copyright (C) 2018 SoftBank Robotics Europe
 * See COPYING for the license
 */
package com.softbankrobotics.sample.attentionstatechecker.game

import com.softbankrobotics.sample.attentionstatechecker.model.data.Direction.*
import org.junit.Assert.*
import org.junit.Test

/**
 * Tests for [DirectionsProvider].
 */
class DirectionsProviderTest {

    // System under tests.
    private val directionsProvider = DirectionsProvider()

    @Test
    fun provides_4_directions() {
        val directions = directionsProvider.provideDirections()
        assertEquals(4, directions.size)
    }

    @Test
    fun provides_all_cardinal_directions() {
        val directions = directionsProvider.provideDirections()
        assertTrue(directions.containsAll(setOf(UP, DOWN, LEFT, RIGHT)))
    }
}
