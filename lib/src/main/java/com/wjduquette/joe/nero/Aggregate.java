package com.wjduquette.joe.nero;

import java.util.List;

/**
 * An aggregating term.
 * @param aggregator The aggregation function
 * @param names The names of the variables being aggregated over.
 */
public record Aggregate(
    Aggregator aggregator,
    List<String> names
) implements Term {

}
