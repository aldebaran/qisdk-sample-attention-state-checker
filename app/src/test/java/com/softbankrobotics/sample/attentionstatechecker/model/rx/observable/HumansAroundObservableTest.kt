package com.softbankrobotics.sample.attentionstatechecker.model.rx.observable

import com.aldebaran.qi.sdk.`object`.human.Human
import com.aldebaran.qi.sdk.`object`.humanawareness.HumanAwareness
import io.mockk.*
import io.reactivex.observers.TestObserver
import org.junit.Before
import org.junit.Test

class HumansAroundObservableTest {

    private val initialHumans = listOf<Human>()
    private val humanAwareness = mockk<HumanAwareness>(relaxed = true)
    private val listenerSlot = slot<HumanAwareness.OnHumansAroundChangedListener>()
    private val observable = HumansAroundObservable(humanAwareness)
    private val observer = TestObserver<List<Human>>()

    @Before
    fun setUp() {
        every { humanAwareness.humansAround } returns initialHumans
        every { humanAwareness.addOnHumansAroundChangedListener(capture(listenerSlot)) } just Runs
        observable.subscribe(observer)
    }

    @Test
    fun initial_value_is_initial_humans() {
        observer.assertValue(initialHumans)
    }

    @Test
    fun notifies_when_humans_change() {
        val newHumans = listOf(mockk<Human>())
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