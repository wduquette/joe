package com.wjduquette.joe.nero;

import java.util.List;
import java.util.stream.Collectors;

/**
 * A rule.  At present, both the head and body components are simply
 * "Facts"; in time, the body components will be simple patterns and
 * comparisons, with "not" as a possibility, and the head will be a
 * fact constructor.
 * @param head The rule's head
 * @param body The body predicates
 */
public record Rule(Fact head, List<Fact> body) {
    @Override
    public String toString() {
        var bodyString = body.stream().map(Fact::toString)
            .collect(Collectors.joining(", "));
        return head + " :- " + bodyString + ".";
    }
}
