package com.wjduquette.joe.nero;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * An aggregating term.
 * @param aggregator The aggregation function
 * @param terms The arguments to the function
 */
public record Aggregate(
    Aggregator aggregator,
    List<Term> terms
) implements Term {
    /**
     * Returns a list of the variables over which this aggregator
     * aggregates, in order.
     * @return the names
     */
    public List<String> overNames() {
        var list = new ArrayList<String>();
        for (var i = 0; i < terms.size(); i++) {
            var term = terms.get(i);
            if (aggregator.types().get(i) == AggParm.VAR) {
                assert term instanceof Variable;
                list.add(((Variable)term).name());
            }
        }
        return list;
    }

    /**
     * Returns a list of any variables passed to the function which it
     * does NOT aggregate over.
     * @return the names
     */
    public List<String> otherNames() {
        var list = new ArrayList<String>();
        for (var i = 0; i < terms.size(); i++) {
            var term = terms.get(i);
            if (aggregator.types().get(i) == AggParm.CONST &&
                term instanceof Variable v
            ) {
                list.add(v.name());
            }
        }
        return list;
    }

    @Override
    public Set<String> getVariableNames() {
        var set = new HashSet<String>();
        for (var term : terms) {
            set.addAll(term.getVariableNames());
        }
        return set;
    }
}
