package com.softbankrobotics.sample.attentionstatechecker.model.rx.observable

import com.aldebaran.qi.sdk.`object`.human.Human
import com.aldebaran.qi.sdk.`object`.humanawareness.HumanAwareness
import io.mockk.*
import io.reactivex.observers.TestObserver
import org.junit.Before
import org.junit.Test

/**
 * Tests for [HumansAroundObservable].
 */
class HumansAroundObservableTest {

    private val initialHumans = listOf<Human>()
    // Used to capture the listener.
    private val listenerSlot = slot<HumanAwareness.OnHumansAroundChangedListener>()
    // Mocked dependency.
    private val humanAwareness = mockk<HumanAwareness>(relaxed = true) {
        every { humansAround } returns initialHumans
        every { addOnHumansAroundChangedListener(capture(listenerSlot)) } just Runs
    }
    // System under tests.
    private val observable = HumansAroundObservable(humanAwareness)
    // TestObserver, observing the list of humans.
    private val observer = TestObserver<List<Human>>()

    @Before
    fun setUp() {
        observable.subscribe(observer)
    }

    @Test
    fun initial_value_is_initial_humans() {
        observer.assertValue(initialHumans)
    }

    @Test
    fun notifies_when_humans_change() {
        val newHumans = listOf(mockk<Human>())
        // Call the captured listener.
        listenerSlot.captured.onHumansAroundChanged(newHumans)

        observer.assertValues(
                initialHumans,
                newHumans
        )
    }

    @Test
    fun removes_listener_when_disposed() {
        observer.dispose()
        verify { humanAwareness.removeOnHumansAroundChangedListener(listenerSlot.captured) }
    }
}
