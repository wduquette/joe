package com.wjduquette.joe.bert;

/**
 * The different kinds of {@link Function} compiled by Bert's
 * {@link Compiler}.  This is used for semantic error checking
 * during compilation, and as introspection data at runtime.
 */
public enum FunctionType {
    /** A normal function. */   FUNCTION("function"),
    /** A lambda function. */   LAMBDA("lambda"),
    /** An instance method. */  METHOD("method"),
    /** A static method. */     STATIC_METHOD("static method"),
    /** A class initializer. */ INITIALIZER("initializer"),
    /** An entire script. */    SCRIPT("script");

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
