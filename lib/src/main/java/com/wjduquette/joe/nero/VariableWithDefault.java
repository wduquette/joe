package com.wjduquette.joe.nero;

/**
 * A composite of a Variable and a default value for that variable.
 * The default value must be a Variable or a Constant.  The
 * Variable has mode DEFVAR; the default value term has mode IN.
 * @param variable The variable
 * @param value The default value.
 */
public record VariableWithDefault(Variable variable, Term value) implements Term {
    @Override
    public String toString() {
        return variable + " | " + value;
    }
}
