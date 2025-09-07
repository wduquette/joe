package com.wjduquette.joe.nero;

import com.wjduquette.joe.*;
import com.wjduquette.joe.parser.Parser;

import java.util.*;
import java.util.stream.Collectors;

public class Nero {
    private Nero() {} // Static Class

    //-------------------------------------------------------------------------
    // Parsing

    /**
     * Parses the source, checking for syntax errors.
     * Returns the rule set on success.
     * @param source The source
     * @return The rule set
     * @throws SyntaxError on parse error.
     */
    public static NeroRuleSet parse(SourceBuffer source) {
        var traces = new ArrayList<Trace>();
        var parser = new Parser(source, (t, flag) -> traces.add(t));
        var ruleset = parser.parseNero();
        if (!traces.isEmpty()) {
            throw new SyntaxError("Error in Nero input.", traces, false);
        }
        return ruleset;
    }

    /**
     * Parses the source, checking for syntax errors.
     * Returns the rule set on success.
     * @param schema The predefined schema
     * @param source The source
     * @return The rule set
     * @throws SyntaxError on parse error.
     */
    private static NeroRuleSet parse(Schema schema, SourceBuffer source) {
        var traces = new ArrayList<Trace>();
        var parser = new Parser(source, (t, flag) -> traces.add(t));
        var ruleset = parser.parseNero(schema);
        if (!traces.isEmpty()) {
            throw new SyntaxError("Error in Nero input.", traces, false);
        }
        return ruleset;
    }

    //-------------------------------------------------------------------------
    // Compilation

    /**
     * Compiles the source, checking for syntax errors and stratification.
     * Returns the rule set on success and throws an appropriate error
     * on error.
     * @param source The source
     * @return The engine, which has not yet be run.
     * @throws SyntaxError on parse error.
     * @throws JoeError on stratification error.
     */
    public static NeroRuleSet compile(SourceBuffer source) {
        var ruleSet = parse(source);
        if (!ruleSet.isStratified()) {
            throw new JoeError("Nero rule set cannot be stratified.");
        }
        return ruleSet;
    }

    /**
     * Compiles the source, checking for syntax errors and stratification.
     * The source must be compatible with the given pre-defined schema.
     * Returns the rule set on success and throws an appropriate error
     * on error.
     * @param schema The schema
     * @param source The source
     * @return The engine, which has not yet be run.
     * @throws SyntaxError on parse error.
     * @throws JoeError on stratification error.
     */
    public static NeroRuleSet compile(Schema schema, SourceBuffer source) {
        var ruleSet = parse(schema, source);
        if (!ruleSet.isStratified()) {
            throw new JoeError("Nero rule set cannot be stratified.");
        }
        return ruleSet;
    }

    //-------------------------------------------------------------------------
    // Execution pipeline

    public static Pipeline with(String script) {
        return with(new Joe(), script);
    }

    public static Pipeline with(SourceBuffer source) {
        return with(new Joe(), source);
    }

    public static Pipeline with(NeroRuleSet ruleSet) {
        return with(new Joe(), ruleSet);
    }

    public static Pipeline with(Joe joe, String script) {
        var source = new SourceBuffer("*nero*", script);
        return with(joe, source);
    }

    public static Pipeline with(Joe joe, SourceBuffer source) {
        var ruleset = Nero.compile(source);
        return with(joe, ruleset);
    }

    public static Pipeline with(Joe joe, NeroRuleSet ruleSet) {
        return new Pipeline(joe, ruleSet);
    }

    public static class Pipeline {
        //---------------------------------------------------------------------
        // Instance Variables

        private final Joe joe;
        private final NeroRuleSet ruleset;
        private boolean debug = false;

        //---------------------------------------------------------------------
        // Constructor

        private Pipeline(Joe joe, NeroRuleSet ruleset) {
            this.joe = joe;
            this.ruleset = ruleset;
        }

        //---------------------------------------------------------------------
        // Pipeline methods

        public Pipeline debug(boolean flag) {
            this.debug = flag;
            return this;
        }

        public Pipeline debug() {
            return debug(true);
        }

        //---------------------------------------------------------------------
        // Execution methods

        /**
         * Infers and returns all facts from the rule set.
         * @return The facts
         */
        public FactSet infer() {
            var engine = new RuleEngine(joe, ruleset, new FactSet());
            engine.setDebug(debug);
            return engine.infer();
        }

        /**
         * Infers all known facts from the rule set and fact set, updating
         * the fact set in place.  Returns the inferred facts.  Make an
         * explicit copy of the fact set to retain it unchanged.
         * @param facts The input facts
         * @return The inferred facts.
         */
        public FactSet infer(FactSet facts) {
            var engine = new RuleEngine(joe, ruleset, facts);
            engine.setDebug(debug);
            return engine.infer();
        }
    }

    //------------------------------------------------------------------------
    // Nero-formatting

    /**
     * Converts the contents of the FactSet into a string in Nero format, if
     * possible.  All fact terms must be expressible as Nero literal terms.
     * Creates a Joe interpreter to do the term conversions.
     * @param db The factSet
     * @return The Nero source text
     * @throws JoeError if constraints are not met.
     */
    public static String toNeroScript(FactSet db) {
        return toNeroScript(new Joe(), db);
    }

    /**
     * Converts the contents of the FactSet into a string in Nero format, if
     * possible.  All fact terms must be expressible as Nero literal terms.
     * Uses the Joe interpreter for the term conversions.
     * @param joe The Joe interpreter
     * @param db The factSet
     * @return The Nero source text
     * @throws JoeError if constraints are not met.
     */
    public static String toNeroScript(Joe joe, FactSet db) {
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
                .map(a -> toNeroAxiom(joe, a))
                .sorted()
                .collect(Collectors.joining("\n"));
            buff.append(axioms).append("\n");
        }

        return buff.toString();
    }

    /**
     * Outputs a Fact as a Nero axiom, if possible.
     * The fact's terms must be expressible as Nero literal terms.
     * @param joe The Joe interpreter
     * @param fact The Fact
     * @return The axiom text
     * @throws JoeError if constraints are not met.
     */
    public static String toNeroAxiom(Joe joe, Fact fact) {
        var buff = new StringBuilder();
        buff.append(fact.relation()).append("(");

        String terms;

        if (fact.isOrdered()) {
            terms = fact.getFields().stream()
                .map(t -> toNeroTerm(joe, t))
                .collect(Collectors.joining(", "));
        } else {
            var map = new TreeMap<>(fact.getFieldMap());
            terms = map.entrySet().stream()
                .map(e -> e.getKey() + ": " +
                    toNeroTerm(joe, e.getValue()))
                .collect(Collectors.joining(", "));
        }

        buff.append(terms).append(");");
        return buff.toString();
    }

    /**
     * Outputs a value as a Nero literal term, if possible.
     * @param joe The Joe interpreter
     * @param term The term
     * @return The term's literal
     * @throws JoeError if the term cannot be expressed as a Nero literal.
     */
    public static String toNeroTerm(Joe joe, Object term) {
        // This is the easiest way to limit the output to Nero literals.
        return switch (term) {
            case null -> "null";
            case Boolean b -> joe.stringify(b);
            case Double d -> joe.stringify(d);
            case Enum<?> e -> "#" + e.name().toLowerCase();
            case Keyword k -> joe.stringify(k);
            case String s -> Joe.quote(s);
            case List<?> t -> {
                var list = t.stream()
                    .map(item -> toNeroTerm(joe, item))
                    .collect(Collectors.joining(", "));
                yield "[" + list + "]";
            }
            case Map<?,?> t -> {
                if (t.isEmpty()) yield "{:}";
                var list = t.entrySet().stream()
                    .map(e -> toNeroTerm(joe, e.getKey()) +
                        ": " + toNeroTerm(joe, e.getValue()))
                    .sorted()
                    .collect(Collectors.joining(", "));
                yield "{" + list + "}";
            }
            case Set<?> t -> {
                var list = t.stream()
                    .map(item -> toNeroTerm(joe, item))
                    .sorted()
                    .collect(Collectors.joining(", "));
                yield "{" + list + "}";
            }
            default -> throw new JoeError(
                "Non-Nero term: '" + joe.stringify(term));
        };
    }

}
