package com.wjduquette.joe.bert;

import com.wjduquette.joe.RuntimeError;
import com.wjduquette.joe.SourceBuffer;

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

    // The Compiler
    private final Compiler compiler = new Compiler();

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

    void interpret(String source) {
        this.chunk = new Chunk();
        compiler.compile(source, chunk);
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
                    var b = pop();
                    var a = pop();
                    checkNumericOperands(opcode, a, b);
                    push((double)a + (double)b);
                }
                case CONST -> push(readConstant());
                case DIV -> {
                    var b = pop();
                    var a = pop();
                    checkNumericOperands(opcode, a, b);
                    push((double)a / (double)b);
                }
                case FALSE -> push(false);
                case MUL -> {
                    var b = pop();
                    var a = pop();
                    checkNumericOperands(opcode, a, b);
                    push((double)a * (double)b);
                }
                case NEGATE -> {
                    var a = pop();
                    checkNumericOperand(a);
                    push(-(double)pop()); // Needs check!
                }
                case NULL -> push(null);
                case RETURN -> {
                    Bert.println(Bert.stringify(pop()));
                    return;
                }
                case SUB -> {
                    var b = pop();
                    var a = pop();
                    checkNumericOperands(opcode, a, b);
                    push((double)a - (double)b);
                }
                case TRUE -> push(true);
                default -> throw new IllegalStateException(
                    "Unknown opcode: " + opcode + ".");
            }
        }
    }

    private SourceBuffer.Span ipSpan() {
        return chunk.span(ip - 1);
    }

    private void checkNumericOperands(char opcode, Object a, Object b) {
        if (a instanceof Double && b instanceof Double) {
            return;
        }
        var op = switch(opcode) {
            case ADD -> "+";
            case DIV -> "/";
            case MUL -> "*";
            case SUB -> "-";
            default -> throw new IllegalStateException(
                "Unexpected opcode: " + opcode);
        };
        throw new RuntimeError(ipSpan(),
            "The '" + op + "' operator expects two numeric operands.");
    }

    private void checkNumericOperand(Object a) {
        if (!(a instanceof Double)) {
            throw new RuntimeError(ipSpan(),
                "Expected numeric operand, got: '" + Bert.stringify(a) + "'.");
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
