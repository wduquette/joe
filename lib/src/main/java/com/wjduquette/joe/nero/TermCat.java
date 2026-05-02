package com.wjduquette.joe.nero;

/**
 * Term category: used to determine which kinds of term are valid
 * in a specific spot. This is used to define Aggregator argument
 * lists.
 */
public enum TermCat {
    /** A bound variable, only. */       BOUND,
    /** A constant or bound variable. */ VALUE
}
