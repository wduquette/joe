package com.wjduquette.joe.nero;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A rule, consisting of a head {@link Atom} (a template for creating new
 * facts), and a body, which consists of normal and negated body
 * {@link Atom Atoms} and a list of constraints.  The normal body Atoms
 * match facts and bind body variables.  For the rule to fire, the bound
 * variables must meet the constraints, and there can be no facts that match
 * the negated body Atoms given the bound variables.
 *
 * <h2>Requirements</h2>
 *
 * <ul>
 * <li>Normal body atoms may contain any kind of {@link Term}.</li>
 * <li>The head atom may not contain {@link Wildcard} terms.</li>
 * <li>Every {@link Variable} in the head atom, the negated body atoms, and
 *     the constraints must be bound in a normal body atom.</li>
 * </ul>
 */
public class Rule {
    //-------------------------------------------------------------------------
    // Instance Variables

    private final Atom head;
    private final List<Atom> body;
    private final List<Atom> normal;
    private final List<Atom> negated;
    private final List<Constraint> constraints;

    //-------------------------------------------------------------------------
    // Constructor

    /**
     * Creates a rule
     * @param head The rule's head
     * @param bodyAtoms The rule's body atoms
     * @param constraints The constraint expressions
     */
    public Rule(
        Atom head,
        List<Atom> bodyAtoms,
        List<Constraint> constraints
    ) {
        this.head = head;
        this.body = bodyAtoms;
        this.constraints = constraints;
        this.normal = bodyAtoms.stream().filter(a -> !a.isNegated()).toList();
        this.negated = bodyAtoms.stream().filter(Atom::isNegated).toList();
    }

    /**
     * Returns the rule's head.
     * @return The head
     */
    public Atom head() {
        return head;
    }

    /**
     * Returns the rule's body atoms in order of definition.
     * @return The atoms
     */
    @SuppressWarnings("unused")
    public List<Atom> bodyAtoms() {
        return Collections.unmodifiableList(body);
    }

    /**
     * Returns the rule's normal (i.e., non-negated) body atoms in order of
     * definition.
     * @return The atoms
     */
    public List<Atom> normal() {
        return normal;
    }

    /**
     * Returns the rule's negated body atoms in order of definition.
     * @return The atoms
     */
    public List<Atom> negated() {
        return negated;
    }

    /**
     * Returns the rule's constraints in order of definition.
     * @return The atoms
     */
    public List<Constraint> constraints() {
        return Collections.unmodifiableList(constraints);
    }

    @Override
    public String toString() {
        var bodyString = body.stream().map(Atom::toString)
            .collect(Collectors.joining(", "));
        var constraintString = constraints.stream().map(Constraint::toString)
            .collect(Collectors.joining(", "));
        return head + " :- " + bodyString
            + (constraints.isEmpty() ? "" : " where " + constraintString)
            + ";";
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
