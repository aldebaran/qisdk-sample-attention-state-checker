/*
 * Copyright (C) 2018 SoftBank Robotics Europe
 * See COPYING for the license
 */
package com.softbankrobotics.sample.attentionstatechecker.model.rx.observable

import com.aldebaran.qi.sdk.`object`.human.AttentionState
import com.aldebaran.qi.sdk.`object`.human.Human
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Observable providing the [AttentionState] of a [Human], using the [Human.OnAttentionChangedListener].
 *
 * <br></br>
 *
 * Note: Code inspired from Jake Wharton's [RxBinding](https://github.com/JakeWharton/RxBinding) library
 * to convert listeners into observables.
 */
internal class AttentionStateObservable(private val human: Human) : Observable<AttentionState>() {

    override fun subscribeActual(observer: Observer<in AttentionState>) {
        // Create a listener to subscribe to Human.OnAttentionChangedListener.
        val listener = Listener(human, observer)
        // Link the disposable to the observer subscription.
        observer.onSubscribe(listener)
        // Get current value.
        observer.onNext(human.attention)
        // Subscribe the listener to Human.OnAttentionChangedListener.
        human.addOnAttentionChangedListener(listener)
    }

    private class Listener(private val human: Human, private val observer: Observer<in AttentionState>) : Disposable, Human.OnAttentionChangedListener {
        private val unSubscribed = AtomicBoolean(false)

        override fun onAttentionChanged(attention: AttentionState) {
            if (!isDisposed) {
                observer.onNext(attention)
            }
        }

        override fun dispose() {
            if (unSubscribed.compareAndSet(false, true)) {
                human.removeOnAttentionChangedListener(this)
            }
        }

        override fun isDisposed(): Boolean {
            return unSubscribed.get()
        }
    }
}
