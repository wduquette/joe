package com.wjduquette.joe.nero;


import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A list-literal term in a Nero axiom or rule head.
 * @param terms The literal's terms.
 */
public record ListTerm(List<Term> terms) implements Term {
    @Override
    public Set<String> getVariableNames() {
        var set = new HashSet<String>();
        for (var t : terms) {
            set.addAll(t.getVariableNames());
        }
        return set;
    }

    @Override
    public String toString() {
        var content = terms.stream().map(Term::toString)
            .collect(Collectors.joining(", "));
        return "[" + content + "]";
    }
}
