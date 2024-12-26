package com.wjduquette.joe.bert;

import com.wjduquette.joe.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.wjduquette.joe.bert.Opcode.*;

class VirtualMachine {
    public static final int DEFAULT_STACK_SIZE = 256;
    public static final int MAX_FRAMES = 64;

    //-------------------------------------------------------------------------
    // Instance Variables

    // The top-level
    private final Joe joe;

    //
    // Components
    //

    // The Disassembler
    private final Disassembler disassembler;

    // The Compiler
    private final Compiler compiler;

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

    // The open upvalues list.  This is an intrusive list, linked by
    // `Upval.next`, so openValues is simply the top upvalue on the list.
    Upval openUpvalues = null;

    //-------------------------------------------------------------------------
    // Constructor

    VirtualMachine(Joe joe) {
        this.joe = joe;
        this.compiler = new Compiler(joe);
        this.disassembler = new Disassembler(joe);
    }

    //-------------------------------------------------------------------------
    // Variable Access

    /**
     * Gets the set of global variable names.
     * @return The set
     */
    Set<String> getVarNames() {
        return globals.keySet();
    }

    /**
     * Gets the value of the named variable, or null if there is none.
     * @param name The name
     * @return The value, or null
     */
    Object getVar(String name) {
        return globals.get(name);
    }

    /**
     * Sets the value of the named variable, replacing any previous value.
     * @param name The name
     * @param value The value
     */
    void setVar(String name, Object value) {
        globals.put(name, value);
    }


    //-------------------------------------------------------------------------
    // Execution

    /**
     * Compiles and executes a script.  The scriptName is usually the script's
     * file name.
     * @param scriptName The name of the script
     * @param source The text of the script
     * @return The result of executing the script.
     * @throws JoeError on compilation or execution error.
     */
    Object interpret(String scriptName, String source) {
        var closure = new Closure(compiler.compile(scriptName, source));
        resetStack();
        stack[top++] = closure;
        call(closure, 0, true);
        try {
            return run();
        } catch (JoeError ex) {
//            System.out.println("Unwinding stack on error: " + ex);
            unwindStack(ex, 0);
            throw ex;
        }
    }

    /**
     * Calls a callable from Java, passing it the given arguments.
     * The callee must be a callable according to `BertEngine::isCallable`.
     * @param callee The callee
     * @param args The arguments
     * @return The result of calling the callable.
     */
    Object callFromJava(Object callee, Object[] args) {
        switch (callee) {
            case Closure closure -> {
                var base = top;
                var argc = args.length;
                stack[top++] = closure;
                for (Object arg : args) {
                    stack[top++] = arg;
                }

                call(closure, argc, true);
                try {
                    return run();
                } catch (JoeError ex) {
                    unwindStack(ex, base);
                    throw ex;
                }
            }
            case NativeCallable jc -> {
                return jc.call(joe, new Args(args));
            }
            default ->
                throw error("Expected callable, got: " + joe.typedValue(callee) + ".");
        }
    }

    // Unwinds the stack back to and including the bottomFrame, adding
    // frame traces to the error.
    private void unwindStack(JoeError error, int bottomFrame) {
        for (var i = frameCount - 1; i >= bottomFrame; i--) {
            var frame = frames[i];
            var function = frame.closure.function;

            var line = function.line(frame.ip);
            var span = function.source().lineSpan(line);
            var message = "In " +
                function.type().text() + " " +
                function.name();
            error.addFrame(span, message);
        }
    }

    private void resetStack() {
        top = 0;
        frameCount = 0;
    }

    private Object run() {
        // Get the top call frame
        frame = frames[frameCount - 1];

        if (joe.isDebug()) {
            // NOTE: Ultimately, the execution trace is going to need to be
            // redirected to a file. OR, redirect output and save it until
            // the end of the instruction.
            joe.printf("%-40s ", " ");
            joe.println("| " + stackText());
        }
        for (;;) {
            if (joe.isDebug()) {
                joe.printf("%-40s ",
                    disassembler.disassembleInstruction(
                        frame.closure.function, frame.ip));
            }
            var opcode = frame.closure.function.code[frame.ip++];
            switch (opcode) {
                case ADD -> {
                    var b = pop();
                    var a = pop();
                    if (a instanceof Double x && b instanceof Double y) {
                        push(x + y);
                    } else if (a instanceof String s) {
                        push(s + joe.stringify(b));
                    } else if (b instanceof String s) {
                        push(joe.stringify(a) + s);
                    } else {
                        throw error("The '+' operator expects two Numbers or at least one String.");
                    }
                }
                case CALL -> {
                    var argCount = readArg();
                    callValue(peek(argCount), argCount);
                    frame = frames[frameCount - 1];
                }
                case CLASS -> push(new BertClass(readString()));
                case CLOSURE -> {
                    var function = readFunction();
                    var closure = new Closure(function);
                    push(closure);
                    for (int i = 0; i < closure.upvalues.length; i++) {
                        boolean isLocal = readArg() == 1;
                        int index = readArg();

                        if (isLocal) {
                            closure.upvalues[i] =
                                captureUpvalue(frame.base + index);
                        } else {
                            closure.upvalues[i] = frame.closure.upvalues[index];
                        }
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
                    push(Joe.isEqual(a, b));
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
                    if (Joe.isFalsey(pop())) frame.ip += offset;
                }
                case JIFKEEP -> {
                    var offset = readArg();
                    if (Joe.isFalsey(peek(0))) frame.ip += offset;
                }
                case JITKEEP -> {
                    var offset = readArg();
                    if (Joe.isTruthy(peek(0))) frame.ip += offset;
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
                case METHOD -> {
                    // NOTE: This was defineMethod in clox
                    var name = readString();
                    var method = (Closure)peek(0);
                    var klass = (BertClass)peek(1);
                    klass.methods.put(name, method);
                    pop(); // Pop the method
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
                    push(!Joe.isEqual(a, b));
                }
                case NEGATE -> {
                    var a = pop();
                    checkNumericOperand(a);
                    push(-(double)a);
                }
                case NOT -> push(Joe.isFalsey(pop()));
                case NULL -> push(null);
                case POP -> pop();
                case PROPGET -> {
                    var target = peek(0);
                    var name = readString();
                    if (target instanceof BertInstance instance) {
                        // FIRST, got a field?
                        if (instance.fields.containsKey(name)) {
                            pop(); // The instance
                            push(instance.fields.get(name));
                            break;
                        }

                        // NEXT, got a method?
                        var method = instance.klass.methods.get(name);

                        if (method != null) {
                            var bound = new BoundMethod(instance, method);
                            pop(); // The instance
                            push(bound);
                            break;
                        }

                        throw error("Undefined property: '" + name + "'.");
                    } else {
                        throw error("Expected Joe object, got: " +
                            joe.typedValue(target));
                    }
                }
                case PROPSET -> {
                    var target = peek(1);
                    var name = readString();
                    if (target instanceof BertInstance instance) {
                        var value = pop();
                        instance.fields.put(name, value);
                        pop();       // Pop the instance
                        push(value); // Push the value; this is an assignment
                    } else {
                        throw error("Expected Joe object, got: " +
                            joe.typedValue(target));
                    }
                }
                case RETURN -> {
                    var result = pop();
                    closeUpvalues(frame.base);
                    frameCount--;

                    // We return from run() if the relevant closure was
                    // called from Java, i.e., via `interpret()` or
                    // via `callFromJava()`.
                    if (frame.calledFromJava) {
                        // Pop the call frame's stack entries
                        top = frame.base;

                        if (joe.isDebug()) {
                            joe.println("| " + stackText());
                        }
                        return result;
                    }

                    // Pop the call frame's stack entries
                    top = frame.base;

                    // Push the result back on the stack, and reset
                    // back to the caller's call frame
                    push(result);
                    frame = frames[frameCount - 1];

                    if (joe.isDebug()) {
                        joe.println("| " + stackText());
                    }
                }
                case SUB -> {
                    var b = pop();
                    var a = pop();
                    checkNumericOperands(opcode, a, b);
                    push((double)a - (double)b);
                }
                case TRUE -> push(true);
                case UPCLOSE -> {
                    // Close and then pop the upvalue whose value is on the
                    // top of the stack.
                    closeUpvalues(top - 1);
                    pop();
                }
                case UPGET -> {
                    int slot = readArg();
                    push(frame.closure.upvalues[slot].get());
                }
                case UPSET -> {
                    int slot = readArg();
                    frame.closure.upvalues[slot].set(peek(0));
                }
                default -> throw new IllegalStateException(
                    "Unknown opcode: " + opcode + ".");
            }
            if (joe.isDebug()) {
                joe.println("| " + stackText());
            }
        }
    }

    private SourceBuffer.Span ipSpan() {
        var line = frame.closure.function.line(frame.ip);
        return frame.closure.function.source().lineSpan(line);
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
                joe.stringify(a) + "'.");
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
                .append(joe.stringify(stack[slot]))
                .append(" ]");
        }
        return buff.toString();
    }

    //-------------------------------------------------------------------------
    // Instruction decoding

    // Reads an instruction argument from the chunk.
    private char readArg() {
        return frame.closure.function.code[frame.ip++];
    }

    // Reads an index from the chunk, and returns the indexed
    // constant as a Function.
    private Function readFunction() {
        var index = frame.closure.function.code[frame.ip++];
        return (Function)frame.closure.function.constants[index];
    }

    // Reads a stack slot argument from the chunk.
    private char readSlot() {
        return frame.closure.function.code[frame.ip++];
    }

    // Reads a constant index from the chunk, and returns the indexed
    // constant.
    private Object readConstant() {
        var index = frame.closure.function.code[frame.ip++];
        return frame.closure.function.constants[index];
    }

    // Reads a constant index from the chunk, and returns the indexed
    // constant as a string.
    private String readString() {
        var index = frame.closure.function.code[frame.ip++];
        return (String)frame.closure.function.constants[index];
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

    // Invoked by Opcode.CALL for all calls
    private void callValue(Object callee, int argCount) {
        switch (callee) {
            case Closure f -> call(f, argCount, false);
            case NativeCallable f -> {
                var args = new Args(Arrays.copyOfRange(stack, top - argCount, top));
                top -= argCount + 1;
                push(f.call(joe, args));
            }
            case BoundMethod bound -> {
                stack[top - argCount - 1] = bound.receiver();
                call(bound.method(), argCount, false);
            }
            case BertClass klass -> {
                stack[top - argCount - 1] = new BertInstance(klass);
            }
            default ->
                throw error("Expected callable, got: " + joe.typedValue(callee) + ".");
        }
    }

    private void call(Closure closure, int argCount, boolean calledFromJava) {
        if (argCount != closure.function.arity) {
            throw error("Callable " + closure + " expected " + closure.function.arity + " arguments, got: " +
                argCount + ".");
        }

        if (frameCount == MAX_FRAMES) {
            throw error("Call stack overflow.");
        }

        var frame = new CallFrame(closure, calledFromJava);
        frames[frameCount++] = frame;
        frame.base = top - argCount - 1;
    }

    private class CallFrame {
        // The closure being executed
        Closure closure;

        // The instruction pointer within the frame.
        int ip;

        // The stack slot for function local 0
        int base;

        // True if this call frame represents a call to `interpret()` or
        // `callFromJava()`.
        final boolean calledFromJava;

        CallFrame(Closure closure, boolean calledFromJava) {
            this.closure = closure;
            this.calledFromJava = calledFromJava;
            this.ip = 0;
            this.base = top;
        }
    }

    //-------------------------------------------------------------------------
    // Upvalues

    private Upval captureUpvalue(int slot) {
        // FIRST, look for an existing upvalue.
        Upval prev = null;
        var upval = openUpvalues;
        while (upval != null && upval.slot > slot) {
            prev = upval;
            upval = upval.next;
        }

        if (upval != null && upval.slot == slot) {
            return upval;
        }

        // NEXT, we need a new upvalue.  Create it, and insert it into
        // the list of open upvalues.
        var createdUpvalue = new Upval(slot);
        createdUpvalue.next = upval;

        if (prev == null) {
            openUpvalues = createdUpvalue;
        } else {
            prev.next = createdUpvalue;
        }

        return createdUpvalue;
    }

    // Close all upvalues with slot >= last.
    private void closeUpvalues(int last) {
        while (openUpvalues != null && openUpvalues.slot >= last) {
            var upval = openUpvalues;
            openUpvalues = upval.next;

            // Move the value to upval.closed and clear upval.slot and
            // upval.next
            upval.close();
        }
    }

    private class Upval implements Upvalue {
        //-------------------------------------------------------------------------
        // Instance variables

        // The next upval in the open value linked list.
        Upval next = null;

        // A stack slot index, or -1
        private int slot;

        // The Upval's value if slot == -1
        private Object closed = null;

        //-------------------------------------------------------------------------
        // Constructor

        // Creates a new upval, initially pointing at the stack slot.
        Upval(int slot) {
            this.slot = slot;
        }

        //-------------------------------------------------------------------------
        // Methods

        public Object get() {
            return slot >= 0 ? stack[slot] : closed;
        }

        public void set(Object value) {
            if (slot >= 0) {
                stack[slot] = value;
            } else {
                closed = value;
            }
        }

        public void close() {
            closed = stack[slot];
            slot = -1;
            next = null;
        }

        @Override
        public String toString() {
            return "Upvalue[slot=" + slot + ", closed=" + closed + "]";
        }
    }
}
