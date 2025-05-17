package com.wjduquette.joe.nero;

/** A Term in an {@link Atom}. */
public sealed interface Term permits Constant, Variable, Wildcard { }
