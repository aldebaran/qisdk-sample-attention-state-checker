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

    private val _content: T?

    val content: T
        get() = _content ?: throw NoSuchElementException("No content present")

    private constructor() {
        this._content = null
    }

    private constructor(content: T) {
        this._content = content
    }

    fun hasContent(): Boolean {
        return _content != null
    }

    override fun toString(): String {
        return if (_content != null)
            String.format("Wrapper[%s]", _content)
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

        return _content == other._content
    }

    override fun hashCode(): Int {
        return Objects.hashCode(_content)
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
