/*
 * Copyright (C) 2018 SoftBank Robotics Europe
 * See COPYING for the license
 */
package com.softbankrobotics.sample.attentionstatechecker.model.data

/**
 * Wrap an object. Can be empty.
 * @param <T> The object type.
 */
data class Wrapper<out T : Any>(val content: T? = null) {

    companion object {
        private val EMPTY = Wrapper<Nothing>()

        fun empty(): Wrapper<Nothing> {
            return EMPTY
        }

        fun <T : Any> of(content: T): Wrapper<T> {
            return Wrapper(content)
        }
    }
}
