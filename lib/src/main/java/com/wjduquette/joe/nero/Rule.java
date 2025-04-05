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
public record Rule(
    Atom head,
    List<Atom> body,
    List<Constraint> constraints
) {
    @Override
    public String toString() {
        var bodyString = body.stream().map(Atom::toString)
            .collect(Collectors.joining(", "));
        var constraintString = constraints.stream().map(Constraint::toString)
            .collect(Collectors.joining(", "));
        return head + " :- " + bodyString
            + (constraints.isEmpty() ? "" : " where " + constraintString)
            + ".";
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        Rule rule = (Rule) o;
        return head.equals(rule.head)
            && body.equals(rule.body)
            && constraints.equals(rule.constraints);
    }

    @Override
    public int hashCode() {
        int result = head.hashCode();
        result = 31 * result + body.hashCode();
        result = 31 * result + constraints.hashCode();
        return result;
    }
}
