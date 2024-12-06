package com.wjduquette.joe.bert;

import java.util.Arrays;

/**
 * Chunk is used to accumulate the byte-code, etc., for a script or
 * function.  The code array grows as needed.  When compilation is
 * complete, the products are copied to an immutable fixed-size Function.
 */
class Chunk {
    //-------------------------------------------------------------------------
    // # clox Notes
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
    // ## Chars instead of Bytes
    //
    // `clox` uses actual bytes, unsigned 8-bit integers.  Java has no
    // unsigned 8-bit type, so we use `char`, an unsigned 16-bit integer.
    // Using `char` also gives us more than enough headroom for jump offsets
    // and constant table indices.

    //-------------------------------------------------------------------------
    // Instance Variables

    // The compiled "byte" code.
    private char[] code;

    // The number of items added to the array.
    private int size = 0;

    //-------------------------------------------------------------------------
    // Constructor

    Chunk() {
        code = new char[8];
    }

    //-------------------------------------------------------------------------
    // Methods

    void write(char value) {
        // FIRST, grow the array if necessary
        if (size == code.length) {
            code = Arrays.copyOf(code, 2*code.length);
        }

        // NEXT, emit the value.
        code[size++] = value;
    }

    int size() {
        return size;
    }

    char get(int offset) {
        return code[offset];
    }
}
