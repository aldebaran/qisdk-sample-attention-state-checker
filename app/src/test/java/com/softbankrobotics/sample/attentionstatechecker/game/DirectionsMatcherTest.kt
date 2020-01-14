package com.softbankrobotics.sample.attentionstatechecker.game

import com.softbankrobotics.sample.attentionstatechecker.model.data.Direction
import com.softbankrobotics.sample.attentionstatechecker.model.data.Direction.*
import org.junit.Assert.*
import org.junit.Test

class DirectionsMatcherTest {

    private val matcher = DirectionsMatcher()

    @Test
    fun matches_UP() {
        assertMatchesOnly(
                UP,
                setOf(UP, UP_LEFT, UP_RIGHT)
        )
    }

    @Test
    fun matches_DOWN() {
        assertMatchesOnly(
                DOWN,
                setOf(DOWN, DOWN_LEFT, DOWN_RIGHT)
        )
    }

    @Test
    fun matches_LEFT() {
        assertMatchesOnly(
                LEFT,
                setOf(LEFT, UP_LEFT, DOWN_LEFT)
        )
    }

    @Test
    fun matches_RIGHT() {
        assertMatchesOnly(
                RIGHT,
                setOf(RIGHT, UP_RIGHT, DOWN_RIGHT)
        )
    }

    @Test
    fun matches_not_cardinal() {
        val allDirections = values()
        val notCardinalDirections = allDirections.filter { it !in setOf(UP, DOWN, LEFT, RIGHT) }
        notCardinalDirections.forEach { notCardinal ->
            allDirections.forEach {
                assertFalse(matcher.matches(notCardinal, it))
            }
        }
    }

    private fun assertMatchesOnly(expected: Direction, matching: Set<Direction>) {
        val notMatching = values().filter { it !in matching }

        matching.forEach {
            assertTrue(matcher.matches(expected, it))
        }
        notMatching.forEach {
            assertFalse(matcher.matches(expected, it))
        }
    }
}