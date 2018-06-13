/*
 * Copyright (C) 2018 Softbank Robotics Europe
 * See COPYING for the license
 */

package com.softbankrobotics.sample.attentionstatechecker.model.data

import java.util.*

/**
 * Wrap an object. Can be empty.
 * @param <T> The object type.
</T> */
class Wrapper<out T : Any> {

    val content: T?

    private constructor() {
        this.content = null
    }

    private constructor(content: T) {
        this.content = content
    }

    override fun toString(): String {
        return if (content != null)
            String.format("Wrapper[%s]", content)
        else
            "Wrapper.empty"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }

        if (other !is Wrapper<*>) {
            return false
        }

        return content == other.content
    }

    override fun hashCode(): Int {
        return Objects.hashCode(content)
    }

    companion object {
        private val EMPTY = Wrapper<Any>()

        @Suppress("UNCHECKED_CAST")
        fun <T : Any> empty(): Wrapper<T> {
            return EMPTY as Wrapper<T>
        }

        fun <T : Any> of(content: T): Wrapper<T> {
            return Wrapper(content)
        }
    }
}
