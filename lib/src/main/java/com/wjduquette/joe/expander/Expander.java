package com.wjduquette.joe.expander;

import com.wjduquette.joe.Args;
import com.wjduquette.joe.Joe;
import com.wjduquette.joe.JoeError;
import com.wjduquette.joe.TypeProxy;

public class Expander {
    //-------------------------------------------------------------------------
    // Instance Variables

    private final Joe joe;
    private String macroStart = "<<";
    private String macroEnd = ">>";
    private boolean processing = false;

    //-------------------------------------------------------------------------
    // Constructor

    public Expander(Joe joe) {
        this.joe = joe;
        joe.installType(new EdgarProxy());
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

    public String getMacroStart() {
        return macroStart;
    }

    public void setMacroStart(String start) {
        if (processing) {
            throw new JoeError(
                "Attempt to configure Edgar while processing input.");
        }
        if (start == null || start.isEmpty()) {
            throw joe.expected("macro start delimiter", start);
        }
        this.macroStart = start;
    }

    public String getMacroEnd() {
        return macroEnd;
    }

    public void setMacroEnd(String end) {
        if (processing) {
            throw new JoeError(
                "Attempt to configure Edgar while processing input.");
        }
        if (end == null || end.isEmpty()) {
            throw joe.expected("macro end delimiter", end);
        }
        this.macroEnd = end;
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

    private class EdgarProxy extends TypeProxy<Void> {
        EdgarProxy() {
            super("Edgar");
            staticType();

            staticMethod("getMacroEnd",       this::_getMacroEnd);
            staticMethod("getMacroStart",     this::_getMacroStart);
            staticMethod("setMacroEnd",       this::_setMacroEnd);
            staticMethod("setMacroStart",     this::_setMacroStart);
        }

        private Object _getMacroEnd(Joe joe, Args args) {
            args.exactArity(0, "getMacroEnd()");
            return getMacroEnd();
        }

        private Object _getMacroStart(Joe joe, Args args) {
            args.exactArity(0, "getMacroStart()");
            return getMacroStart();
        }

        private Object _setMacroEnd(Joe joe, Args args) {
            args.exactArity(1, "setMacroEnd(delimiter)");

            setMacroEnd(joe.stringify(args.next()));
            return this;
        }

        private Object _setMacroStart(Joe joe, Args args) {
            args.exactArity(1, "setMacroStart(delimiter)");

            setMacroStart(joe.stringify(args.next()));
            return this;
        }
    }
}
