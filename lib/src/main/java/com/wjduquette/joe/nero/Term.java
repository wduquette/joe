package com.wjduquette.joe.nero;

import java.util.Objects;

public sealed interface Term
    permits Term.Constant, Term.Variable
{
    record Constant(Object value) implements Term {
        @Override public String toString() {
            // Handles null.
            return Objects.toString(value);
        }
    }

    record Variable(String name) implements Term {
        @Override public String toString() {
            return name;
        }
    }
}
