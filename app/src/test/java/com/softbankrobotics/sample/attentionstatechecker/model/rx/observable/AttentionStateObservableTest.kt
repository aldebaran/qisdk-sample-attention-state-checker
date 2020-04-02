package com.softbankrobotics.sample.attentionstatechecker.model.rx.observable

import com.aldebaran.qi.sdk.`object`.human.AttentionState
import com.aldebaran.qi.sdk.`object`.human.Human
import io.mockk.*
import io.reactivex.observers.TestObserver
import org.junit.Before
import org.junit.Test

/**
 * Tests for [AttentionStateObservable].
 */
class AttentionStateObservableTest {

    private val initialAttention = AttentionState.LOOKING_UP
    // Used to capture the listener.
    private val listenerSlot = slot<Human.OnAttentionChangedListener>()
    // Mocked dependency.
    private val human = mockk<Human>(relaxed = true) {
        every { attention } returns initialAttention
        every { addOnAttentionChangedListener(capture(listenerSlot)) } just Runs
    }
    // System under tests.
    private val observable = AttentionStateObservable(human)
    // TestObserver, observing the attention state.
    private val observer = TestObserver<AttentionState>()

    @Before
    fun setUp() {
        observable.subscribe(observer)
    }

    @Test
    fun initial_value_is_initial_attention() {
        observer.assertValue(initialAttention)
    }

    @Test
    fun notifies_when_attention_change() {
        val newAttention = AttentionState.LOOKING_DOWN
        // Call the captured listener.
        listenerSlot.captured.onAttentionChanged(newAttention)

        observer.assertValues(
                initialAttention,
                newAttention
        )
    }

    @Test
    fun removes_listener_when_disposed() {
        observer.dispose()
        verify { human.removeOnAttentionChangedListener(listenerSlot.captured) }
    }
}
