package com.wjduquette.joe.types.text;

import java.util.function.Function;

/**
 * A column in a text table.
 *
 * @param <R> The record type
 * @param <V> The column's value type.
 */
public class TextColumn<R, V> {
    private String header = "";
    private TextAlign alignment = TextAlign.LEFT;
    private Function<R,V> valueGetter = null;

    /**
     * Creates a new text column
     */
    public TextColumn() {
        // Nothing to do
    }

    /**
     * Gets the header string.
     * @return The text
     */
    public String getHeader() {
        return header;
    }

    /**
     * Sets the header string.
     * @param header The text
     */
    public TextColumn<R,V> header(String header) {
        this.header = header;
        return this;
    }

    /**
     * Gets the text alignment in the column.
     * @return The alignment
     */
    public TextAlign getAlignment() {
        return alignment;
    }

    /**
     * Sets the text alignment in the column.
     * @param alignment The alignment
     */
    public TextColumn<R,V> alignment(TextAlign alignment) {
        this.alignment = alignment;
        return this;
    }

    /**
     * Gets the function used to extract the column value from
     * a row's record.
     * @return The function
     */
    public Function<R, V> getValueGetter() {
        return valueGetter;
    }

    /**
     * Sets the function used to extract the column value from
     * a row's record.
     * @param valueGetter The function
     */
    public TextColumn<R,V> valueGetter(Function<R, V> valueGetter) {
        this.valueGetter = valueGetter;
        return this;
    }
}
