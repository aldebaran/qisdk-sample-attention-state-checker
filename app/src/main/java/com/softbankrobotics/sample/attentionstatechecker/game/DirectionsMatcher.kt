/*
 * Copyright (C) 2018 Softbank Robotics Europe
 * See COPYING for the license
 */

package com.softbankrobotics.sample.attentionstatechecker.game

import com.softbankrobotics.sample.attentionstatechecker.model.data.Direction
import com.softbankrobotics.sample.attentionstatechecker.model.data.Direction.*

/**
 * A [Direction] matcher.
 */
internal class DirectionsMatcher {

    /**
     * Returns `true` if the lookDirection matches the expectedDirection.
     * @param expectedDirection The expected direction
     * @param lookDirection The look direction
     * @return Returns `true` if the lookDirection matches the expectedDirection.
     */
    fun matches(expectedDirection: Direction, lookDirection: Direction) =
            when (expectedDirection) {
                UP -> lookDirection in setOf(UP, UP_LEFT, UP_RIGHT)
                DOWN -> lookDirection in setOf(DOWN, DOWN_LEFT, DOWN_RIGHT)
                LEFT -> lookDirection in setOf(LEFT, UP_LEFT, DOWN_LEFT)
                RIGHT -> lookDirection in setOf(RIGHT, UP_RIGHT, DOWN_RIGHT)
                else -> false
            }
}
