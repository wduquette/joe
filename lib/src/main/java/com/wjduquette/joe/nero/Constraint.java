package com.wjduquette.joe.nero;

public record Constraint(Variable a, Op op, Term b) {
    @Override public String toString() {
        return a + " " + op.lexeme() + " " + b;
    }
}
