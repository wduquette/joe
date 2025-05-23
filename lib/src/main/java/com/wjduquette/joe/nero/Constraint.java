package com.wjduquette.joe.nero;

/**
 * A Constraint in a {@link Rule}.  Any {@link Variable} terms must
 * be bound in one of the Rule's body atoms.
 * @param a The constrained variable
 * @param op The comparison operator
 * @param b The compared term, a {@link Constant} or {@link Variable}.
 */
public record Constraint(Variable a, Op op, Term b) {
    @Override public String toString() {
        return a + " " + op.lexeme() + " " + b;
    }

    /**
     * A comparison operator, as used in constraints.
     */
    public enum Op {
        EQ("=="),
        NE("!="),
        GT(">"),
        GE(">="),
        LT("<"),
        LE("<=");

        private final String lexeme;

        Op(String lexeme) { this.lexeme = lexeme; }
        public String lexeme() { return lexeme; }
    }
}
