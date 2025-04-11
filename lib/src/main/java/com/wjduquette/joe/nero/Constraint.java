package com.wjduquette.joe.nero;

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
