package com.wjduquette.joe.nero;

/** A Term in a Nero {@link HeadAtom} or {@link Atom}. */
public sealed interface Term permits Constant, Variable, Wildcard { }
