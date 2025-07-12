package com.wjduquette.joe.nero;

import com.wjduquette.joe.Joe;

import java.util.Objects;

/**
 * A Constant in a Nero {@link Atom}.  The
 * Constant's value is an arbitrary Java object.
 * @param value The value
 */
public record Constant(Object value) implements Term {
    @Override
    public String toString() {
        return switch (value) {
            case null -> "null";
            case String s -> "\"" + Joe.escape(s) + "\"";
            default -> value.toString();
        };
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
