package com.wjduquette.joe.nero;

/**
 * A Variable in a {@link Atom}.  The Variable's name must be
 * a valid identifier, and must not begin with an underscore.
 * @param name The name
 */
public record Variable(String name) implements Term {
    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        Variable variable = (Variable) o;
        return name.equals(variable.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
