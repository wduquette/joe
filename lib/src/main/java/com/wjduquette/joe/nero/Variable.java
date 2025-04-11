package com.wjduquette.joe.nero;

/**
 * A variable in an Atom.  The Variable has a name.
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

        com.wjduquette.joe.nero.Variable variable = (com.wjduquette.joe.nero.Variable) o;
        return name.equals(variable.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
