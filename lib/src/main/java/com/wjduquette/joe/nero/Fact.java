package com.wjduquette.joe.nero;

import java.util.List;
import java.util.stream.Collectors;

public record Fact(String relation, List<Term> terms) {
    @Override public String toString() {
        var termString = terms.stream().map(Term::toString)
            .collect(Collectors.joining(", "));
        return relation + "(" + termString + ")";
    }
}
