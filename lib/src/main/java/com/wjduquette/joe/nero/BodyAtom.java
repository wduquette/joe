package com.wjduquette.joe.nero;

/**
 * An atom in the body of a rule.
 */
public interface BodyAtom {
    /**
     * Gets whether this atom requires ordered fields or not.
     * @return true or false.
     */
    boolean requiresOrderedFields();

    /**
     * Gets the atom's relation string.
     * @return The relation
     */
    String relation();

    /**
     * Matches the atom against the fact, given the pre-existing bindings.
     * Returns a (possibly updated) set of bindings on successful match,
     * and null on failure.
     * @param fact The fact
     * @param given The given bindings.
     * @return Bindings or null
     */
    Bindings matches(Fact fact, Bindings given);
}
