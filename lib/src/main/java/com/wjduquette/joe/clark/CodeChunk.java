package com.wjduquette.joe.clark;

import com.wjduquette.joe.SourceBuffer;

/**
 * An interface for a buffer of compiled code, along with its constants table.
 * {@link Chunk} is a mutable CodeChunk used during compilation; it has a number of
 * features to make compilation easier.  A {@link Function} is an immutable
 * CodeChunk along with function metadata.
 */
public interface CodeChunk {
    /**
     * Gets the chunk's source buffer.
     * @return The buffer
     */
    SourceBuffer source();

    /**
     * The chunk's overall span in the source.
     * @return the span
     */
    SourceBuffer.Span span();

    /**
     * Gets the chunk's function type.
     * @return The type
     */
    FunctionType type();

    /**
     * Gets the chunk's name, e.g., the function name.
     * @return The name
     */
    String name();

    /**
     * Gets the number of constants in the function's constants table.
     * @return The number
     */
    int numConstants();

    /**
     * Gets the constant at the given index in the chunk's constant
     * table.
     * @param index The index
     * @return The constant value
     */
    Object getConstant(int index);

    /**
     * Gets the size of the code array
     * @return The size
     */
    int codeSize();

    /**
     * Gets the value at the given offset into the code array.
     * @param index The offset
     * @return The value
     */
    char code(int index);

    /**
     * Gets the source line number for the instruction at the given
     * offset in the code array.
     * @param index The offset
     * @return The line number
     */
    int line(int index);

    /**
     * Gets the array of line number info by instruction.
     * @return The array of lines.
     */
    int[] lines();
}
