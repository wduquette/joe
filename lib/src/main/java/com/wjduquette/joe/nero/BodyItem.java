package com.wjduquette.joe.nero;

public sealed interface BodyItem
    permits BodyItem.Normal, BodyItem.Negated
{
    /**
     * Returns true if the item is negated, and false otherwise.
     * @return true or false
     */
    default boolean isNegated() {
        return this instanceof Negated;
    }

    /**
     * Gets the item's atom, or null if none.
     * @return The atom or null.
     */
    Atom atom();

    record Normal(Atom atom) implements BodyItem {
        @Override public String toString() {
            return atom.toString();
        }
    }
    record Negated(Atom atom) implements BodyItem {
        @Override public String toString() {
            return "not " + atom.toString();
        }
    }
}
