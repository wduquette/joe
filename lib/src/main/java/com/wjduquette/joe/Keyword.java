package com.wjduquette.joe;

import java.util.Objects;

/**
 * An interned symbol in a Joe interpreter.
 */
public final class Keyword implements Comparable<Keyword> {
    private final String name;

    /**
     * Creates a new keyword.  The name should omit the leading "#".
     * @param name The keyword's name.
     */
    public Keyword(String name) {
        this.name = Objects.requireNonNull(name);
    }

    /**
     * The keyword's name, omitting the leading "#"
     * @return The name
     */
    public String name() {
        return name;
    }

    /**
     * Given a string, converts the string to a Keyword. The
     * string must be a "#" followed by a valid identifier.
     * @param string The string
     * @return The keyword
     * @throws IllegalArgumentException on failure.
     */
    @SuppressWarnings("unused")
    public static Keyword valueOf(String string) {
        if (string.startsWith("#") &&
            Joe.isIdentifier(string.substring(1))
        ) {
            return new Keyword(string.substring(1));
        }

        throw new IllegalArgumentException(
            "Invalid keyword string: '" + string + "'.");
    }

    @Override
    public String toString() {
        return "#" + name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Keyword keyword = (Keyword) o;

        return name.equals(keyword.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public int compareTo(Keyword o) {
        return name.compareTo(o.name);
    }
}
