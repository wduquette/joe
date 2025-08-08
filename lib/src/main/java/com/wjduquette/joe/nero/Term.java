package com.wjduquette.joe.nero;

/** A Term in a Nero {@link Atom}. */
public sealed interface Term permits Constant, Variable, Wildcard {
    /**
     * Computes the value of the term as used in a rule head or
     * axiom, given the bindings.
     * @param bindings The bindings
     * @return The value
     */
    static Object toValue(Term term, Bindings bindings) {
        return switch (term) {
            case Constant c -> c.value();
            case Variable v -> bindings.get(v.name());
            default -> throw new UnsupportedOperationException(
                "toValue is unsupported for body term: " +
                    term.getClass().getSimpleName() + " '" +
                    term + "'.");
        };
    }
}
