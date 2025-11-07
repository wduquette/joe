package com.wjduquette.joe.types.text;

import java.util.function.Function;

/**
 * A column in a text table.
 *
 * @param header The header text
 * @param alignment How to align text in the column
 * @param valueGetter How to extract values from records.
 * @param <R> The record type
 * @param <V> The column's value type.
 */
public record TextColumn<R, V> (
    String header,
    TextAlign alignment,
    Function<R,V> valueGetter
)
{
    // Nothing to do
}
