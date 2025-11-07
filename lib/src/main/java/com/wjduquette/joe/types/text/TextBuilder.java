package com.wjduquette.joe.types.text;

/**
 * A Joe replacement for the Java StringBuilder.
 */
public class TextBuilder {
    //-------------------------------------------------------------------------
    // Instance Variables

    // The buffer
    private StringBuilder buff = new StringBuilder();

    //-------------------------------------------------------------------------
    // Constructor

    /**
     * Creates a TextBuilder.
     */
    public TextBuilder() {
        // Nothing to do.
    }

    //-------------------------------------------------------------------------
    // TextBuilder API

    /**
     * Clears the buffer.
     */
    public void clear() {
        this.buff = new StringBuilder();
    }

    /**
     * Appends a string.
     * @param value The string.
     * @return this
     */
    public TextBuilder append(String value) {
        buff.append(value);
        return this;
    }

    /**
     * Gets the accumulated string.
     * @return The string
     */
    public String toString() {
        return buff.toString();
    }
}
