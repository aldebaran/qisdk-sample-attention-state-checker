/*
 * Copyright (C) 2018 SoftBank Robotics Europe
 * See COPYING for the license
 */
package com.softbankrobotics.sample.attentionstatechecker.model.rx.observable

import com.aldebaran.qi.sdk.`object`.human.Human
import com.aldebaran.qi.sdk.`object`.humanawareness.HumanAwareness
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Observable providing the list of humans from [HumanAwareness], using the [HumanAwareness.OnHumansAroundChangedListener].
 *
 * <br></br>
 *
 * Note: Code inspired from Jake Wharton's [RxBinding](https://github.com/JakeWharton/RxBinding) library
 * to convert listeners into observables.
 */
internal class HumansAroundObservable(private val humanAwareness: HumanAwareness) : Observable<List<Human>>() {

    override fun subscribeActual(observer: Observer<in List<Human>>) {
        // Create a listener to subscribe to HumanAwareness.OnHumansAroundChangedListener.
        val listener = Listener(humanAwareness, observer)
        // Link the disposable to the observer subscription.
        observer.onSubscribe(listener)
        // Get current value.
        observer.onNext(humanAwareness.humansAround)
        // Subscribe the listener to HumanAwareness.OnHumansAroundChangedListener.
        humanAwareness.addOnHumansAroundChangedListener(listener)
    }

    private class Listener(private val humanAwareness: HumanAwareness, private val observer: Observer<in List<Human>>) : Disposable, HumanAwareness.OnHumansAroundChangedListener {
        private val unSubscribed = AtomicBoolean(false)

        override fun onHumansAroundChanged(humans: MutableList<Human>) {
            if (!isDisposed) {
                observer.onNext(humans)
            }
        }

        override fun dispose() {
            if (unSubscribed.compareAndSet(false, true)) {
                humanAwareness.removeOnHumansAroundChangedListener(this)
            }
        }

        override fun isDisposed(): Boolean {
            return unSubscribed.get()
        }
    }
}
