package com.wjduquette.joe.nero;


import com.wjduquette.joe.patterns.Pattern;

import java.util.Set;

/**
 * A term in a Nero body atom that matches a Joe destructuring pattern.
 * @param pattern The pattern
 */
public record PatternTerm(Pattern pattern) implements Term {
    @Override
    public Set<String> getVariableNames() {
        return Pattern.getVariableNames(pattern);
    }

    @Override
    public String toString() {
        return pattern.toString();
    }
}
