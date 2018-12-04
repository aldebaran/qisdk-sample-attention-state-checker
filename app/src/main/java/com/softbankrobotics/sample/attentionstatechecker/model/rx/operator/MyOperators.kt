/*
 * Copyright (C) 2018 SoftBank Robotics Europe
 * See COPYING for the licence
 */
@file:JvmName("MyOperators")

package com.softbankrobotics.sample.attentionstatechecker.model.rx.operator

import com.aldebaran.qi.sdk.`object`.human.AttentionState
import com.aldebaran.qi.sdk.`object`.human.AttentionState.*
import com.softbankrobotics.sample.attentionstatechecker.model.data.Direction
import com.softbankrobotics.sample.attentionstatechecker.model.data.Direction.*
import com.softbankrobotics.sample.attentionstatechecker.model.data.HumanData
import com.softbankrobotics.sample.attentionstatechecker.model.data.Wrapper
import io.reactivex.Observable

fun Observable<List<HumanData>>.closest(): Observable<Wrapper<HumanData>> {
    return map { humanDataList ->
        val min = humanDataList.minBy { it.distance }

        if (min != null) {
            Wrapper.of(min)
        } else {
            Wrapper.empty()
        }
    }
}

fun Observable<Wrapper<HumanData>>.attentionState(): Observable<Wrapper<AttentionState>> {
    return map { wrapper ->
        if (wrapper.content != null) {
            Wrapper.of(wrapper.content.attentionState)
        } else {
            Wrapper.empty()
        }
    }
}

fun Observable<Wrapper<AttentionState>>.direction(): Observable<Direction> {
    return map { wrapper ->
        if (wrapper.content == null) {
            return@map Direction.UNKNOWN
        }

        val attentionState = wrapper.content

        when (attentionState) {
            LOOKING_UP -> UP
            LOOKING_DOWN -> DOWN
            LOOKING_LEFT -> LEFT
            LOOKING_RIGHT -> RIGHT
            LOOKING_UP_LEFT -> UP_LEFT
            LOOKING_UP_RIGHT -> UP_RIGHT
            LOOKING_DOWN_LEFT -> DOWN_LEFT
            LOOKING_DOWN_RIGHT -> DOWN_RIGHT
            else -> Direction.UNKNOWN
        }
    }
}
