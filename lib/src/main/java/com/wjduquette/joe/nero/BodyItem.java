package com.wjduquette.joe.nero;

public sealed interface BodyItem
    permits BodyItem.Atom, BodyItem.NotAtom
{
    default boolean isNegated() {
        return this instanceof NotAtom;
    }

    record Atom(com.wjduquette.joe.nero.Atom atom) implements BodyItem {
        @Override public String toString() {
            return atom.toString();
        }
    }
    record NotAtom(com.wjduquette.joe.nero.Atom atom) implements BodyItem {
        @Override public String toString() {
            return "not " + atom.toString();
        }
    }
}
