package com.wjduquette.joe.nero;

/**
 * An aggregation function, as used by in rule heads.  The enum symbol
 * provides the function's name and arity.
 */
public enum Aggregator {
    /**
     * {@code indexList(index, item)}: Aggregates a list of the matched items,
     * sorting them by their index values.  The order is stable, and will be
     * as expected if the indices are all numbers or all strings.
     */
    INDEXED_LIST("indexedList", 2),

    /**
     * {@code list(item)}: Aggregates a list of the matched items.  The order
     * is unpredictable, but all elements will be retained.
     */
    LIST("list", 1),

    /**
     * {@code map(key, value)}: Aggregates a map of key/value pairs.  The
     * RuleEngine will throw an error if there are duplicate keys.
     */
    MAP("map", 2),

    /**
     * {@code max(x)}: Aggregates the maximum of the values, ignoring
     * non-numeric values. If there are no numeric values, the rule will
     * not fire.
     */
    MAX("max", 1),

    /**
     * {@code min(x)}: Aggregates the minimum of the values, ignoring
     * non-numeric values. If there are no numeric values, the rule will
     * not fire.
     */
    MIN("min", 1),

    /**
     * {@code set(x)}: Aggregates a set of the matched items.
     */
    SET("set", 1),

    /**
     * {@code sum(x)}: Aggregates the sum of the values, ignoring non-numeric
     * values. If there are no numeric values, the sum will be 0.
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

    /**
     * The aggregation function's name
     * @return the name
     */
    public String function() { return function; }

    /**
     * The aggregation function's arity
     * @return The arity
     */
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
