package com.wjduquette.joe.bert;

import com.wjduquette.joe.SourceBuffer;

public interface CodeChunk {
    /**
     * Gets the chunk's source buffer.
     * @return The buffer
     */
    SourceBuffer source();

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
}
