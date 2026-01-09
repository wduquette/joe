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
     * validation schema and with the current content.
     * @param ruleset The rule set
     * @param validationSchema A static validation schema
     * @return The database
     * @throws JoeError on any Nero error.
     */
    public NeroDatabase update(NeroRuleSet ruleset, Schema validationSchema) {
        var outputSchema = ruleset.outputSchema();
        if (validationSchema != null) {
            checkCompatibility("given schema", validationSchema, outputSchema);
        }
        checkCompatibility("current content", currentSchema(), outputSchema);
        nero.withRules(ruleset).debug(debug).update(db);
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
        return update(ruleset, null);
    }

    /**
     * Updates the content of the database given the Nero script,
     * verifying that the script's schema is compatible with the
     * validation schema and with the current content
     * @param source The Nero source
     * @param validationSchema A static validation schema
     * @return The database
     * @throws SyntaxError on any Nero compilation error, including schema
     *         mismatch
     * @throws JoeError on any Nero error.
     */
    public NeroDatabase update(SourceBuffer source, Schema validationSchema) {
        var ruleset = Nero.compile(source);
        return update(ruleset, validationSchema);
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
        return update(source, null);
    }

    /**
     * Updates the content of the database given the Nero script,
     * verifying that the script's schema is compatible with the
     * validation schema and with the current content.
     * @param script The Nero script
     * @param validationSchema The static validation schema
     * @return The database
     */
    public NeroDatabase update(String script, Schema validationSchema) {
        return update(new SourceBuffer("*java*", script), validationSchema);
    }

    /**
     * Updates the content of the database given the Nero script,
     * verifying that the script's schema is compatible with the
     * current content.
     * @param script The Nero script
     * @return The database
     */
    public NeroDatabase update(String script) {
        return update(new SourceBuffer("*java*", script), null);
    }

    /**
     * Updates the content of the database given the Nero file,
     * verifying that the script's schema is compatible with the
     * validation schema and with the current content.
     * @param scriptFile The Nero file
     * @param validationSchema The static validation schema
     * @return The database
     */
    public NeroDatabase load(Path scriptFile, Schema validationSchema) {
        String script;
        try {
            script = Files.readString(scriptFile);
        } catch (IOException ex) {
            throw new JoeError(
                "Could not read Nero script file from disk:" + ex.getMessage());
        }
        var sourceBuffer =
            new SourceBuffer(scriptFile.getFileName().toString(), script);
        return update(sourceBuffer, validationSchema);
    }

    /**
     * Updates the content of the database given the Nero file,
     * verifying that the script's schema is compatible with the
     * current content.
     * @param scriptFile The Nero file
     * @return The database
     */
    public NeroDatabase load(Path scriptFile) {
        String script;
        try {
            script = Files.readString(scriptFile);
        } catch (IOException ex) {
            throw new JoeError(
                "Could not read Nero script file from disk:" + ex.getMessage());
        }
        var sourceBuffer =
            new SourceBuffer(scriptFile.getFileName().toString(), script);
        return update(sourceBuffer);
    }

    /**
     * Queries the database given the Nero script, verifying that the
     * query is compatible with the database's current content.
     * @param script The Nero script
     * @return The inferred facts
     */
    public FactSet query(String script) {
        return query(Nero.compile(new SourceBuffer("*nero*", script)));
    }

    /**
     * Queries the database given the Nero rule set, verifying that the
     * query is compatible with the database's current content.
     * @param ruleset The rule set
     * @return The inferred facts
     */
    public FactSet query(NeroRuleSet ruleset) {
        checkCompatibility("current content", currentSchema(), ruleset.outputSchema());
        return nero.withRules(ruleset).debug(debug).query(db);
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
