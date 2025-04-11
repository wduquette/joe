package com.wjduquette.joe.bert;

import com.wjduquette.joe.scanner.SourceBuffer;

import java.util.Arrays;
import java.util.List;

/**
 * A compiled function.  Functions proper, methods, and entire scripts all
 * get compiled to a Function.
 *
 * <p>A Function is an immutable {@link CodeChunk}, a buffer of compiled
 * code.  It is not directly callable; callable functions can close over
 * the local variable values present when the function is declared, which
 * can happen any number of times.  Thus, the {@link VirtualMachine}
 * wraps up each Function with the {@link Upvalue Upvalues} it closes over
 * as a {@link Closure}, which is the real internal representation of
 * a function reference.
 * </p>
 */
public class Function implements CodeChunk {
    private static final String ARGS = "args";

    //-------------------------------------------------------------------------
    // Instance Variables

    //
    // Function info
    //

    // The function or method name, or "*script*" or "*lambda*".
    private final String name;

    // The function's type
    private final FunctionType type;

    // The function's arity.  If isVarargs is true, this is the minimum
    // arity.
    final int arity;

    // Whether the function is a varargs function (has the `args`
    // parameter).
    final boolean isVarargs;

    // The function's parameter names
    final List<String> parameters;

    // The number of upvalues the function closes over.
    final int upvalueCount;

    //
    // Chunk info
    //

    // The source buffer
    private final SourceBuffer source;

    // The function's span in the source code.
    private final SourceBuffer.Span span;

    // The constants table
    final Object[] constants;

    // The compiled code
    final char[] code;

    // The line number associated with each index in code[].
    private final int[] lines;

    //-------------------------------------------------------------------------
    // Constructor

    /**
     * Creates a function from the compiled chunk.
     * @param chunk The chunk.
     */
    Function(List<String> parameters, Chunk chunk, int upvalueCount) {
        // Operational data
        this.name = chunk.name;
        this.type = chunk.type;
        if (!parameters.isEmpty() && parameters.getLast().equals(ARGS)) {
            this.arity = parameters.size() - 1;
            this.isVarargs = true;
        } else {
            this.arity = parameters.size();
            this.isVarargs = false;
        }
        this.constants = Arrays.copyOf(chunk.constants, chunk.numConstants);
        this.code = Arrays.copyOf(chunk.code, chunk.size);
        this.upvalueCount = upvalueCount;

        // Debugging/error info
        this.parameters = parameters;
        this.source = chunk.source();
        this.span = chunk.span;
        this.lines = Arrays.copyOf(chunk.lines, chunk.size);
    }

    //-------------------------------------------------------------------------
    // CodeChunk API

    @Override public SourceBuffer source() { return source; }
    @Override public SourceBuffer.Span span() { return span; }
    @Override public FunctionType type() { return type; }
    @Override public String name() { return name; }
    @Override public int codeSize() { return code.length; }
    @Override public char code(int index) { return code[index]; }
    @Override public int line(int index) { return lines[index]; }
    @Override public int numConstants() { return constants.length; }
    @Override public Object getConstant(int index) { return constants[index]; }
    @Override public int[] lines() { return lines; }

    //-------------------------------------------------------------------------
    // Function API

    /**
     * Gets the function's signature, e.g., "name(p1, p2, ...)"
     * @return The signature
     */
    public String signature() {
        if (type == FunctionType.LAMBDA) {
            return "\\" + String.join(",", parameters);

        } else {
            return name + "(" + String.join(", ", parameters) + ")";
        }
    }

    //-------------------------------------------------------------------------
    // Object API

    @Override
    public String toString() {
        return "Function[" + type.text() + "," + name + "]";
    }
}
