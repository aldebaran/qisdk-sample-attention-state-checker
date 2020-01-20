/*
 * Copyright (C) 2018 SoftBank Robotics Europe
 * See COPYING for the license
 */
package com.softbankrobotics.sample.attentionstatechecker.game

import com.softbankrobotics.sample.attentionstatechecker.model.data.Direction
import java.util.*

/**
 * A directions provider.
 */
internal class DirectionsProvider {

    private val possibleDirections = listOf(Direction.UP, Direction.DOWN, Direction.LEFT, Direction.RIGHT)

    fun provideDirections(): LinkedList<Direction> {
        return LinkedList(possibleDirections.shuffled())
    }
}
