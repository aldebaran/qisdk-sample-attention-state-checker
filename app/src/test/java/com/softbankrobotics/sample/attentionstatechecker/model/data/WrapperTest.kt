/*
 * Copyright (C) 2018 SoftBank Robotics Europe
 * See COPYING for the license
 */
package com.softbankrobotics.sample.attentionstatechecker.model.data

import org.junit.Assert.*
import org.junit.Test

/**
 * Tests for [Wrapper].
 */
class WrapperTest {

    @Test
    fun empty_has_null_content() {
        // System under tests.
        val wrapper = Wrapper.empty()
        assertNull(wrapper.content)
    }

    @Test
    fun not_empty_keeps_content() {
        val content = 42
        // System under tests.
        val wrapper = Wrapper.of(content)
        assertEquals(content, wrapper.content)
    }
}