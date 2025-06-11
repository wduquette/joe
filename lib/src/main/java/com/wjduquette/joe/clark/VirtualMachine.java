package com.wjduquette.joe.clark;

import com.wjduquette.joe.*;
import com.wjduquette.joe.nero.RuleSet;
import com.wjduquette.joe.parser.FunctionType;
import com.wjduquette.joe.patterns.Matcher;
import com.wjduquette.joe.patterns.Pattern;
import com.wjduquette.joe.SourceBuffer;
import com.wjduquette.joe.types.ListValue;
import com.wjduquette.joe.types.MapValue;
import com.wjduquette.joe.types.RuleSetValue;

import java.util.*;

import static com.wjduquette.joe.clark.Opcode.*;

/**
 * The {@link ClarkEngine}'s virtual machine.  This is where the magic
 * happens.
 */
class VirtualMachine {
    public static final int DEFAULT_STACK_SIZE = 256;
    public static final int MAX_FRAMES = 64;
    private static final String STACK_SEPARATOR = "►";

    private enum Origin {
        /** Called from Java code. */ JAVA,
        /** Called from Joe code. */  JOE
    }

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

    // Registers
    private Object registerT = null;

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

    /**
     * Creates an instance of the virtual machine
     * @param joe The owning Joe interpreter.
     */
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
        call(closure, 0, Origin.JAVA);
        try {
            return run();
        } catch (JoeError ex) {
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
            case NativeCallable jc -> {
                // NOTE: callValue could handle this, but that would
                // require first pushing the args onto the stack and
                // then copying them back into an Object[] array.
                try {
                    return jc.call(joe, new Args(args));
                } catch (JoeError ex) {
                    ex.addInfo("In " + jc.callableType() + " " + jc.signature());
                    throw ex;
                } catch (Exception ex) {
                    throw new UnexpectedError(null,
                        "Unexpected Java error: " + ex, ex);
                }
            }
            case ClarkCallable bc -> {
                // FIRST, set up the stack
                var oldTop = top;
                var oldFrameCount = frameCount;
                var argc = args.length;
                stack[top++] = bc;
                for (Object arg : args) {
                    stack[top++] = arg;
                }

                callValue(bc, argc, Origin.JAVA);

                try {
                    return run();
                } catch (JoeError ex) {
                    unwindStack(ex, oldFrameCount);
                    // Result the stack to the old values.
                    top = oldTop;
                    frameCount = oldFrameCount;

                    ex.addInfo("In java call(<" +
                        bc.callableType() + " " + bc.signature() + ">)");
                    throw ex;
                }
            }
            default ->
                throw error("Expected callable, got: " + joe.typedValue(callee) + ".");
        }
    }

    @SuppressWarnings("unused")
    private void dumpState() {
        System.out.println("dumpState:");
        System.out.println("  Stack         = " + stackText());
        System.out.println("  top           = " + top);
        System.out.println("  frameCount    = " + frameCount);
        System.out.println("  frame.closure = " + frame.closure);
        System.out.println("  frame.base    = " + frame.base);
        System.out.println("  frame.ip      = " + frame.ip);
    }

    // Unwinds the stack back to and including the bottomFrame, adding
    // frame traces to the error.
    private void unwindStack(JoeError error, int bottomFrame) {
        for (var i = frameCount - 1; i >= bottomFrame; i--) {
            // FIRST, get the frame and function.
            var frame = frames[i];
            var function = frame.closure.function;

            // NEXT, add the postTrace stack levels, if any.
            SourceBuffer.Span pendingSpan = null;
            if (frame.postTraces != null) {
                while (!frame.postTraces.isEmpty()) {
                    var trace = frame.postTraces.pop();
                    pendingSpan = trace.context();
                    error.addPendingFrame(trace.context(), trace.message());
                }
            }

            // NEXT, add the frame's own stack level.
            // Note: frame.ip is the *next* instruction, the one that
            // didn't actually execute yet.
            var lastIP = Math.max(0, frame.ip - 1);
            var line = function.line(lastIP);
            var span = pendingSpan != null
                ? pendingSpan : function.source().lineSpan(line);
            if (function.type() == FunctionType.SCRIPT) {
                var message = "In <script>";
                error.addFrame(span, message);
            } else {
                var message = "In " +
                    function.type().text() + " " +
                    function.signature();
                error.addFrame(span, message);
            }

            // NEXT, add the preTrace stack trace, if any.
            if (frame.preTrace != null) {
                error.addFrame(frame.preTrace.context(), frame.preTrace.message());
            }
        }
    }

    private void resetStack() {
        // Clear the stack contents to null, so that values can be
        // garbage-collected.  We could set stack cells to null
        // on `pop()`, but we often pop many items at a time by resetting
        // top.  Doing it here ensures that it gets done eventually.
        Arrays.fill(stack, null);

        top = 0;
        frameCount = 0;
        registerT = null;
    }

    private Object run() {
        // Get the top call frame
        frame = frames[frameCount - 1];

        if (joe.isDebug()) {
            // NOTE: Ultimately, the execution trace is going to need to be
            // redirected to a file. OR, redirect output and save it until
            // the end of the instruction.
            joe.printf("%-40s ", " ");
            joe.println(stackText());
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
                case ASSERT -> {
                    var message = pop();
                    throw new AssertError(ipSpan(), joe.stringify(message));
                }
                case CALL -> {
                    var argCount = readArg();
                    callValue(peek(argCount), argCount, Origin.JOE);
                    frame = frames[frameCount - 1];
                }
                case CLASS -> push(new ClarkClass(readString()));
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
                case COMMENT -> readConstant(); // NO-OP
                case CONST -> push(readConstant());
                case DECR -> {
                    var a = pop();
                    checkPrePostOperand("--", a);
                    push((double)a - 1);
                }
                case DIV -> {
                    var b = pop();
                    var a = pop();
                    checkNumericOperands(opcode, a, b);
                    push((double)a / (double)b);
                }
                case DUP -> push(peek(0));
                case DUP2 -> {
                    push(peek(1));
                    push(peek(1));
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
                case GETNEXT -> {
                    var iterator = peek(0);
                    if (iterator instanceof Iterator<?> iter) {
                        push(iter.next());
                    } else {
                        throw new IllegalStateException(
                            "GETNEXT expected iterator, got " +
                                joe.typedValue(iterator));
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
                case GLOBIND -> {
                    var target = pop();
                    var pv = (PatternValue)pop();
                    var bound = new HashMap<String,Object>();

                    // FIRST, see if there's a match.  This is defining globals;
                    // the global environment persists, so we don't want to
                    // touch it unless the match is successful.  Save the
                    // bindings as we go, and then add them to the global
                    // environment as a group.
                    if (!Matcher.bind(
                        joe,
                        pv.pattern,
                        target,
                        pv.constants::get,
                        bound::put
                    )) {
                        throw error(
                            "'var' pattern failed to match target value.");
                    }

                    // NEXT, add the bindings to the global scope.
                    globals.putAll(bound);
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
                case HASNEXT -> {
                    var iterator = peek(0);
                    if (iterator instanceof Iterator<?> iter) {
                        push(iter.hasNext());
                    } else {
                        throw new IllegalStateException(
                            "HASNEXT expected iterator, got " +
                            joe.typedValue(iterator));
                    }
                }
                case IN -> {
                    var collection = checkCollection(pop());
                    var item = pop();
                    push(collection.contains(item));
                }
                case INCR -> {
                    var a = pop();
                    checkPrePostOperand("++", a);
                    push((double)a + 1);
                }
                case INDGET -> {
                    var index = pop();
                    var coll = pop();

                    switch (coll) {
                        case JoeList list -> {
                            var i = checkListIndex(list, index);
                            push(list.get(i));
                        }
                        case JoeMap map -> push(map.get(index));
                        default ->
                            throw error("Expected indexed collection, got: " +
                                joe.typedValue(coll) + ".");
                    }
                }
                case INDSET -> {
                    var value = pop();
                    var index = pop();
                    var coll = pop();

                    switch (coll) {
                        case JoeList list -> {
                            var i = checkListIndex(list, index);
                            list.set(i, value);
                        }
                        case JoeMap map -> map.put(index, value);
                        default ->
                            throw error("Expected indexed collection, got: " +
                                joe.typedValue(coll) + ".");
                    }
                    push(value);
                }
                case INHERIT -> {
                    var superclass = peek(1);

                    if (superclass instanceof JoeClass jc) {
                        // FIRST, can this superclass be extended?
                        if (jc.canBeExtended()) {
                            var subclass = (ClarkClass) peek(0);
                            subclass.inheritSuperclass(jc);
                            pop();  // Subclass
                        } else {
                            throw error("Superclass '" + jc.name() +
                                "' cannot be extended.");
                        }
                    } else {
                        throw error("Expected superclass, got: " +
                            joe.typedValue(superclass));
                    }
                    // NOTE: Superclass is still on the stack, I think
                    // as the `super` variable.  Seems weird, though.
                }
                case ITER -> {
                    var collection = checkCollection(pop());
                    push(collection.iterator());
                }
                case JIF -> {
                    var offset = readArg();
                    if (Joe.isFalsey(pop())) frame.ip += offset;
                }
                case JIFKEEP -> {
                    var offset = readArg();
                    if (Joe.isFalsey(peek(0))) frame.ip += offset;
                }
                case JIT -> {
                    var offset = readArg();
                    if (Joe.isTruthy(pop())) frame.ip += offset;
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
                        throw error(
                            "The '<=' operator expects two Numbers or two Strings.");
                    }
                }
                case LISTADD -> {
                    var item = pop();
                    var list = (ListValue)peek(0);
                    list.add(item);
                }
                case LISTNEW -> push(new ListValue());
                case LOCBIND -> {
                    var target = pop();
                    var pv = (PatternValue)pop();

                    // FIRST, see if there's a match.  This is defining locals,
                    // so just push the bound values onto the stack.  They
                    // are being processed in the order they were defined by
                    // the compiler.
                    if (!Matcher.bind(
                        joe,
                        pv.pattern,
                        target,
                        pv.constants::get,
                        (id, value) -> push(value)
                    )) {
                        throw error(
                            "'var' pattern failed to match target value.");
                    }
                }
                case LOCGET -> {
                    var slot = readInt();
                    push(stack[frame.base + slot]);
                }
                case LOCMOVE -> {
                    var slot = readInt();
                    var n = readInt();
                    var range = top - (frame.base + slot);
                    shift(n, range);
                }
                case LOCSET -> {
                    var slot = readInt();
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
                case MAPNEW -> push(new MapValue());
                case MAPPUT -> {
                    var value = pop();
                    var key = pop();
                    var map = (MapValue)peek(0);
                    map.put(key, value);
                }
                case MATCH -> {
                    var target = pop();
                    var pv = (PatternValue)pop();

                    // FIRST, save the top of the stack.
                    int here = top;

                    // NEXT, match the pattern against the target given the
                    // constants, pushing bound values onto the stack as the
                    // match proceeds.
                    var flag = Matcher.bind(
                        joe,
                        pv.pattern,
                        target,
                        pv.constants::get,
                        (id, value) -> push(value)
                    );

                    // NEXT, if the match failed, pop the bound values.
                    if (!flag) {
                        top = here;
                    }

                    // FINALLY, push the success/failure flag.
                    push(flag);
                }
                case MATCHG -> {
                    var target = pop();
                    var pv = (PatternValue)pop();

                    // FIRST, see if there's a match.  Saves bound values
                    // to the global environment as it goes.
                    var flag = Matcher.bind(
                        joe,
                        pv.pattern,
                        target,
                        pv.constants::get,
                        globals::put
                    );

                    if (!flag) {
                        // Match failed; set all relevant globals to null.
                        for (var name : pv.bindings()) {
                            globals.put(name, null);
                        }
                    }

                    push(flag);
                }
                case MATCHL -> {
                    var target = pop();
                    var pv = (PatternValue)pop();

                    // FIRST, save the top of the stack.
                    int here = top;

                    // NEXT, match the pattern against the target given the
                    // constants, pushing bound values onto the stack as the
                    // match proceeds.
                    var flag = Matcher.bind(
                        joe,
                        pv.pattern,
                        target,
                        pv.constants::get,
                        (id, value) -> push(value)
                    );

                    // NEXT, if the match failed, pop the bound values.
                    if (!flag) {
                        top = here;
                        for (var ignored : pv.bindings) {
                            push(null);
                        }
                    }

                    // FINALLY, push the success/failure flag.
                    push(flag);
                }
                case METHOD -> {
                    // NOTE: This was defineMethod in clox
                    var name = readString();
                    var method = (Closure)peek(0);
                    var type = (ClarkType)peek(1);
                    switch (method.function.type()) {
                        case METHOD, INITIALIZER ->
                            type.addMethod(name, method);
                        case STATIC_METHOD ->
                            type.addStaticMethod(name, method);
                        default -> throw new IllegalStateException(
                            "Invalid closure type in METHOD instruction: " +
                            method.function.type());
                    }
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
                case NI -> {
                    var collection = checkCollection(pop());
                    var item = pop();
                    push(!collection.contains(item));
                }
                case NOT -> push(Joe.isFalsey(pop()));
                case NULL -> push(null);
                case PATTERN -> {
                    var pattern = readPattern();
                    var bindings = readStringList();
                    var constants = (ListValue)pop();
                    push(new PatternValue(pattern, bindings, constants));
                }
                case POP -> pop();
                case POPN -> top -= readArg();
                case PROPGET -> {
                    var target = peek(0);
                    var name = readString();

                    if (target == null) {
                        throw error("Cannot retrieve property, target is null.");
                    }

                    // Note: this works for all JoeObjects, including
                    // `BertClass` and `BertInstance`.
                    var joeObject = joe.getJoeValue(target);
                    pop();
                    push(joeObject.get(name));
                }
                case PROPSET -> {
                    var target = peek(1);
                    var name = readString();

                    if (target == null) {
                        throw error("Cannot retrieve property, target is null.");
                    }

                    // Handle JoeObjects
                    var joeObject = joe.getJoeValue(target);
                    var value = pop();
                    joeObject.set(name, value);
                    pop();       // Pop the instance
                    push(value); // Push the value; this is an assignment.
                }
                case RECORD ->
                    push(new ClarkRecord(readString(), readStringList()));
                case RETURN -> {
                    var result = pop();
                    closeUpvalues(frame.base);
                    frameCount--;

                    // We return from run() if the relevant closure was
                    // called from Java, i.e., via `interpret()` or
                    // via `callFromJava()`.
                    if (frame.origin == Origin.JAVA) {
                        // Pop the call frame's stack entries
                        top = frame.base;

                        if (joe.isDebug()) {
                            joe.println(stackText());
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
                        joe.println(stackText());
                    }
                }
                case RULESET -> {
                    var ruleset = readRuleSet();
                    @SuppressWarnings("unchecked")
                    var exports = (Map<String,Object>)pop();

                    for (var callable : exports.values()) {
                        checkCallable(callable);
                    }

                    push(new RuleSetValue(ruleset, exports));
                }
                case SUB -> {
                    var b = pop();
                    var a = pop();
                    checkNumericOperands(opcode, a, b);
                    push((double)a - (double)b);
                }
                case SUPGET -> {
                    var name = readString();
                    var superclass = (JoeClass)pop();
                    var instance = (JoeValue)peek(0);
                    var method = superclass.bind(instance, name);

                    if (method != null) {
                        pop(); // The instance
                        push(method);
                    } else {
                        throw error("Undefined property: '" + name + "'.");
                    }
                }
                case SWAP -> {
                    var a = pop();
                    var b = pop();
                    push(a);
                    push(b);
                }
                case TGET -> push(registerT);
                case THROW -> {
                    var value = pop();
                    if (value instanceof JoeError err) {
                        throw err.addInfo("Rethrowing existing error.");
                    } else {
                        throw error(joe.stringify(value));
                    }
                }
                case TPUT -> registerT = pop();
                case TRUE -> push(true);
                case TRCPOP -> frame.postTraces.pop();
                case TRCPUSH -> {
                    if (frame.postTraces == null) {
                        frame.postTraces = new Stack<>();
                    }
                    frame.postTraces.push((Trace)readConstant());
                }
                case TSET -> registerT = peek(0);
                case UPCLOSE -> {
                    // Close and then pop the *n* upvalues on the
                    // top of the stack.
                    int n = readArg();
                    closeUpvalues(top - n);
                    top -= n;
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
                joe.println(stackText());
            }
        }
    }

    // Gets the span for the source line that includes that last
    // executed instruction.
    private SourceBuffer.Span ipSpan() {
        // NOTE: frame.ip points at the *next* instruction, not the
        // last one that executed.
        var ip = Math.max(frame.ip - 1, 0);
        var line = frame.closure.function.line(ip);
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

    private void checkPrePostOperand(String op, Object a) {
        if (!(a instanceof Double)) {
            throw error("Target of '" + op + "' must contain a number.");
        }
    }

    private int checkListIndex(List<?> list, Object index) {
        if (index instanceof Double d) {
            int i = d.intValue();
            if (i >= 0 && i < list.size()) {
                return i;
            } else {
                throw error("List index out of range [0, " +
                    (list.size() - 1) + "]: " + i + ".");
            }
        } else {
            throw error("Expected list index, got: " +
                joe.typedValue(index) + ".");
        }
    }

    // Gets the argument as a collection, if possible
    private Collection<?> checkCollection(Object arg) {
        return switch (arg) {
            case Collection<?> c -> c;
            case JoeIterable i -> i.getItems();
            default -> {
                var instance = joe.getJoeValue(arg);
                if (instance.canIterate()) {
                    yield instance.getItems();
                } else {
                    throw error("Expected iterable, got: " +
                            joe.typedValue(arg) + ".");
                }
            }
        };
    }

    private void checkCallable(Object arg) {
        if (!(arg instanceof JoeCallable)) {
            throw error("Expected callable, got: " +
                joe.typedValue(arg) + ".");
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
            buff.append(STACK_SEPARATOR)
                .append(" ")
                .append(joe.stringify(stack[slot]))
                .append(" ");
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

    // Reads a `char` argument from the chunk as an integer.
    private int readInt() {
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

    // Reads a constant index from the chunk, and returns the indexed
    // constant as a list of strings.
    @SuppressWarnings("unchecked")
    private List<String> readStringList() {
        var index = frame.closure.function.code[frame.ip++];
        return (List<String>)frame.closure.function.constants[index];
    }

    // Reads a constant index from the chunk, and returns the indexed
    // constant as a Pattern.
    private Pattern readPattern() {
        var index = frame.closure.function.code[frame.ip++];
        return (Pattern)frame.closure.function.constants[index];
    }

    // Reads a constant index from the chunk, and returns the indexed
    // constant as a RuleSet.
    private RuleSet readRuleSet() {
        var index = frame.closure.function.code[frame.ip++];
        return (RuleSet)frame.closure.function.constants[index];
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

    // Slides the n items at the top of the stack down to the beginning
    // of the range, e.g., given
    //
    //    5 4 3 2 1 top
    //
    // shift(2, 5) will move items like this:
    //
    //    2 1 5 4 3 top
    @SuppressWarnings("ManualArrayCopy")
    private void shift(int n, int range) {
        var base = top - range;
        var copy = Arrays.copyOfRange(stack, base, top);
        var m = range - n;

        // Move vars down
        for (var i = 0; i < n; i++) {
            stack[base + i] = copy[m + i];
        }

        for (var i = 0; i < m; i++) {
            stack[base + n + i] = copy[i];
        }
    }

    //-------------------------------------------------------------------------
    // Call Stack Operations

    // Invoked by Opcode.CALL for all calls
    private void callValue(Object callee, int argCount, Origin origin) {
        switch (callee) {
            case Closure f -> call(f, argCount, origin);
            case NativeCallable f -> {
                var args = new Args(Arrays.copyOfRange(stack, top - argCount, top));
                top -= argCount + 1;
                try {
                    push(f.call(joe, args));
                } catch (JoeError ex) {
                    ex.addInfo("In " + f.callableType() + " " + f.signature());
                    throw ex;
                } catch (Exception ex) {
                    throw new UnexpectedError(null,
                        "Unexpected Java error: " + ex, ex);
                }
            }
            case BoundMethod bound -> {
                stack[top - argCount - 1] = bound.receiver();
                call(bound.method(), argCount, origin);
            }
            case ClarkClass klass -> {
                stack[top - argCount - 1] = klass.make(joe, klass);
                var initializer = klass.methods.get("init");
                if (initializer != null) {
                    var frame = call(initializer, argCount, origin);

                    // Add a trace level for the class itself.
                    frame.preTrace = new Trace(
                        initializer.function.span(),
                        "In " + klass.callableType() + " " + klass.signature());
                } else if (argCount != 0) {
                    throw error(Args.arityFailureMessage(klass.name() + "()"));
                }
            }
            case null -> throw error("Expected callable, got: 'null'.");
            default ->
                throw error("Expected callable, got: " + joe.typedValue(callee) + ".");
        }
    }

    private CallFrame call(Closure closure, int argCount, Origin origin) {
        if (closure.function.isVarargs) {
            // FIRST, make sure we've got the minimum arguments.
            if (argCount < closure.function.arity) {
                throw error(Args.arityFailureMessage(closure.function.signature()));
            }

            // NEXT, replace excess arguments with a single ListValue
            var args = new ListValue();
            var argsSize = argCount - closure.function.arity;
            for (var i = 0; i < argsSize; i++) {
                args.add(stack[top - argsSize + i]);
            }
            top -= argsSize;
            push(args);
            argCount = closure.function.arity + 1;
        } else if (argCount != closure.function.arity) {
            throw error(Args.arityFailureMessage(closure.function.signature()));
        }

        if (frameCount == MAX_FRAMES) {
            throw error("Call stack overflow.");
        }

        var frame = new CallFrame(closure, origin);
        frames[frameCount++] = frame;
        frame.base = top - argCount - 1;
        return frame;
    }

    private class CallFrame {
        // The closure being executed
        Closure closure;

        // The instruction pointer within the frame.
        int ip;

        // The stack slot for function local 0
        int base;

        // Origin.JAVA if this call frame represents a call to `interpret()` or
        // `callFromJava()`, and Origin.JOE otherwise.
        final Origin origin;

        // A stack of pseudo-CallFrame traces, used to add stack levels to
        // the error stack trace within this call frame.  This is used when
        // executing class static initializer blocks, to add the class itself
        // to the stack trace.
        Stack<Trace> postTraces = null;

        // Pre-trace: used to add a pseudo-CallFrame just below this
        // call frame.  The pre-trace will be used to add a stack level to the
        // error stack trace, but is otherwise ignored.  This is used when
        // a class's init() method is implicitly invoked on a call to
        // a `BertClass`, to add the class stack frame.
        Trace preTrace = null;

        //---------------------------------------------------------------------
        // Constructors

        CallFrame(Closure closure, Origin origin) {
            this.closure = closure;
            this.origin = origin;
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

    // A pattern, as evaluated by the PATTERN instruction and used by
    // the MATCH instruction.
    private record PatternValue(
        Pattern pattern,         // The pattern proper
        List<String> bindings,   // The names of the pattern variables
        List<Object> constants   // The evaluated pattern constants
    ) {}
}
