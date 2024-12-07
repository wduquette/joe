package com.wjduquette.joe.bert;

import com.wjduquette.joe.RuntimeError;
import com.wjduquette.joe.SourceBuffer;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

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

    // The global environment.
    private final Map<String,Object> globals = new HashMap<>();

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
        if (Bert.isDebug()) {
            // NOTE: Ultimately, the execution trace is going to need to be
            // redirected to a file.
            Bert.printf("%-40s ", " ");
            Bert.println("| " + stackText());
        }
        for (;;) {
            if (Bert.isDebug()) {
                Bert.printf("%-40s ", disassembler.disassembleInstruction(chunk, ip));
            }
            var opcode = chunk.code(ip++);
            switch (opcode) {
                case ADD -> {
                    var b = pop();
                    var a = pop();
                    if (a instanceof Double x && b instanceof Double y) {
                        push(x + y);
                    } else if (a instanceof String s) {
                        push(s + Bert.stringify(b));
                    } else if (b instanceof String s) {
                        push(Bert.stringify(a) + s);
                    } else {
                        throw error("The '+' operator expects two Numbers or at least one String.");
                    }
                }
                case CONST -> push(readConstant());
                case DIV -> {
                    var b = pop();
                    var a = pop();
                    checkNumericOperands(opcode, a, b);
                    push((double)a / (double)b);
                }
                case EQ -> {
                    var b = pop();
                    var a = pop();
                    push(Bert.isEqual(a, b));
                }
                case FALSE -> push(false);
                case GE -> {
                    var b = pop();
                    var a = pop();
                    if (a instanceof Double x && b instanceof Double y) {
                        push(x >= y);
                    } else if (a instanceof String s && b instanceof String t) {
                        push(s.compareTo(t) >= 0);
                    } else {
                        throw error("The '>=' operator expects two Numbers or two Strings.");
                    }
                }
                case GLODEF -> globals.put(readString(), pop());
                case GLOGET -> {
                    var name = readString();
                    if (globals.containsKey(name)) {
                        push(globals.get(name));
                    } else {
                        throw error("Undefined variable: '" + name + "'.");
                    }
                }
                case GLOSET -> {
                    var name = readString();
                    if (globals.containsKey(name)) {
                        globals.put(name, peek(0));
                        // NOTE: we leave the value on the stack, since
                        // assignment is an expression.
                    } else {
                        throw error("Undefined variable: '" + name + "'.");
                    }
                }
                case GT -> {
                    var b = pop();
                    var a = pop();
                    if (a instanceof Double x && b instanceof Double y) {
                        push(x > y);
                    } else if (a instanceof String s && b instanceof String t) {
                        push(s.compareTo(t) > 0);
                    } else {
                        throw error("The '>' operator expects two Numbers or two Strings.");
                    }
                }
                case JIF -> {
                    var offset = readArg();
                    if (Bert.isFalsey(pop())) ip += offset;
                }
                case JIFKEEP -> {
                    var offset = readArg();
                    if (Bert.isFalsey(peek(0))) ip += offset;
                }
                case JITKEEP -> {
                    var offset = readArg();
                    if (Bert.isTruthy(peek(0))) ip += offset;
                }
                case JUMP -> {
                    var offset = readArg();
                    ip += offset;
                }
                case LE -> {
                    var b = pop();
                    var a = pop();
                    if (a instanceof Double x && b instanceof Double y) {
                        push(x <= y);
                    } else if (a instanceof String s && b instanceof String t) {
                        push(s.compareTo(t) <= 0);
                    } else {
                        throw error("The '<=' operator expects two Numbers or two Strings.");
                    }
                }
                case LOCGET -> push(stack[readSlot()]);
                case LOCSET -> stack[readSlot()] = peek(0);
                case LOOP -> {
                    var offset = readArg();
                    ip -= offset;
                }
                case LT -> {
                    var b = pop();
                    var a = pop();
                    if (a instanceof Double x && b instanceof Double y) {
                        push(x < y);
                    } else if (a instanceof String s && b instanceof String t) {
                        push(s.compareTo(t) < 0);
                    } else {
                        throw error("The '<' operator expects two Numbers or two Strings.");
                    }
                }
                case MUL -> {
                    var b = pop();
                    var a = pop();
                    checkNumericOperands(opcode, a, b);
                    push((double)a * (double)b);
                }
                case NE -> {
                    var b = pop();
                    var a = pop();
                    push(!Bert.isEqual(a, b));
                }
                case NEGATE -> {
                    var a = pop();
                    checkNumericOperand(a);
                    push(-(double)pop()); // Needs check!
                }
                case NOT -> push(Bert.isFalsey(pop()));
                case NULL -> push(null);
                case PRINT -> {
                    var value = Bert.stringify(pop());
                    Bert.println(value);
                    if (Bert.isDebug()) {
                        Bert.printf("%-40s ", " ");
                    }
                }
                case POP -> pop();
                case RETURN -> {
                    if (Bert.isDebug()) {
                        Bert.println("| " + stackText());
                    }
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
            if (Bert.isDebug()) {
                Bert.println("| " + stackText());
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
            case EQ -> "==";
            case GE -> ">=";
            case GT -> ">";
            case LE -> "<=";
            case LT -> "<";
            case MUL -> "*";
            case NE -> "!=";
            case SUB -> "-";
            default -> throw new IllegalStateException(
                "Unexpected opcode: " + opcode);
        };
        throw error(
            "The '" + op + "' operator expects two numeric operands.");
    }

    private void checkNumericOperand(Object a) {
        if (!(a instanceof Double)) {
            throw error("Expected numeric operand, got: '" +
                Bert.stringify(a) + "'.");
        }
    }

    private RuntimeError error(String message) {
        return new RuntimeError(ipSpan(), message);
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

    // Reads an instruction argument from the chunk.
    private char readArg() {
        return chunk.code(ip++);
    }

    // Reads a stack slot argument from the chunk.
    private char readSlot() {
        return chunk.code(ip++);
    }

    // Reads a constant index from the chunk, and returns the indexed
    // constant.
    private Object readConstant() {
        var index = chunk.code(ip++);
        return chunk.getConstant(index);
    }

    // Reads a constant index from the chunk, and returns the indexed
    // constant as a string.
    private String readString() {
        var index = chunk.code(ip++);
        return (String)chunk.getConstant(index);
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

    private Object peek(int depth) {
        return stack[top - depth - 1];
    }
}
