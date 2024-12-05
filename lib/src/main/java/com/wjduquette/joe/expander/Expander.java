package com.wjduquette.joe.expander;

import com.wjduquette.joe.Args;
import com.wjduquette.joe.Joe;
import com.wjduquette.joe.JoeError;
import com.wjduquette.joe.TypeProxy;
import com.wjduquette.joe.types.EnumProxy;

import java.util.Objects;

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

    //-------------------------------------------------------------------------
    // Constructor

    public Expander() {
        this(new Joe());
    }

    public Expander(Joe joe) {
        this.joe = joe;

        //**
        // @package joe.expander
        // TODO
        joe.installType(new ExpanderProxy());


        //**
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
                "Attempt to configure Edgar while processing input.");
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

        try {
            processing = true;
            var tokens = new Scanner(this, name, source).getTokens();
            var buff = new StringBuilder();

            // Rendering pass
            for (var token : tokens) {
                switch (token.type()) {
                    case TEXT -> buff.append(token.text());
                    case MACRO -> buff.append(evaluate(token));
                    default -> {} // Do nothing
                }
            }

            return buff.toString();
        } finally {
            processing = false;
        }
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
    // Edgar API

    private class ExpanderProxy extends TypeProxy<Void> {
        ExpanderProxy() {
            super("Expander");
            staticType();

            staticMethod("getErrorMode", this::_getErrorMode);
            staticMethod("left",         this::_left);
            staticMethod("right",        this::_right);
            staticMethod("setBrackets",  this::_setBrackets);
            staticMethod("setErrorMode", this::_setErrorMode);
        }

        private Object _left(Joe joe, Args args) {
            args.exactArity(0, "left()");
            return left();
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

        private Object _getErrorMode(Joe joe, Args args) {
            args.exactArity(0, "getErrorMode()");
            return errorMode;
        }

        private Object _setErrorMode(Joe joe, Args args) {
            args.exactArity(1, "setErrorMode(mode)");
            var mode = joe.toEnum(args.next(), ErrorMode.class);

            setErrorMode(mode);
            return this;
        }
    }

    //-------------------------------------------------------------------------
    // Error Mode

    public enum ErrorMode {
        FAIL,
        MACRO,
        IGNORE
    }
}
