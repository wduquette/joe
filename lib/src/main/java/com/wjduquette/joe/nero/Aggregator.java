package com.wjduquette.joe.nero;

/**
 * An aggregation function, as used by in rule heads.  The enum symbol
 * provides the function's name and arity.
 */
public enum Aggregator {
    SUM("sum", 1);

    //-------------------------------------------------------------------------
    // Metadata

    private final String function;
    private final int arity;

    Aggregator(String function, int arity) {
        this.function = function;
        this.arity = arity;
    }

    public String function() { return function; }
    public int arity() { return arity; }

    /**
     * Finds the aggregator with the given function
     * @param function The function name
     * @return The aggregator or null if not found.
     */
    public static Aggregator find(String function) {
        for (var a : values()) {
            if (a.function().equals(function)) return a;
        }
        return null;
    }
}
