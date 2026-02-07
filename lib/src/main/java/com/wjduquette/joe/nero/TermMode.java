package com.wjduquette.joe.nero;

/**
 * Term Modes, governing how variables are used by an atom's terms.
 * This enum is primarily used with built-in predicates, as each
 * predicate has its own pattern of INOUT and IN terms.
 */
public enum TermMode {
    /**
     * A normal Datalog term, accepting a value from a matched fact or
     * filter matched facts given a value.
     */
    INOUT,

    /**
     * A Datalog term that can only filter matched facts given a value.
     * An IN term must be a constant or a variable bound to the left.
     */
    IN
}
