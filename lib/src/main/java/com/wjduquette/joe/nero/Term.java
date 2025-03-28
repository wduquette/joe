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

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;

            Constant constant = (Constant) o;
            return Objects.equals(value, constant.value);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(value);
        }
    }

    record Variable(String name) implements Term {
        @Override public String toString() {
            return name;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;

            Variable variable = (Variable) o;
            return name.equals(variable.name);
        }

        @Override
        public int hashCode() {
            return name.hashCode();
        }
    }
}
