package com.wjduquette.joe.nero;


import com.wjduquette.joe.patterns.Pattern;

/**
 * A term in a Nero body atom that matches a Joe destructuring pattern.
 * @param pattern The pattern
 */
public record PatternTerm(Pattern pattern) implements Term {
    @Override
    public String toString() {
        return pattern.toString();
    }
}
