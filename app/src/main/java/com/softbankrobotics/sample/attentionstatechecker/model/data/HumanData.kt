/*
 * Copyright (C) 2018 SoftBank Robotics Europe
 * See COPYING for the license
 */
package com.softbankrobotics.sample.attentionstatechecker.model.data

import com.aldebaran.qi.sdk.`object`.human.AttentionState
import com.aldebaran.qi.sdk.`object`.human.Human

/**
 * Hold a [Human] data.
 */
data class HumanData(val human: Human, val attentionState: AttentionState, val distance: Double)
