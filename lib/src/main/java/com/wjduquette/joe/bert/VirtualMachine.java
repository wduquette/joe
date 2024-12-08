package com.wjduquette.joe.bert;

import com.wjduquette.joe.RuntimeError;
import com.wjduquette.joe.SourceBuffer;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static com.wjduquette.joe.bert.Opcode.*;

class VirtualMachine {
    public static final int DEFAULT_STACK_SIZE = 256;
    public static final int MAX_FRAMES = 64;

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

    // The value stack
    private Object[] stack = new Object[DEFAULT_STACK_SIZE];

    // The number of items on the stack.  The top item is at
    // stack[top - 1].
    private int top = 0;

    // The call frame stack
    private final CallFrame[] frames = new CallFrame[MAX_FRAMES];
    private int frameCount;
    private CallFrame frame; // The current frame.  Managed by run().

    //-------------------------------------------------------------------------
    // Constructor

    VirtualMachine() {
    }

    //-------------------------------------------------------------------------
    // Methods

    void interpret(String source) {
        var function = compiler.compile("*script*", source);
        resetStack();
        frames[frameCount++] = new CallFrame(function);
        stack[top++] = function;
        run();
    }

    private void resetStack() {
        top = 0;
        frameCount = 0;
    }


    // At present this uses Chunk directly.  Later the chunk info will
    // be in `Function` in a more efficient form.
    private void run() {
        // Get the top call frame
        frame = frames[frameCount - 1];

        if (Bert.isDebug()) {
            // NOTE: Ultimately, the execution trace is going to need to be
            // redirected to a file.
            Bert.printf("%-40s ", " ");
            Bert.println("| " + stackText());
        }
        for (;;) {
            if (Bert.isDebug()) {
                Bert.printf("%-40s ",
                    disassembler.disassembleInstruction(
                        frame.function, frame.ip));
            }
            var opcode = frame.function.code[frame.ip++];
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
                    if (Bert.isFalsey(pop())) frame.ip += offset;
                }
                case JIFKEEP -> {
                    var offset = readArg();
                    if (Bert.isFalsey(peek(0))) frame.ip += offset;
                }
                case JITKEEP -> {
                    var offset = readArg();
                    if (Bert.isTruthy(peek(0))) frame.ip += offset;
                }
                case JUMP -> {
                    var offset = readArg();
                    frame.ip += offset;
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
                case LOCGET -> {
                    var slot = readSlot();
                    push(stack[frame.base + slot]);
                }
                case LOCSET -> {
                    var slot = readSlot();
                    stack[frame.base + slot] = peek(0);
                }
                case LOOP -> {
                    var offset = readArg();
                    frame.ip -= offset;
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
        var line = frame.function.line(frame.ip);
        return frame.function.source().lineSpan(line);
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
        return frame.function.code[frame.ip++];
    }

    // Reads a stack slot argument from the chunk.
    private char readSlot() {
        return frame.function.code[frame.ip++];
    }

    // Reads a constant index from the chunk, and returns the indexed
    // constant.
    private Object readConstant() {
        var index = frame.function.code[frame.ip++];
        return frame.function.constants[index];
    }

    // Reads a constant index from the chunk, and returns the indexed
    // constant as a string.
    private String readString() {
        var index = frame.function.code[frame.ip++];
        return (String)frame.function.constants[index];
    }

    //-------------------------------------------------------------------------
    // Value Stack Operations

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

    //-------------------------------------------------------------------------
    // Call Stack Operations

    private class CallFrame {
        // The function
        Function function;

        // The instruction pointer within the frame.
        int ip;

        // The stack slot for function local 0
        int base;

        CallFrame(Function function) {
            this.function = function;
            this.ip = 0;
            this.base = top;
        }
    }
}
