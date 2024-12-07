package com.wjduquette.joe.bert;

import com.wjduquette.joe.SourceBuffer;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Chunk is used to accumulate the byte-code, etc., for a script or
 * function.  The code array grows as needed.  When compilation is
 * complete, the products are copied to an immutable fixed-size Function.
 */
class Chunk {
    //-------------------------------------------------------------------------
    // # clox Notes
    //
    // ## Chars instead of Bytes
    //
    // `clox` uses actual bytes, unsigned 8-bit integers.  Java has no
    // unsigned 8-bit type, so we use `char`, an unsigned 16-bit integer.
    // Using `char` also gives us more than enough headroom for jump offsets
    // and constant table indices.
    //
    // ## Chunk Structure
    //
    // This is the analogue of the `Chunk` in Nystrom Ch 14.  In `clox`,
    // the `Chunk` isn't space efficient:
    //
    // - The `code` and `line` arrays can be much larger than necessary.
    // - The `constants` table can contain the same constant multiple times.
    //
    // Further, the `Chunk` is included in-line in the `Function` struct,
    // which makes it faster to access; we can't do that in Java.
    //
    // Consequently, `Chunk` is a dynamic type which is used during
    // compilation; and then a more concise set of data will be copied to
    // the `Function` object to minimize access time.
    //
    // - A code[] array that's exactly the right size.
    // - A constants[] array that's exactly the right size, with no cache.
    // - A LineInfo value that will return the source line for each
    //   code index.

    //-------------------------------------------------------------------------
    // Instance Variables

    private SourceBuffer source = null;

    // The compiled "byte" code.
    private char[] code = new char[8];

    // The source line number associated with each opcode
    private int[] lines = new int[8];

    // The number of items in the code array.
    private int size = 0;

    // The array of constants
    private Object[] constants = new Object[8];
    private char numConstants = 0;
    private final Map<Object,Character> cache = new HashMap<>();

    //-------------------------------------------------------------------------
    // Constructor

    Chunk() {
        // Nothing to do
    }

    //-------------------------------------------------------------------------
    // Methods

    void setSource(SourceBuffer source) {
        this.source = source;
    }

    /**
     * Writes a char value to the code array, growing the array as needed.
     * @param value The value
     * @param line The line in the source code.
     */
    void write(char value, int line) {
        // FIRST, grow the array if necessary
        if (size == code.length) {
            code = Arrays.copyOf(code, 2*code.length);
            lines = Arrays.copyOf(lines, 2*code.length);
        }

        // NEXT, add the value.
        lines[size] = line;
        code[size++] = value;
    }

    /**
     * Gets the size of the code array.
     * @return The size
     */
    int codeSize() {
        return size;
    }

    /**
     * Gets the value at the given index in the code array
     * @param index The index
     * @return The value
     */
    char code(int index) {
        return code[index];
    }

    void setCode(int index, char value) {
        code[index] = value;
    }

    int line(int index) {
        return lines[index];
    }

    SourceBuffer.Span span(int index) {
        return source != null ? source.lineSpan(lines[index]) : null;
    }

    /**
     * Adds a constant to the chunk, and returns its index.  If the
     * constant had already been added, returns the existing index.
     * @param constant The constant value
     * @return the index
     */
    char addConstant(Object constant) {
        // TODO: Check for max constants.
        var index = cache.get(constant);

        if (index == null) {
            if (numConstants == constants.length) {
                constants = Arrays.copyOf(constants, 2*constants.length);
            }
            index = numConstants++;
            constants[index] = constant;
            cache.put(constant, index);
        }

        return index;
    }

    /**
     * Gets the constant at the given index.
     * @param index The index
     * @return The constant value
     */
    Object getConstant(int index) {
        return constants[index];
    }

    int numConstants() {
        return numConstants;
    }
}
