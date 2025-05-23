package com.wjduquette.joe.nero;

/** A Term in an {@link HeadAtom}. */
public sealed interface Term permits Constant, Variable, Wildcard { }
