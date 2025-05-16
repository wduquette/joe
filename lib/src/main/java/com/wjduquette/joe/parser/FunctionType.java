package com.wjduquette.joe.parser;

/**
 * The different kinds of function parsed by the Joe {@link Parser}.
 * This is used for semantic error checking during compilation, and as
 * introspection data at runtime.
 */
public enum FunctionType {
    /** An entire script. */        SCRIPT("script"),
    /** A normal function. */       FUNCTION("function"),
    /** An instance method. */      METHOD("method"),
    /** An instance initializer. */ INITIALIZER("initializer"),
    /** A static method. */         STATIC_METHOD("static method"),
    /** A static initializer. */    STATIC_INITIALIZER("static initializer"),
    /** A lambda function. */       LAMBDA("lambda");

    private final String text;
    FunctionType(String text) {
        this.text = text;
    }

    /**
     * Returns the type name for use in human-readable output.
     * @return The text
     */
    public String text() { return text; }
}
