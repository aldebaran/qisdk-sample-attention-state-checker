package com.softbankrobotics.sample.attentionstatechecker.game

import com.softbankrobotics.sample.attentionstatechecker.model.data.Direction.*
import org.junit.Assert.*
import org.junit.Test

class DirectionsProviderTest {

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
