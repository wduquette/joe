package com.wjduquette.joe.nero;

import java.util.Objects;

/**
 * A Constant in a {@link HeadAtom}.  The Constant's value is an arbitrary Java
 * object.
 * @param value The value
 */
public record Constant(Object value) implements Term {
    @Override
    public String toString() {
        // Handles null.
        return Objects.toString(value);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        com.wjduquette.joe.nero.Constant constant = (com.wjduquette.joe.nero.Constant) o;
        return Objects.equals(value, constant.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }
}
