/*
 * Copyright (C) 2018 SoftBank Robotics Europe
 * See COPYING for the license
 */
package com.softbankrobotics.sample.attentionstatechecker

import io.reactivex.observers.TestObserver

fun <T> TestObserver<T>.assertLastValueIs(value: T) {
    assertValueAt(valueCount() - 1, value)
}
