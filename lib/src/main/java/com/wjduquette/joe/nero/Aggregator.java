package com.wjduquette.joe.nero;

import java.util.List;

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
    INDEXED_LIST("indexedList", List.of(AggParm.VAR, AggParm.VAR)),

    /**
     * {@code list(item)}: Aggregates a list of the matched items.  The order
     * is unpredictable, but all elements will be retained.
     */
    LIST("list", List.of(AggParm.VAR)),

    /**
     * {@code map(key, value)}: Aggregates a map of key/value pairs.  The
     * RuleEngine will throw an error if there are duplicate keys.
     */
    MAP("map", List.of(AggParm.VAR, AggParm.VAR)),

    /**
     * {@code max(x)}: Aggregates the maximum of the values, ignoring
     * non-numeric values. If there are no numeric values, the rule will
     * not fire.
     */
    MAX("max", List.of(AggParm.VAR)),

    /**
     * {@code min(x)}: Aggregates the minimum of the values, ignoring
     * non-numeric values. If there are no numeric values, the rule will
     * not fire.
     */
    MIN("min", List.of(AggParm.VAR)),

    /**
     * {@code set(x)}: Aggregates a set of the matched items.
     */
    SET("set", List.of(AggParm.VAR)),

    /**
     * {@code sum(x)}: Aggregates the sum of the values, ignoring non-numeric
     * values. If there are no numeric values, the sum will be 0.
     */
    SUM("sum", List.of(AggParm.VAR));

    //-------------------------------------------------------------------------
    // Metadata

    private final String function;
    private final List<AggParm> signature;  // The signature

    // The signature is a list of ArgType values indicating whether the
    // argument is a data value (VAL) to be consumed by the aggregator or a
    // variable (VAR) to be aggregated over. By convention, VAL parameters
    // precede VAR parameters.
    Aggregator(String function, List<AggParm> signature) {
        this.function = function;
        this.signature = signature;
    }

    /**
     * The aggregation function's name
     * @return the name
     */
    public String function() {
        return function;
    }

    /**
     * Returns the function's signature, as a list of term categories.
     * @return the list
     */
    public List<AggParm> signature() {
        return signature;
    }

    /**
     * The aggregation function's arity
     * @return The arity
     */
    public int arity() { return signature.size(); }

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
