/*
 * Copyright (C) 2018 Softbank Robotics Europe
 * See COPYING for the license
 */

package com.softbankrobotics.sample.attentionstatechecker.model.rx.observable;

import android.support.annotation.NonNull;

import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.object.actuation.Actuation;
import com.aldebaran.qi.sdk.object.actuation.Frame;
import com.aldebaran.qi.sdk.object.geometry.Vector3;
import com.aldebaran.qi.sdk.object.human.AttentionState;
import com.aldebaran.qi.sdk.object.human.Human;
import com.aldebaran.qi.sdk.object.humanawareness.HumanAwareness;
import com.softbankrobotics.sample.attentionstatechecker.model.data.HumanData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Observable providing the list of {@link HumanData} corresponding to the list of humans around the robot.
 */
public final class MyHumanDataListObservable extends Observable<List<HumanData>> {

    @NonNull
    private final QiContext qiContext;

    public MyHumanDataListObservable(@NonNull QiContext qiContext) {
        this.qiContext = qiContext;
    }

    @Override
    protected void subscribeActual(Observer<? super List<HumanData>> observer) {
        // Get the robot frame.
        Actuation actuation = qiContext.getActuation();
        Frame robotFrame = actuation.robotFrame();

        // Get the humans around as an observable.
        HumanAwareness humanAwareness = qiContext.getHumanAwareness();
        HumansAroundObservable humansAroundObservable = new HumansAroundObservable(humanAwareness);

        Observable<List<HumanData>> humanDataListObservable =
                humansAroundObservable
                        // Use debounce to wait for human list stabilisation.
                        .debounce(1, TimeUnit.SECONDS)
                        // Use switchMap to automatically unsubscribe from inner observables when the human list changes.
                        .switchMap(humans -> {
                            // If no human, return an empty list.
                            if (humans.isEmpty()) {
                                return Observable.just(new ArrayList<>());
                            }

                            // Create HumanData observables and put them in a list.
                            List<Observable<HumanData>> observables = new ArrayList<>();
                            for (Human human : humans) {
                                Observable<HumanData> humanDataObservable = humanDataObservable(human, robotFrame);
                                observables.add(humanDataObservable);
                            }

                            // Combine latest the observable list to observe the latest HumanData list.
                            return combineLatestToList(observables);
                        });

        // Subscribe the observer to the previously created observable.
        humanDataListObservable.subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(observer);
    }

    @NonNull
    private static Observable<HumanData> humanDataObservable(@NonNull Human human, @NonNull Frame robotFrame) {
        // Create observables from the Human.
        Observable<AttentionState> attentionStateObservable = new AttentionStateObservable(human);
        Observable<Double> distanceObservable = new DistanceObservable(human.getHeadFrame(), robotFrame);

        // Combine these observable into an observable of HumanData.
        return humanDataObservable(human, attentionStateObservable, distanceObservable);
    }

    @NonNull
    private static Observable<HumanData> humanDataObservable(@NonNull Human human,
                                                             @NonNull Observable<AttentionState> attentionStateObservable,
                                                             @NonNull Observable<Double> distanceObservable) {
        return Observable.combineLatest(attentionStateObservable, distanceObservable,
                (attentionState, distance) -> new HumanData(human, attentionState, distance));
    }

    @SuppressWarnings("unchecked")
    @NonNull
    private static <T> Observable<List<T>> combineLatestToList(@NonNull List<Observable<T>> observables) {
        return Observable.combineLatest(observables, objects -> (List<T>) Arrays.asList(objects));
    }

    /**
     * Observable providing the {@link AttentionState} of a {@link Human}, using the {@link Human.OnAttentionChangedListener}.
     *
     * <br/>
     *
     * Note: Code inspired from Jake Wharton's <a href="https://github.com/JakeWharton/RxBinding">RxBinding</a> library
     * to convert listeners into observables.
     */
    private static final class AttentionStateObservable extends Observable<AttentionState> {

        @NonNull
        private final Human human;

        private AttentionStateObservable(@NonNull Human human) {
            this.human = human;
        }

        @Override
        protected void subscribeActual(Observer<? super AttentionState> observer) {
            // Create a listener to subscribe to Human.OnAttentionChangedListener.
            Listener listener = new Listener(human, observer);
            // Link the disposable to the observer subscription.
            observer.onSubscribe(listener);
            // Get current value.
            observer.onNext(human.getAttention());
            // Subscribe the listener to Human.OnAttentionChangedListener.
            human.addOnAttentionChangedListener(listener);
        }

        private static final class Listener implements Disposable, Human.OnAttentionChangedListener {

            @NonNull
            private final Human human;
            @NonNull
            private final Observer<? super AttentionState> observer;
            @NonNull
            private final AtomicBoolean unSubscribed = new AtomicBoolean(false);

            private Listener(@NonNull Human human, @NonNull Observer<? super AttentionState> observer) {
                this.human = human;
                this.observer = observer;
            }

            @Override
            public void onAttentionChanged(AttentionState attention) {
                if (!isDisposed()) {
                    observer.onNext(attention);
                }
            }

            @Override
            public void dispose() {
                if (unSubscribed.compareAndSet(false, true)) {
                    human.removeOnAttentionChangedListener(this);
                }
            }

            @Override
            public boolean isDisposed() {
                return unSubscribed.get();
            }
        }
    }

    /**
     * Observable providing the distance between a {@link Human} and the robot, using the human and robot frames.
     *
     * <br/>
     *
     * Note: Code inspired from Jake Wharton's <a href="https://github.com/JakeWharton/RxBinding">RxBinding</a> library
     * to convert listeners into observables.
     */
    private static final class DistanceObservable extends Observable<Double> {

        @NonNull
        private final Frame humanFrame;
        @NonNull
        private final Frame robotFrame;

        private DistanceObservable(@NonNull Frame humanFrame, @NonNull Frame robotFrame) {
            this.humanFrame = humanFrame;
            this.robotFrame = robotFrame;
        }

        @Override
        protected void subscribeActual(Observer<? super Double> observer) {
            // Compute the distance every second.
            Observable.interval(1, TimeUnit.SECONDS)
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.io())
                    .map(ignored -> distance(humanFrame, robotFrame))
                    .subscribe(observer);
        }

        private static double distance(@NonNull Frame frameA, @NonNull Frame frameB) {
            Vector3 translation = frameA.computeTransform(frameB).getTransform().getTranslation();
            double x = translation.getX();
            double y = translation.getY();
            return Math.sqrt(x * x + y * y);
        }
    }

    /**
     * Observable providing the list of humans from {@link HumanAwareness}, using the {@link HumanAwareness.OnHumansAroundChangedListener}.
     *
     * <br/>
     *
     * Note: Code inspired from Jake Wharton's <a href="https://github.com/JakeWharton/RxBinding">RxBinding</a> library
     * to convert listeners into observables.
     */
    private static final class HumansAroundObservable extends Observable<List<Human>> {

        @NonNull
        private final HumanAwareness humanAwareness;

        private HumansAroundObservable(@NonNull HumanAwareness humanAwareness) {
            this.humanAwareness = humanAwareness;
        }

        @Override
        protected void subscribeActual(Observer<? super List<Human>> observer) {
            // Create a listener to subscribe to HumanAwareness.OnHumansAroundChangedListener.
            Listener listener = new Listener(humanAwareness, observer);
            // Link the disposable to the observer subscription.
            observer.onSubscribe(listener);
            // Get current value.
            observer.onNext(humanAwareness.getHumansAround());
            // Subscribe the listener to HumanAwareness.OnHumansAroundChangedListener.
            humanAwareness.addOnHumansAroundChangedListener(listener);
        }

        private static final class Listener implements Disposable, HumanAwareness.OnHumansAroundChangedListener {

            @NonNull
            private final HumanAwareness humanAwareness;
            @NonNull
            private final Observer<? super List<Human>> observer;
            @NonNull
            private final AtomicBoolean unSubscribed = new AtomicBoolean(false);

            private Listener(@NonNull HumanAwareness humanAwareness, @NonNull Observer<? super List<Human>> observer) {
                this.humanAwareness = humanAwareness;
                this.observer = observer;
            }

            @Override
            public void onHumansAroundChanged(List<Human> humans) {
                if (!isDisposed()) {
                    observer.onNext(humans);
                }
            }

            @Override
            public void dispose() {
                if (unSubscribed.compareAndSet(false, true)) {
                    humanAwareness.removeOnHumansAroundChangedListener(this);
                }
            }

            @Override
            public boolean isDisposed() {
                return unSubscribed.get();
            }
        }
    }
}
