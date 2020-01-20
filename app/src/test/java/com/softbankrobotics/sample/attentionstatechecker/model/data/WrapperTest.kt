package com.softbankrobotics.sample.attentionstatechecker.model.data

import org.junit.Assert.*
import org.junit.Test

class WrapperTest {

    @Test
    fun empty_has_null_content() {
        val wrapper = Wrapper.empty()
        assertNull(wrapper.content)
    }

    @Test
    fun not_empty_keeps_content() {
        val content = 42
        val wrapper = Wrapper.of(content)
        assertEquals(content, wrapper.content)
    }
}