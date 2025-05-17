package com.wjduquette.joe.nero;

import java.util.List;
import java.util.stream.Collectors;

/**
 * A rule, consisting of a head {@link Atom} (a template for creating new
 * facts), a body, consisting of normal body atoms that match known facts and bind
 * variables, a list of negations, consisting of patterns that must
 * not match known facts given the bindings, and constraints that
 * the bindings must meet.
 *
 * <h2>Requirements</h2>
 *
 * <ul>
 * <li>Normal body atoms may contain any kind of {@link Term}.</li>
 * <li>The head atom may not contain {@link Wildcard} terms.</li>
 * <li>Every {@link Variable} in the head atom, the negated body atoms, and
 *     the constraints must be bound in a normal body atom.</li>
 * </ul>
 * @param head The rule's head
 * @param body The normal body atoms
 * @param negations The negated body atoms
 * @param constraints The constraint expressions
 */
public record Rule(
    Atom head,
    List<Atom> body,
    List<Atom> negations,
    List<Constraint> constraints
) {
    @Override
    public String toString() {
        var bodyString = body.stream().map(Atom::toString)
            .collect(Collectors.joining(", "));
        var negString = "not " + negations.stream().map(Atom::toString)
            .collect(Collectors.joining(", not "));
        var constraintString = constraints.stream().map(Constraint::toString)
            .collect(Collectors.joining(", "));
        return head + " :- " + bodyString
            + (negations.isEmpty() ? "" : ", " + negString)
            + (constraints.isEmpty() ? "" : " where " + constraintString)
            + ";";
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        Rule rule = (Rule) o;
        return head.equals(rule.head)
            && body.equals(rule.body)
            && negations.equals(rule.negations)
            && constraints.equals(rule.constraints);
    }

    @Override
    public int hashCode() {
        int result = head.hashCode();
        result = 31 * result + body.hashCode();
        result = 31 * result + negations.hashCode();
        result = 31 * result + constraints.hashCode();
        return result;
    }
}
