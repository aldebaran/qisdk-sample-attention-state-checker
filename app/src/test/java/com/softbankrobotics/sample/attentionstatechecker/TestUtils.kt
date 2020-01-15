package com.softbankrobotics.sample.attentionstatechecker

import io.reactivex.observers.TestObserver

fun <T> TestObserver<T>.assertLastValueIs(value: T) {
    assertValueAt(valueCount() - 1, value)
}

fun <T> TestObserver<T>.assertLastValue(predicate: (T) -> Boolean) {
    assertValueAt(valueCount() - 1, predicate)
}