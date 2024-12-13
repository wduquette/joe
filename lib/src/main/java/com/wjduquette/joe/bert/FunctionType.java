package com.wjduquette.joe.bert;

public enum FunctionType {
    FUNCTION("function"),
    SCRIPT("script");

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
