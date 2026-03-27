package com.wjduquette.joe.nero;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * An aggregating term.
 * @param aggregator The aggregation function
 * @param names The names of the variables being aggregated over.
 */
public record Aggregate(
    Aggregator aggregator,
    List<String> names
) implements Term {
    @Override
    public Set<String> getVariableNames() {
        return new HashSet<>(names);
    }
}
