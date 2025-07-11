package com.wjduquette.joe.nero;

/**
 * A Nero atom, i.e., a relation with its terms.
 */
public sealed interface Atom permits
    NamedAtom, OrderedAtom
{
    /**
     * Gets the atom's relation string.
     * @return The relation
     */
    String relation();
}
