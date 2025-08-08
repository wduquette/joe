package com.wjduquette.joe.nero;

import java.util.List;
import java.util.stream.Collectors;

/**
 * A set-literal term in a Nero axiom or rule head.
 * @param terms The literal's terms.
 */
public record SetTerm(List<Term> terms) implements Term {
    @Override
    public String toString() {
        var content = terms.stream().map(Term::toString)
            .collect(Collectors.joining(", "));
        return "{" + content + "}";
    }
}
