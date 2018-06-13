/*
 * Copyright (C) 2018 Softbank Robotics Europe
 * See COPYING for the license
 */

package com.softbankrobotics.sample.attentionstatechecker.model.data

/**
 * Wrap an object. Can be empty.
 * @param <T> The object type.
 */
data class Wrapper<out T : Any>(val content: T? = null) {

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
