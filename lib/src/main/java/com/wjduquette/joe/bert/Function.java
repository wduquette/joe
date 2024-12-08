package com.wjduquette.joe.bert;

import com.wjduquette.joe.SourceBuffer;

import java.util.Arrays;

/**
 * A compiled function.  Functions proper, methods, and entire scripts all
 * get compiled to a Function.
 */
public class Function implements CodeChunk {
    //-------------------------------------------------------------------------
    // Instance Variables

    //
    // Function info
    //

    // The function or method name, or "*script*" or "*lambda*".
    private final String name;

    // The function's type
    private final FunctionType type;

    // The function's arity
    private final int arity;

    //
    // Chunk info
    //

    // The source buffer
    private final SourceBuffer source;

    // The constants table
    final Object[] constants;

    // The compiled code
    final char[] code;

    // The line number associated with each index in code[].
    private final int[] lines;


    //-------------------------------------------------------------------------
    // Constructor

    Function(Chunk chunk) {
        this.name = chunk.name;
        this.type = chunk.type;
        this.arity = chunk.arity;
        this.source = chunk.source();
        this.constants = Arrays.copyOf(chunk.constants, chunk.numConstants);
        this.code = Arrays.copyOf(chunk.code, chunk.size);
        this.lines = Arrays.copyOf(chunk.lines, chunk.size);
    }

    //-------------------------------------------------------------------------
    // CodeChunk API

    @Override public SourceBuffer source() { return source; }
    @Override public FunctionType type() { return type; }
    @Override public String name() { return name; }
    @Override public int codeSize() { return code.length; }
    @Override public char code(int index) { return code[index]; }
    @Override public int line(int index) { return lines[index]; }
    @Override public int numConstants() { return constants.length; }
    @Override public Object getConstant(int index) { return constants[index]; }

    //-------------------------------------------------------------------------
    // Object API

    @Override
    public String toString() {
        return switch (type) {
            case FUNCTION -> "<fn " + name + ">";
            case SCRIPT -> "<script>";
        };
    }
}
