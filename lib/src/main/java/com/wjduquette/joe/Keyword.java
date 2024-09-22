package com.wjduquette.joe;

import java.util.Objects;

/**
 * An interned symbol in a Joe interpreter.
 */
public final class Keyword {
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
}
