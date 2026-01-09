package com.wjduquette.joe.nero;

import com.wjduquette.joe.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * A convenience layer for interacting with collections of Nero facts
 * at the Java level.  NeroDatabase allows the client to work with a
 * collection of facts, preserving the schema as it goes along.
 */
@SuppressWarnings({"unused", "UnusedReturnValue"})
public class NeroDatabase {
    //-------------------------------------------------------------------------
    // Instance Variables

    // The Nero instance
    private final Nero nero;

    // The accumulated facts
    private final FactSet db = new FactSet();

    // The debugging flag for inference.
    private boolean debug = false;

    //------------------------------------------------------------------------
    // Constructor

    /**
     * Creates a new database using an anonymous
     * instance of Joe.
     */
    public NeroDatabase() {
        this(new Nero());
    }

    /**
     * Creates a new database using an explicit
     * instance of Joe.
     * @param joe The Joe interpreter
     */
    public NeroDatabase(Joe joe) {
        this(new Nero(joe));
    }

    private NeroDatabase(Nero nero) {
        this.nero = nero;
    }

    //------------------------------------------------------------------------
    // Configuration

    /**
     * Gets whether debugging output is enabled or not.
     * @return true or false
     */
    public boolean isDebug() {
        return debug;
    }

    /**
     * Sets whether debugging output is enabled or not.
     * @param debug true or false
     * @return The database
     */
    public NeroDatabase setDebug(boolean debug) {
        this.debug = debug;
        return this;
    }

    /**
     * Adds a single mapsTo/f,a,b mapper function to NeroDatabase. The name
     * must be a valid identifier, which will be exposed in Nero
     * scripts as a keyword.
     * @param name The name
     * @param mapper The mapper
     */
    public void addMapper(String name, Mapper mapper) {
        nero.addMapper(name, mapper);
    }

    /**
     * Gets a read-only map of the defined mapsTo/f,a,b mappers.
     * @return the map
     */
    public Map<Keyword,Mapper> getMappers() {
        return nero.getMappers();
    }

    //------------------------------------------------------------------------
    // Operations

    /**
     * Clears all content from the database.
     * @return The database
     */
    public NeroDatabase clear() {
        db.clear();
        return this;
    }

    /**
     * Updates the content of the database given the rule set,
     * verifying that the rule set's schema is compatible with the
     * current content.
     * @param ruleset The rule set
     * @return The database
     * @throws JoeError on any Nero error.
     */
    public NeroDatabase update(NeroRuleSet ruleset) {
        return withRules(ruleset).update();
    }

    /**
     * Updates the content of the database given the Nero script,
     * verifying that the script's schema is compatible with the
     * current content.
     * @param source The Nero source
     * @return The database
     * @throws SyntaxError on any Nero compilation error, including schema
     *         mismatch
     * @throws JoeError on any Nero error.
     */
    public NeroDatabase update(SourceBuffer source) {
        return withScript(source).update();
    }

    /**
     * Updates the content of the database given the Nero script,
     * verifying that the script's schema is compatible with the
     * current content.
     * @param script The Nero script
     * @return The database
     */
    public NeroDatabase update(String script) {
        return withScript(script).update();
    }

    /**
     * Updates the content of the database given the Nero file,
     * verifying that the script's schema is compatible with the
     * current content.
     * @param scriptFile The Nero file
     * @return The database
     */
    public NeroDatabase load(Path scriptFile) {
        return withFile(scriptFile).update();
    }

    /**
     * Queries the database given the Nero script, verifying that the
     * query is compatible with the database's current content.
     * @param script The Nero script
     * @return The inferred facts
     */
    public FactSet query(String script) {
        return withScript(script).query();
    }

    /**
     * Queries the database given the Nero rule set, verifying that the
     * query is compatible with the database's current content.
     * @param ruleset The rule set
     * @return The inferred facts
     */
    public FactSet query(NeroRuleSet ruleset) {
        return withRules(ruleset).query();
    }

    /**
     * Returns a pipeline for processing the Nero rule set found in the
     * script file.  Checks the rule set for compatibility with the
     * current database content.
     * @param scriptFile The path
     * @return The pipeline
     */
    public Pipeline withFile(Path scriptFile) {
        String script;
        try {
            script = Files.readString(scriptFile);
        } catch (IOException ex) {
            throw new JoeError(
                "Could not read Nero script file from disk:" + ex.getMessage());
        }
        return withScript(
            new SourceBuffer(scriptFile.getFileName().toString(), script));
    }

    /**
     * Returns a pipeline for processing the Nero rule set found in the
     * script.  Checks the rule set for compatibility with the
     * current database content.
     * @param script The script
     * @return The pipeline
     */
    public Pipeline withScript(String script) {
        return withScript(new SourceBuffer("*script", script));
    }

    /**
     * Returns a pipeline for processing the Nero rule set found in the
     * buffer.  Checks the rule set for compatibility with the
     * current database content.
     * @param source The buffer
     * @return The pipeline
     */
    public Pipeline withScript(SourceBuffer source) {
        return withRules(Nero.compile(source));
    }

    /**
     * Returns a pipeline for processing the Nero rule set.
     * Checks the rule set for compatibility with the
     * current database content.
     * @param rules The rule set
     * @return The pipeline
     */
    public Pipeline withRules(NeroRuleSet rules) {
        return new Pipeline(this, rules);
    }

    /**
     * Adds all Facts from another NeroDatabase into the database, checking
     * for schema mismatches.
     * @param other The other database
     * @return this
     * @throws JoeError if there is a schema mismatch
     */
    public NeroDatabase addFacts(NeroDatabase other) {
        checkNewFacts(other.db.all());
        addFacts(other.db);
        return this;
    }

    /**
     * Adds all Facts from a FactSet into the database, checking for schema
     * mismatches.
     * @param factSet The fact set
     * @return this
     * @throws JoeError if there is a schema mismatch
     */
    public NeroDatabase addFacts(FactSet factSet) {
        return addFacts(factSet.all());
    }

    /**
     * Adds a collection of Facts into the database, checking for schema
     * mismatches.
     * @param facts The facts
     * @return this
     * @throws JoeError if there is a schema mismatch
     */
    public NeroDatabase addFacts(Collection<Fact> facts) {
        checkNewFacts(facts);
        db.addAll(facts);
        return this;
    }

    // Verifies that all incoming facts are compatible with the existing
    // facts and with each other, i.e., all incoming facts with a given
    // relation have the same shape.
    private void checkNewFacts(Collection<Fact> facts) {
        var schema = currentSchema();

        for (var fact : facts) {
            // Throws an error for an incompatible fact.
            if (!schema.checkAndAdd(fact)) {
                throw new JoeError(
                    "Added fact is incompatible with current content, " +
                    "expected shape '" + schema.get(fact.relation()) +
                    "', got fact: '" + fact +  "'.");
            }
        }
    }

    /**
     * Drops the relation from the database, if it exists.
     * @param relation The relation
     * @return this
     */
    public NeroDatabase drop(String relation) {
        db.drop(relation);
        return this;
    }

    /**
     * Deletes a single fact from the database.
     * @param fact the fact
     * @return this
     */
    public NeroDatabase remove(Fact fact) {
        db.remove(fact);
        return this;
    }

    /**
     * Deletes a collection of facts from the database.
     * @param collection The facts
     * @return this
     */
    public NeroDatabase removeAll(Collection<Fact> collection) {
        db.removeAll(collection);
        return this;
    }

    /**
     * Deletes the facts in another FactBase from the database.
     * @param other The other FactBase
     * @return this
     */
    public NeroDatabase removeAll(FactSet other) {
        db.removeAll(other);
        return this;
    }

    /**
     * Renames a relation, replacing any existing relation that has the new
     * name.
     * @param oldName The old name
     * @param newName The new name
     * @return this
     */
    public NeroDatabase rename(String oldName, String newName) {
        db.rename(oldName, newName);
        return this;
    }

    //------------------------------------------------------------------------
    // Queries

    /**
     * Computes a schema that reflects the current content of the database.
     * @return The schema
     */
    public Schema currentSchema() {
        // Assumes that each relation is homogeneous, and just get the shape
        // from one fact of each relation.
        var schema = new Schema();
        for (var name : getRelations()) {
            relation(name).stream()
                .findAny().map(Fact::shape)
                .ifPresent(schema::add);
        }
        return schema;
    }


    /**
     * Gets whether the database is empty or not.
     * @return true or false
     */
    public boolean isEmpty() {
        return db.isEmpty();
    }

    /**
     * Gets the number of facts in the database.
     * @return the size
     */
    public int size() {
        return db.size();
    }

    /**
     * Returns all facts from the database.
     * @return The facts
     */
    public Set<Fact> all() {
        return db.all();
    }


    /**
     * Returns all facts having the given relation.
     * @param name The relation
     * @return The facts
     */
    public Set<Fact> relation(String name) {
        return db.relation(name);
    }

    /**
     * Returns a set of the relations of all facts in the database.
     * @return the set
     */
    public Set<String> getRelations() {
        return db.getRelations();
    }

    /**
     * Converts the facts into a string in Nero format.
     * Throws an error if a fact in the database contains a data value that
     * cannot be represented in Nero syntax.
     * @param facts The facts
     * @return The Nero source text
     * @throws JoeError if constraints are not met.
     */
    @SuppressWarnings("unused")
    public String toNeroScript(Collection<Fact> facts) {
        return nero.toNeroScript(facts);
    }

    /**
     * Converts the content of the database to a Nero script.
     * Throws an error if a fact in the database contains a data value that
     * cannot be represented in Nero syntax.
     * @return The script
     * @throws JoeError on non-Nero data.
     */
    public String toNeroScript() {
        return nero.toNeroScript(db);
    }

    /**
     * Converts the content of the fact set to a Nero script.
     * Throws an error if a fact in the database contains a data value that
     * cannot be represented in Nero syntax.
     * @param facts The fact set
     * @return The script
     * @throws JoeError on non-Nero data.
     */
    public String toNeroScript(FactSet facts) {
        return nero.toNeroScript(facts);
    }

    /**
     * Converts the given fact to a Nero axiom.
     * Throws an error if the axiom contains a data value that
     * cannot be represented in Nero syntax.
     * @param fact The fact
     * @return The script
     * @throws JoeError on non-Nero data.
     */
    public String toNeroAxiom(Fact fact) {
        return nero.toNeroAxiom(fact);
    }

    //------------------------------------------------------------------------
    // Pipeline

    /**
     * A Pipeline is a fluent API for working with the database.
     */
    public static class Pipeline {
        //---------------------------------------------------------------------
        // Instance Variables

        private final NeroDatabase database;
        private final NeroRuleSet ruleset;
        private boolean debug;

        //---------------------------------------------------------------------
        // Constructor

        private Pipeline(NeroDatabase database, NeroRuleSet ruleset) {
            this.database = database;
            this.ruleset = ruleset;
            this.debug = database.debug;
            database.checkCompatibility("current content",
                database.currentSchema(),
                ruleset.outputSchema());
        }

        //---------------------------------------------------------------------
        // Pipeline methods

        public Pipeline check(Schema validationSchema) {
            database.checkCompatibility(
                "given schema",
                validationSchema,
                ruleset.outputSchema());
            return this;
        }

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

        //---------------------------------------------------------------------
        // Execution methods

        /**
         * Updates the content of the database given the rule set,
         * verifying that the script's schema is compatible with the
         * current content.
         * @return The database
         */
        public NeroDatabase update() {
            database.nero.withRules(ruleset)
                .debug(debug)
                .update(database.db);
            return database;
        }

        /**
         * Infers all known facts from the rule set and database content,
         * returning the newly inferred facts.
         * @return The inferred facts.
         */
        public FactSet query() {
            return database.nero.withRules(ruleset)
                .debug(debug)
                .query(database.db);
        }
    }


    //------------------------------------------------------------------------
    // Utilities

    private void checkCompatibility(String text, Schema expected, Schema got) {
        for (var name : expected.getRelations()) {
            if (expected.hasRelation(name)) {
                var e = expected.get(name);
                var s = got.get(name);
                if (s != null && !e.equals(s)) {
                    throw new JoeError(
                        "Rule set is incompatible with " + text + ", expected '" +
                        e + "', got: '" + s + "'.");
                }
            }
        }
    }
}
