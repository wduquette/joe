package com.wjduquette.joe.nero;

/**
 * A Wildcard {@link Term} in an {@link Atom}.  The Wildcard's name
 * must be a valid identifier that begins with an underscore.
 * @param name The name
 */
public record Wildcard(String name) implements Term {
    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        Wildcard other = (Wildcard) o;
        return name.equals(other.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
