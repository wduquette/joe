package com.wjduquette.joe.nero;

import com.wjduquette.joe.*;
import com.wjduquette.joe.parser.Parser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * This class is the primary entry point for the Nero language.
 */
public class Nero {
    /** Relation name for the query/... relation used by query parameters. */
    public static final String QUERY = "query";

    //-------------------------------------------------------------------------
    // Instance Variables

    // The Joe interpreter used when formatting output, etc.
    private final Joe joe;

    // The client-defined mapsTo/f,a,b mapping functions.
    private final Map<Keyword,Mapper> mappers = new HashMap<>();

    //-------------------------------------------------------------------------
    // Constructor

    /**
     * Creates an instance of Nero relative to a specific Joe interpreter.
     * @param joe The interpreter
     */
    public Nero(Joe joe) {
        this.joe = joe;
    }

    /**
     * Creates an instance of Nero relative to a vanilla Joe interpreter.
     */
    public Nero() {
        this(new Joe());
    }

    //-------------------------------------------------------------------------
    // Components

    /**
     * Nero's instance of Joe.
     * @return The Joe interpreter.
     */
    public Joe joe() {
        return joe;
    }

    //-------------------------------------------------------------------------
    // Configuration

    /**
     * Adds a single mapsTo/f,a,b mapper function to Nero. The name
     * must be a valid identifier, which will be exposed in Nero
     * scripts as a keyword.
     * @param name The name
     * @param mapper The mapper
     */
    public void addMapper(String name, Mapper mapper) {
        if (!Joe.isIdentifier(name)) {
            throw new IllegalArgumentException(
                "not an identifier: '" + name + "'.");
        }
        mappers.put(new Keyword(name), mapper);
    }

    /**
     * Gets a read-only map of the defined mapsTo/f,a,b mappers.
     * @return the map
     */
    public Map<Keyword,Mapper> getMappers() {
        return Collections.unmodifiableMap(mappers);
    }

    //-------------------------------------------------------------------------
    // Execution pipeline

    /**
     * Creates a pipeline for the Nero file.
     * @param scriptFile The file's path
     * @return The pipeline
     */
    @SuppressWarnings("unused")
    public Pipeline withFile(Path scriptFile) {
        String script;
        try {
            script = Files.readString(scriptFile);
        } catch (IOException ex) {
            throw new JoeError(
                "Could not read Nero script file from disk:" + ex.getMessage());
        }
        var sourceBuffer =
            new SourceBuffer(scriptFile.getFileName().toString(), script);
        return withScript(sourceBuffer);
    }

    /**
     * Creates a pipeline for the given Nero script.
     * @param script The script
     * @return The pipeline
     */
    public Pipeline withScript(String script) {
        var source = new SourceBuffer("*nero*", script);
        return withScript(source);
    }

    /**
     * Creates a pipeline for the Nero script in the given SourceBuffer.
     * @param source The SourceBuffer
     * @return The pipeline
     */
    public Pipeline withScript(SourceBuffer source) {
        var ruleset = Nero.compile(source);
        return withRules(ruleset);
    }

    /**
     * Creates a pipeline for the given Nero rule set.
     * @param ruleSet the rule set
     * @return The pipeline
     */
    public Pipeline withRules(NeroRuleSet ruleSet) {
        return new Pipeline(this, ruleSet);
    }

    /**
     * A Pipeline is a fluent API for using a rule set.
     */
    public static class Pipeline {
        //---------------------------------------------------------------------
        // Instance Variables

        private final Nero nero;
        private final Joe joe;
        private final NeroRuleSet ruleset;
        private boolean debug = false;
        private final Map<String, Object> parms = new HashMap<>();

        //---------------------------------------------------------------------
        // Constructor

        private Pipeline(Nero nero, NeroRuleSet ruleset) {
            this.nero = nero;
            this.joe = nero.joe;
            this.ruleset = ruleset;
        }

        //---------------------------------------------------------------------
        // Pipeline methods

        /**
         * Sets the Nero debug flag.
         * @param flag true or false
         * @return the pipeline
         */
        public Pipeline debug(boolean flag) {
            this.debug = flag;
            return this;
        }

        /**
         * Sets the Nero debug flag to true.
         * @return the pipeline
         */
        public Pipeline debug() {
            return debug(true);
        }

        /**
         * Defines a query parameter for use by the rule set.  The name
         * must be a valid identifier string.
         * All accumulated query parameters will be visible in the rule set
         * as the fields of a {@code query/...} fact. The {@code query/...} fact
         * will not appear in the output.
         * @param name the parameter name
         * @param value the parameter value
         * @return The pipeline
         */
        public Pipeline queryParm(String name, Object value) {
            if (!Joe.isIdentifier(name)) {
                throw new IllegalArgumentException(
                    "Not an identifier string: '" + name + "'.");
            }
            parms.put(name, value);
            return this;
        }

        /**
         * Defines a collection of query parameters for use by the rule
         * set as a map from parameter name to parameter value.
         * The names must be valid identifier strings.
         * All accumulated query parameters will be visible in the rule set
         * as the fields of a {@code query/...} fact. The {@code query/...} fact
         * will not appear in the output.
         * @param parms the parameter map
         * @return The pipeline
         */
        public Pipeline queryParms(Map<String,Object> parms) {
            for (var name : parms.keySet()) {
                if (!Joe.isIdentifier(name)) {
                    throw new IllegalArgumentException(
                        "Not an identifier string: '" + name + "'.");
                }
            }
            this.parms.putAll(parms);
            return this;
        }

        //---------------------------------------------------------------------
        // Execution methods

        /**
         * Infers and returns all facts from the rule set.
         * @return The facts
         */
        public FactSet infer() {
            var db = new FactSet();
            var engine = new RuleEngine(joe, ruleset, db);
            engine.setDebug(debug);
            engine.addMappers(nero.getMappers());

            var query = new Fact(QUERY, parms);
            if (debug) joe.println("Query Parameters: " + query);
            db.add(query);

            return engine.infer();
        }

        /**
         * Infers all known facts from the rule set and fact set, updating
         * the fact set in place.  Returns the newly inferred facts.  Make an
         * explicit copy of the fact set to retain it unchanged, or call
         * query() instead.
         * @param facts The input facts
         * @return The inferred facts.
         */
        public FactSet update(FactSet facts) {
            var engine = new RuleEngine(joe, ruleset, facts);
            engine.setDebug(debug);
            engine.addMappers(nero.getMappers());
            try {
                var query = new Fact(QUERY, parms);
                if (debug) joe.println("Query Parameters: " + query);
                facts.add(query);
                return engine.infer();
            } finally {
                facts.drop(QUERY);
            }
        }

        /**
         * Infers all known facts from the rule set and provided facts,
         * returning the newly inferred facts.
         * @param inputs The input facts
         * @return The inferred facts.
         */
        public FactSet query(Collection<Fact> inputs) {
            return doQuery(new FactSet(inputs));
        }

        /**
         * Infers all known facts from the rule set and fact set.  Makes
         * a copy of the fact set, ensuring that the input database
         * remains unchanged. Returns the inferred facts.
         * If the input fact set should be updated, or if it doesn't matter,
         * call update() instead.
         * @param inputs The input facts
         * @return The inferred facts.
         */
        public FactSet query(FactSet inputs) {
            return doQuery(new FactSet(inputs));
        }

        private FactSet doQuery(FactSet facts) {
            var engine = new RuleEngine(joe, ruleset, facts);
            engine.setDebug(debug);
            engine.addMappers(nero.getMappers());

            var query = new Fact(QUERY, parms);
            if (debug) joe.println("Query Parameters: " + query);
            facts.add(query);

            return engine.infer();
        }
    }


    //------------------------------------------------------------------------
    // Nero String conversions

    /**
     * Converts the facts into a string in Nero format, if
     * possible.  All fact terms must be expressible as Nero literal terms.
     * @param facts The facts
     * @return The Nero source text
     * @throws JoeError if constraints are not met.
     */
    @SuppressWarnings("unused")
    public String toNeroScript(Collection<Fact> facts) {
        return toNeroScript(new FactSet(facts));
    }

    /**
     * Converts the contents of the FactSet into a string in Nero format, if
     * possible.  All fact terms must be expressible as Nero literal terms.
     * @param db The factSet
     * @return The Nero source text
     * @throws JoeError if constraints are not met.
     */
    public String toNeroScript(FactSet db) {
        var schema = Schema.inferSchema(db.all());
        var buff = new StringBuilder();
        var relations = db.getRelations().stream().sorted().toList();
        var gotFirst = false;

        for (var relation : relations) {
            var shape = schema.get(relation).toSpec();
            if (gotFirst) buff.append("\n");
            buff.append("define ").append(shape).append(";\n");
            gotFirst = true;

            var axioms = db.relation(relation).stream()
                .map(this::toNeroAxiom)
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
    public String toNeroAxiom(Fact fact) {
        var buff = new StringBuilder();
        buff.append(fact.relation()).append("(");

        String terms;

        if (fact.isOrdered()) {
            terms = fact.getFields().stream()
                .map(this::toNeroTerm)
                .collect(Collectors.joining(", "));
        } else {
            var map = new TreeMap<>(fact.getFieldMap());
            terms = map.entrySet().stream()
                .map(e -> e.getKey() + ": " +
                    toNeroTerm(e.getValue()))
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
    public String toNeroTerm(Object term) {
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
                    .map(this::toNeroTerm)
                    .collect(Collectors.joining(", "));
                yield "[" + list + "]";
            }
            case Map<?,?> t -> {
                if (t.isEmpty()) yield "{:}";
                var list = t.entrySet().stream()
                    .map(e -> toNeroTerm(e.getKey()) +
                        ": " + toNeroTerm(e.getValue()))
                    .sorted()
                    .collect(Collectors.joining(", "));
                yield "{" + list + "}";
            }
            case Set<?> t -> {
                var list = t.stream()
                    .map(this::toNeroTerm)
                    .sorted()
                    .collect(Collectors.joining(", "));
                yield "{" + list + "}";
            }
            default -> throw new JoeError(
                "Non-Nero term: '" + joe.stringify(term));
        };
    }

    //-------------------------------------------------------------------------
    // Static API: Parsing and Compiling

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
     * Compiles the script, checking for syntax errors and stratification.
     * Returns the rule set on success and throws an appropriate error
     * on error.
     * @param script The script
     * @return The engine, which has not yet be run.
     * @throws SyntaxError on parse error.
     * @throws JoeError on stratification error.
     */
    @SuppressWarnings("unused")
    public static NeroRuleSet compile(String script) {
        return compile(new SourceBuffer("*nero*", script));
    }

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
     * Gets a static schema from a Nero script.  Throws an error if the
     * script contains anything but static schema definitions.
     * @param script The script
     * @return The schema
     */
    public static Schema schema(String script) {
        var ruleset = parse(new SourceBuffer("*script*", script));
        return schema(ruleset);
    }

    /**
     * Gets a static schema from a rule set.  Throws an error if the rule set
     * contains anything but static schema definitions.
     * @param ruleset The rule set
     * @return The schema
     */
    public static Schema schema(NeroRuleSet ruleset) {
        if (ruleset.rules().isEmpty() &&
            ruleset.axioms().isEmpty() &&
            ruleset.schema().isStatic()
        ) {
            return ruleset.schema();
        }
        throw new JoeError("Expected a static schema.");
    }
}
