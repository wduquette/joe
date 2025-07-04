package com.wjduquette.joe.nero;

import com.wjduquette.joe.*;
import com.wjduquette.joe.parser.ASTRuleSet;
import com.wjduquette.joe.parser.Parser;

import java.util.ArrayList;
import java.util.List;

/**
 * The Nero class provides standalone parsing and execution of Nero scripts.
 * It is the Nero equivalent of the
 * {@link com.wjduquette.joe.Joe} class, and provides the API used
 * by the `joe nero` tool and by other clients wishing to load and execute
 * standalone Nero scripts.
 */
public class Nero {
    //-------------------------------------------------------------------------
    // Instance Variables

    // The debug flag
    private boolean debug = false;

    // Any error traces.
    private List<Trace> traces = null;

    //-------------------------------------------------------------------------
    // Constructor

    public Nero() {
        // Nothing to do
    }

    //-------------------------------------------------------------------------
    // Internals

    // Compiles the source to a NeroEngine, and checks for stratification.
    // Throws SyntaxError on any parse error and JoeError if the rule set
    // is not stratified.
    private NeroEngine compile(SourceBuffer source) {
        var ast = parse(source);
        var ruleSet = new RuleSetCompiler(ast).compile();
        if (!ruleSet.isStratified()) {
            throw new JoeError("Nero rule set is not stratified.");
        }
        var engine = new NeroEngine(ruleSet);
        engine.setDebug(debug);

        return engine;
    }

    // Parses the source, throwing a SyntaxError on error.
    private ASTRuleSet parse(SourceBuffer source) {
        traces = new ArrayList<>();
        var parser = new Parser(source, this::errorHandler);
        var ast = parser.parseNero();
        if (!traces.isEmpty()) {
            var error = new SyntaxError("Error in Nero input.", traces, false);
            traces = null;
            throw error;
        }
        return ast;
    }

    private void errorHandler(Trace trace, boolean incomplete) {
        traces.add(trace);
    }

    //-------------------------------------------------------------------------
    // Public API

    /**
     * Gets whether Joe is configured for debugging output.
     * @return true or false
     */
    public boolean isDebug() {
        return debug;
    }

    /**
     * Sets whether Joe is configured for debugging output.  This
     * is primarily of use to the Joe maintainer.
     * @param flag true or false
     */
    public void setDebug(boolean flag) {
        this.debug = flag;
    }

    /**
     * Executes the Nero script, throwing an appropriate error on failure
     * and returning a NeroEngine containing the results on success.
     * The filename is usually the bare file name of the script file,
     * but can be any string relevant to the application.
     * @param source The source buffer
     * @return The NeroEngine
     * @throws SyntaxError if the script could not be compiled.
     * @throws JoeError on all runtime errors.
     */
    public NeroEngine execute(SourceBuffer source)
        throws SyntaxError, JoeError
    {
        var engine = compile(source);
        engine.setDebug(debug);
        engine.infer();
        return engine;
    }

    /**
     * Parses the Nero script, returning a text dump of the resulting
     * Abstract Syntax Tree (AST).
     * @param source The source buffer
     * @return The string
     * @throws SyntaxError on any parsing error.
     */
    public String dumpAST(SourceBuffer source) throws SyntaxError {
        return parse(source).toString();
    }
}
