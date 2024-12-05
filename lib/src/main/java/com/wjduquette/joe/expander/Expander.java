package com.wjduquette.joe.expander;

import com.wjduquette.joe.Args;
import com.wjduquette.joe.Joe;
import com.wjduquette.joe.JoeError;
import com.wjduquette.joe.TypeProxy;

/**
 * A Joe "macro expander", based on the Tcllib textutil::expander package.
 */
public class Expander {
    //-------------------------------------------------------------------------
    // Instance Variables

    private final Joe joe;
    private String left = "<<";
    private String right = ">>";
    private boolean processing = false;

    //-------------------------------------------------------------------------
    // Constructor

    public Expander(Joe joe) {
        this.joe = joe;
        joe.installType(new ExpanderProxy());
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
            throw ex.addInfo("At (" +
                macro.span().startPosition() + ") in source");
        }
    }

    //-------------------------------------------------------------------------
    // Edgar API

    private class ExpanderProxy extends TypeProxy<Void> {
        ExpanderProxy() {
            super("Expander");
            staticType();

            staticMethod("left",         this::_left);
            staticMethod("right",        this::_right);
            staticMethod("setBrackets",  this::_setBrackets);
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
    }
}
