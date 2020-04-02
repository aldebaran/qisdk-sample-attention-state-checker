/*
 * Copyright (C) 2018 SoftBank Robotics Europe
 * See COPYING for the license
 */
package com.softbankrobotics.sample.attentionstatechecker.game

import com.softbankrobotics.sample.attentionstatechecker.model.data.Direction
import com.softbankrobotics.sample.attentionstatechecker.model.data.Direction.*
import org.junit.Assert.*
import org.junit.Test

/**
 * Tests for [DirectionsMatcher].
 */
class DirectionsMatcherTest {

    // System under tests.
    private val matcher = DirectionsMatcher()

    @Test
    fun matches_for_UP() {
        assertMatchesOnly(
                UP,
                setOf(UP, UP_LEFT, UP_RIGHT)
        )
    }

    @Test
    fun matches_for_DOWN() {
        assertMatchesOnly(
                DOWN,
                setOf(DOWN, DOWN_LEFT, DOWN_RIGHT)
        )
    }

    @Test
    fun matches_for_LEFT() {
        assertMatchesOnly(
                LEFT,
                setOf(LEFT, UP_LEFT, DOWN_LEFT)
        )
    }

    @Test
    fun matches_for_RIGHT() {
        assertMatchesOnly(
                RIGHT,
                setOf(RIGHT, UP_RIGHT, DOWN_RIGHT)
        )
    }

    @Test
    fun matches_gives_false_for_not_cardinal_directions() {
        val allDirections = values()
        val notCardinalDirections = allDirections.filter { it !in setOf(UP, DOWN, LEFT, RIGHT) }
        notCardinalDirections.forEach { notCardinal ->
            allDirections.forEach {
                assertFalse(matcher.matches(notCardinal, it))
            }
        }
    }

    /**
     * Assert that the specified [direction] matches only with the [matching] set of directions.
     */
    private fun assertMatchesOnly(direction: Direction, matching: Set<Direction>) {
        val notMatching = values().filter { it !in matching }

        matching.forEach {
            assertTrue(matcher.matches(direction, it))
        }
        notMatching.forEach {
            assertFalse(matcher.matches(direction, it))
        }
    }
}