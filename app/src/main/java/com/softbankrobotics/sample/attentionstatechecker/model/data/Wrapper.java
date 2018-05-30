/*
 * Copyright (C) 2018 Softbank Robotics Europe
 * See COPYING for the license
 */

package com.softbankrobotics.sample.attentionstatechecker.model.data;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * Wrap an object. Can be empty.
 * @param <T> The object type.
 */
public final class Wrapper<T> {

    @NonNull
    private static final Wrapper<?> EMPTY = new Wrapper<>();

    @Nullable
    private final T content;

    private Wrapper() {
        this.content = null;
    }

    private Wrapper(@NonNull T content) {
        this.content = Objects.requireNonNull(content);
    }

    @NonNull
    public static<T> Wrapper<T> empty() {
        @SuppressWarnings("unchecked")
        Wrapper<T> wrapper = (Wrapper<T>) EMPTY;
        return wrapper;
    }

    @NonNull
    public static <T> Wrapper<T> of(@NonNull T content) {
        return new Wrapper<>(content);
    }

    public boolean hasContent() {
        return content != null;
    }

    @NonNull
    public T getContent() {
        if (content == null) {
            throw new NoSuchElementException("No content present");
        }
        return content;
    }

    @Override
    public String toString() {
        return content != null
                ? String.format("Wrapper[%s]", content)
                : "Wrapper.empty";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof Wrapper)) {
            return false;
        }

        Wrapper<?> other = (Wrapper<?>) obj;
        return Objects.equals(content, other.content);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(content);
    }
}
