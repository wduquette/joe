package com.wjduquette.joe.expander;

import com.wjduquette.joe.Args;
import com.wjduquette.joe.Joe;
import com.wjduquette.joe.JoeError;
import com.wjduquette.joe.TypeProxy;
import com.wjduquette.joe.types.EnumProxy;
import com.wjduquette.joe.types.MapValue;

import java.util.Objects;
import java.util.Stack;

/**
 * A Joe "macro expander", based on the Tcllib textutil::expander package.
 */
public class Expander {
    //-------------------------------------------------------------------------
    // Instance Variables

    private final Joe joe;
    private String left = "<<";
    private String right = ">>";
    private ErrorMode errorMode = ErrorMode.FAIL;
    private boolean processing = false;
    private final Stack<Context> contextStack = new Stack<>();

    //-------------------------------------------------------------------------
    // Constructor

    public Expander() {
        this(new Joe());
    }

    public Expander(Joe joe) {
        this.joe = joe;

        //   **
        // @package joe.expander
        // TODO
        joe.installType(new ExpanderProxy());


        //   **
        // @enum ErrorMode
        // How the `Expander` will handle macros that throw errors.
        // @constant FAIL
        // The error will propagate to the client (default).
        // @constant MACRO
        // The macro will output itself, with brackets.
        // @constant IGNORE
        // The macro will be ignored, producing no output.
        joe.installType(new EnumProxy<>("ErrorMode", ErrorMode.class));
    }

    //-------------------------------------------------------------------------
    // Configuration

    public Joe joe() {
        return joe;
    }

    public void loadConfiguration(String fileName, String source)
        throws JoeError
    {
        joe.run(fileName, source);
    }

    public String left() {
        return left;
    }

    public String right() {
        return right;
    }

    public void setBrackets(String left, String right) {
        if (processing) {
            throw new JoeError(
                "Attempt to configure Expander while processing input.");
        }
        if (left == null || left.isEmpty()) {
            throw joe.expected("left macro bracket", right);
        }
        if (right == null || right.isEmpty()) {
            throw joe.expected("right macro bracket", right);
        }
        this.left = left;
        this.right = right;
    }

    @SuppressWarnings("unused")
    public ErrorMode getErrorMode() {
        return errorMode;
    }

    public void setErrorMode(ErrorMode mode) {
        this.errorMode = Objects.requireNonNull(mode);
    }

    //-------------------------------------------------------------------------
    // Expansion

    public String expand(String name, String source) throws JoeError {
        if (processing) {
            throw new IllegalStateException("Recursive expansion!");
        }
        contextStack.clear();
        contextStack.push(new Context(null));

        try {
            processing = true;
            var tokens = new Scanner(this, name, source).getTokens();

            // Rendering pass
            for (var token : tokens) {
                switch (token.type()) {
                    case TEXT ->
                        current().append(token.text());
                    case MACRO -> {
                        var text = evaluate(token);
                        current().append(text);
                    }
                    default -> {} // Do nothing
                }
            }

            if (contextStack.size() != 1) {
                throw new JoeError("Context error in input!");
            }
            return current().text();
        } finally {
            processing = false;
        }
    }

    private Context current() {
        return contextStack.peek();
    }

    private String evaluate(Token macro) {
        try {
            var result = joe.run("*expand*", macro.text() + ";");
            return joe.stringify(result);
        } catch (JoeError ex) {
            return switch (errorMode) {
                case IGNORE -> "";
                case MACRO -> left() + macro.text() + right();
                case FAIL -> throw ex
                    .addInfo("In macro '" + macro.text() + "'")
                    .addInfo("At (" + macro.span().startPosition() +
                        ") in source");
            };
        }
    }

    //-------------------------------------------------------------------------
    // Expander's Joe API

    private class ExpanderProxy extends TypeProxy<Void> {
        ExpanderProxy() {
            super("Expander");
            staticType();

            staticMethod("current",        this::_current);
            staticMethod("getErrorMode",   this::_getErrorMode);
            staticMethod("getVar",         this::_getVar);
            staticMethod("inContext",      this::_inContext);
            staticMethod("left",           this::_left);
            staticMethod("pop",            this::_pop);
            staticMethod("push",           this::_push);
            staticMethod("right",          this::_right);
            staticMethod("setBrackets",    this::_setBrackets);
            staticMethod("setErrorMode",   this::_setErrorMode);
            staticMethod("setVar",         this::_setVar);
        }

        private Object _current(Joe joe, Args args) {
            args.exactArity(0, "current()");
            return current().name();
        }

        private Object _getErrorMode(Joe joe, Args args) {
            args.exactArity(0, "getErrorMode()");
            return errorMode;
        }

        private Object _getVar(Joe joe, Args args) {
            args.exactArity(1, "getVar(name)");
            return current().get(joe.toString(args.next()));
        }

        private Object _inContext(Joe joe, Args args) {
            args.exactArity(1, "inContext(name)");
            return current().name().equals(args.next());
        }

        private Object _left(Joe joe, Args args) {
            args.exactArity(0, "left()");
            return left();
        }

        private Object _pop(Joe joe, Args args) {
            args.exactArity(1, "pop(name)");
            var name = args.next();
            if (contextStack.size() == 1) {
                throw new JoeError("Attempted to pop empty context stack.");
            }

            if (current().name().equals(name)) {
                var text = current().text();
                contextStack.pop();
                return text;
            } else {
                throw joe.expected("context '" + current().name() +
                    "'", name);
            }
        }

        private Object _push(Joe joe, Args args) {
            args.exactArity(1, "push(name)");
            contextStack.push(new Context(args.next()));
            return "";
        }

        private Object _right(Joe joe, Args args) {
            args.exactArity(0, "right()");
            return right();
        }

        private Object _setBrackets(Joe joe, Args args) {
            args.exactArity(2, "setBrackets(left, right)");

            setBrackets(
                joe.stringify(args.next()),
                joe.stringify(args.next()));

            return this;
        }

        private Object _setErrorMode(Joe joe, Args args) {
            args.exactArity(1, "setErrorMode(mode)");
            var mode = joe.toEnum(args.next(), ErrorMode.class);

            setErrorMode(mode);
            return this;
        }

        private Object _setVar(Joe joe, Args args) {
            args.exactArity(2, "setVar(name, value)");
            current()
                .set(joe.toString(args.next()), args.next());
            return this;
        }

    }

    //-------------------------------------------------------------------------
    // Helper Types

    /**
     * The execution context.  Macros can push and pop the context.
     */
    private static class Context {
        //---------------------------------------------------------------------
        // Instance Variables

        private final Object name;
        private final MapValue fields = new MapValue();
        private final StringBuilder buff = new StringBuilder();

        //---------------------------------------------------------------------
        // Constructor

        public Context(Object name) {
            this.name = name;
        }

        //---------------------------------------------------------------------
        // Methods

        public void append(String text) {
            buff.append(text);
        }

        public Object name() {
            return name;
        }

        public Object get(String field) {
            return fields.get(field);
        }

        public void set(String field, Object value) {
            fields.put(field, value);
        }

        public String text() {
            return buff.toString();
        }
    }

    /**
     * How to handle macro errors.
     */
    public enum ErrorMode {
        /** Propagate the error normally. */
        FAIL,

        /** Leave the macro in place. */
        MACRO,

        /** Ignore the error, as though the macro wasn't present. */
        IGNORE
    }
}
