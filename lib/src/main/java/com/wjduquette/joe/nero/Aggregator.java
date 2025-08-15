package com.wjduquette.joe.nero;

/**
 * An aggregation function, as used by in rule heads.  The enum symbol
 * provides the function's name and arity.
 */
public enum Aggregator {
    /**
     * Aggregates a list of the matched items.  The order is unpredictable,
     * but all elements will be retained.
     */
    LIST("list", 1),

    /**
     * Aggregates a map of key/value pairs.  The RuleEngine will throw an
     * error if there are duplicate keys.
     */
    MAP("map", 2),

    /**
     * Aggregates the maximum of the values, ignoring non-numeric values.
     * If there are no numeric values, the rule will not fire.
     */
    MAX("max", 1),

    /**
     * Aggregates the minimum of the values, ignoring non-numeric values.
     * If there are no numeric values, the rule will not fire.
     */
    MIN("min", 1),

    /**
     * Aggregates a set of the matched items.
     */
    SET("set", 1),

    /**
     * Aggregates the sum of the values, ignoring non-numeric values.
     * If there are no numeric values, the sum will be 0.
     */
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
