package com.wjduquette.joe.bert;

import java.util.Arrays;

import static com.wjduquette.joe.bert.Opcode.*;

class VirtualMachine {
    public static final int DEFAULT_STACK_SIZE = 256;

    //-------------------------------------------------------------------------
    // Instance Variables

    //
    // Components
    //

    // The Disassembler
    private final Disassembler disassembler = new Disassembler();

    //
    // Runtime Data
    //

    // The current chunk.  Later, this will be a `Function`.
    private Chunk chunk = null;

    // The instruction pointer, an index into this chunk's code[].
    private int ip;

    // The value stack
    private Object[] stack = new Object[DEFAULT_STACK_SIZE];

    // The number of items on the stack.  The top item is at
    // stack[top - 1].
    private int top = 0;

    //-------------------------------------------------------------------------
    // Constructor

    VirtualMachine() {
    }

    //-------------------------------------------------------------------------
    // Methods

    void interpret(Chunk chunk) {
        this.chunk = chunk;
        this.ip = 0;
        resetStack();
        run();
    }

    // At present this uses Chunk directly.  Later the chunk info will
    // be in `Function` in a more efficient form.
    private void run() {
        for (;;) {
            if (Bert.isDebug()) {
                Bert.println("           stack: " + stackText());
                Bert.println(disassembler.disassembleInstruction(chunk, ip));
            }
            var opcode = chunk.code(ip++);
            switch (opcode) {
                case ADD -> {
                    var b = (double)pop();
                    var a = (double)pop();
                    push(a + b);
                }
                case CONST -> push(readConstant());
                case DIV -> {
                    var b = (double)pop();
                    var a = (double)pop();
                    push(a / b);
                }
                case MUL -> {
                    var b = (double)pop();
                    var a = (double)pop();
                    push(a * b);
                }
                case NEGATE -> push(-(double)pop()); // Needs check!
                case RETURN -> {
                    Bert.println(Bert.stringify(pop()));
                    return;
                }
                case SUB -> {
                    var b = (double)pop();
                    var a = (double)pop();
                    push(a - b);
                }
                default -> throw new IllegalStateException(
                    "Unknown opcode: " + opcode + ".");
            }
        }
    }

    private String stackText() {
        if (top == 0) {
            return "";
        }
        var buff = new StringBuilder();
        for (var slot = 0; slot < top; slot++) {
            buff.append("[ ")
                .append(Bert.stringify(stack[slot]))
                .append(" ]");
        }
        return buff.toString();
    }

    //-------------------------------------------------------------------------
    // Instruction decoding

    // Reads a constant index from the chunk, and returns the indexed
    // constant.
    private Object readConstant() {
        var index = chunk.code(ip++);
        return chunk.getConstant(index);
    }

    //-------------------------------------------------------------------------
    // Stack Operations

    private void resetStack() {
        top = 0;
    }

    private void push(Object value) {
        if (top == stack.length) {
            stack = Arrays.copyOf(stack, 2*stack.length);
        }
        stack[top++] = value;
    }

    private Object pop() {
        return stack[--top];
    }

}
