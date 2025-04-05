package com.wjduquette.joe.nero;

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

