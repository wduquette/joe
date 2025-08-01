package com.wjduquette.joe.nero;

import com.wjduquette.joe.*;
import com.wjduquette.joe.parser.Parser;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;

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

    // The Joe instance
    private final Joe joe;

    // The debug flag
    private boolean debug = false;

    // Any error traces.
    private List<Trace> traces = null;

    //-------------------------------------------------------------------------
    // Constructor

    /**
     * Creates a new instance of the Nero entry point.  The instance of
     * Joe is required to access `Joe::stringify`.
     * @param joe A Joe interpreter.
     */
    public Nero(Joe joe) {
        this.joe = joe;
    }

    //-------------------------------------------------------------------------
    // Public API

    /**
     * Compiles the source, checking for syntax errors and stratification.
     * Returns a RuleEngine on success and throws an appropriate error
     * on error.
     * @param source The source
     * @return The engine, which has not yet be run.
     * @throws JoeError on error.
     */
    public NeroRuleSet compile(SourceBuffer source) {
        var ruleSet = parse(source);
        if (!ruleSet.isStratified()) {
            throw new JoeError("Nero rule set is not stratified.");
        }
        return ruleSet;
    }

    /**
     * Parses the source, checking for syntax errors.
     * Returns a RuleSet on success.
     * @param source The source
     * @return The rule set
     * @throws SyntaxError on error.
     */
    public NeroRuleSet parse(SourceBuffer source) {
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
     * and returning a RuleEngine containing the results on success.
     * @param source The source buffer
     * @return The RuleEngine
     * @throws SyntaxError if the script could not be compiled.
     * @throws JoeError on all runtime errors.
     */
    public RuleEngine execute(SourceBuffer source)
        throws SyntaxError, JoeError
    {
        return execute(compile(source));
    }

    /**
     * Executes the Nero rule set, throwing an appropriate error on failure
     * and returning a RuleEngine containing the results on success.
     * @param ruleSet The rule set
     * @return The RuleEngine
     * @throws JoeError on all runtime errors.
     */
    public RuleEngine execute(NeroRuleSet ruleSet)
        throws JoeError
    {
        var engine = new RuleEngine(ruleSet);
        engine.setDebug(debug);
        engine.infer();
        return engine;
    }

    /**
     * Executes the Nero script with the given input facts, throwing an
     * appropriate error on failure and returning a RuleEngine containing
     * the results on success.
     * @param source The source buffer
     * @param db The input facts
     * @return The RuleEngine
     * @throws SyntaxError if the script could not be compiled.
     * @throws JoeError on all runtime errors.
     */
    public RuleEngine execute(SourceBuffer source, FactSet db)
        throws SyntaxError, JoeError
    {
        return execute(compile(source), db);
    }

    /**
     * Executes the Nero rule set with the given input facts, throwing an
     * appropriate error on failure and returning a RuleEngine containing
     * the results on success.
     * @param ruleSet The rule set
     * @param db The input facts
     * @return The RuleEngine
     * @throws JoeError on all runtime errors.
     */
    public RuleEngine execute(NeroRuleSet ruleSet, FactSet db)
        throws JoeError
    {
        var engine = new RuleEngine(ruleSet, db);
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

    /**
     * Converts the contents of the FactSet into a string in Nero format, if
     * possible.  All fact terms must be expressible as Nero literal terms.
     * @param db The factSet
     * @return The Nero source text
     * @throws JoeError if constraints are not met.
     */
    public String asNeroScript(FactSet db) {
        var schema = Schema.inferSchema(db.getAll());
        var buff = new StringBuilder();
        var relations = db.getRelations().stream().sorted().toList();
        var gotFirst = false;

        for (var relation : relations) {
            var shape = schema.get(relation).toSpec();
            if (gotFirst) buff.append("\n");
            buff.append("define ").append(shape).append(";\n");
            gotFirst = true;

            var axioms = db.getRelation(relation).stream()
                .map(this::asNeroAxiom)
                .sorted()
                .collect(Collectors.joining("\n"));
            buff.append(axioms).append("\n");
        }

        return buff.toString();
    }

    /**
     * Outputs a Fact as a Nero axiom, if possible.
     * The fact's terms must be expressible as Nero literal terms.
     * @param fact The Fact
     * @return The axiom text
     * @throws JoeError if constraints are not met.
     */
    public String asNeroAxiom(Fact fact) {
        var buff = new StringBuilder();
        buff.append(fact.relation()).append("(");

        String terms;

        if (fact.isOrdered()) {
            terms = fact.getFields().stream()
                .map(this::asNeroTerm)
                .collect(Collectors.joining(", "));
        } else {
            var map = new TreeMap<>(fact.getFieldMap());
            terms = map.entrySet().stream()
                .map(e -> e.getKey() + ": " + asNeroTerm(e.getValue()))
                .collect(Collectors.joining(", "));
        }

        buff.append(terms).append(");");
        return buff.toString();
    }

    /**
     * Outputs a value as a Nero literal term, if possible.
     * @param term The term
     * @return The term's literal
     * @throws JoeError if the term cannot be expressed as a Nero literal.
     */
    public String asNeroTerm(Object term) {
        // At present, all we support are the standard scalar literals;
        // this is the easiest way to limit the output to that.
        return switch (term) {
            case null -> "null";
            case Boolean b -> joe.stringify(b);
            case Double d -> joe.stringify(d);
            case Keyword k -> joe.stringify(k);
            case String s -> Joe.quote(s);
            default -> throw new JoeError(
                "Non-Nero term: '" + joe.stringify(term));
        };
    }
}
