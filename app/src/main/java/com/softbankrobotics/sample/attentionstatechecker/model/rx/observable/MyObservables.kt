/*
 * Copyright (C) 2018 SoftBank Robotics Europe
 * See COPYING for the license
 */
@file:JvmName("MyObservables")

package com.softbankrobotics.sample.attentionstatechecker.model.rx.observable

import com.aldebaran.qi.sdk.QiContext
import com.aldebaran.qi.sdk.`object`.actuation.Frame
import com.aldebaran.qi.sdk.`object`.human.AttentionState
import com.aldebaran.qi.sdk.`object`.human.Human
import com.softbankrobotics.sample.attentionstatechecker.model.data.HumanData
import com.softbankrobotics.sample.attentionstatechecker.utils.distanceObservableFrom
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import io.reactivex.functions.BiFunction
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Provide an observable of the list of [HumanData] corresponding to the list of humans around the robot.
 *
 * @return An observable of the list of [HumanData] corresponding to the list of humans around the robot.
 */
fun humanDataListObservable(qiContext: QiContext): Observable<List<HumanData>> {
    // Get the robot frame.
    val actuation = qiContext.actuation
    val robotFrame = actuation.robotFrame()

    // Get the humans around as an observable.
    val humanAwareness = qiContext.humanAwareness
    val humansAroundObservable = HumansAroundObservable(humanAwareness)

    return humansAroundObservable
            // Use debounce to wait for human list stabilisation.
            .debounce(1, TimeUnit.SECONDS)
            // Use switchMap to automatically unsubscribe from inner observables when the human list changes.
            .switchMap { humans ->
                // If no human, return an empty list.
                if (humans.isEmpty()) {
                    return@switchMap Observable.just(emptyList<HumanData>())
                }

                // Create HumanData observables and put them in a list.
                val observables = humans.map { humanDataObservable(it, robotFrame) }

                // Combine latest the observable list to observe the latest HumanData list.
                combineLatestToList(observables)
            }
}

private fun humanDataObservable(human: Human, robotFrame: Frame): Observable<HumanData> {
    // Create observables from the Human.
    val attentionStateObservable = AttentionStateObservable(human)
    val distanceObservable = human.headFrame.distanceObservableFrom(robotFrame)

    // Combine these observable into an observable of HumanData.
    return humanDataObservable(human, attentionStateObservable, distanceObservable)
}

private fun humanDataObservable(human: Human,
                                attentionStateObservable: Observable<AttentionState>,
                                distanceObservable: Observable<Double>): Observable<HumanData> {
    return Observable.combineLatest(attentionStateObservable, distanceObservable,
            BiFunction { attentionState, distance -> HumanData(human, attentionState, distance) })
}

@Suppress("UNCHECKED_CAST")
private fun <T> combineLatestToList(observables: List<Observable<T>>): Observable<List<T>> {
    return Observable.combineLatest(observables) { objects -> objects.toList() as List<T> }
}

/**
 * Observable providing the [AttentionState] of a [Human], using the [Human.OnAttentionChangedListener].
 *
 * <br></br>
 *
 * Note: Code inspired from Jake Wharton's [RxBinding](https://github.com/JakeWharton/RxBinding) library
 * to convert listeners into observables.
 */
private class AttentionStateObservable(private val human: Human) : Observable<AttentionState>() {

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
