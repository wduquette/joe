package com.wjduquette.joe.nero;

/** A Term in a Nero {@link Atom}. */
public sealed interface Term permits Constant, Variable, Wildcard { }
