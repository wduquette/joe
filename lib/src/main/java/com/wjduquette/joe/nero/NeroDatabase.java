package com.wjduquette.joe.nero;

import com.wjduquette.joe.Joe;
import com.wjduquette.joe.JoeError;
import com.wjduquette.joe.SourceBuffer;
import com.wjduquette.joe.SyntaxError;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Set;

/**
 * A convenience layer for interacting with collections of Nero facts
 * at the Java level.
 */
@SuppressWarnings({"unused", "UnusedReturnValue"})
public class NeroDatabase {
    //-------------------------------------------------------------------------
    // Instance Variables

    // The Nero instance
    private final Nero nero;

    // The accumulated schema
    private Schema schema;

    // The accumulated facts
    private final FactSet db = new FactSet();

    // The debugging flag for inference.
    private boolean debug = false;

    //------------------------------------------------------------------------
    // Constructor

    /**
     * Creates a new database with an empty schema, using an anonymous
     * instance of Joe.
     */
    public NeroDatabase() {
        this(new Nero(), new Schema());
    }

    /**
     * Creates a new database with an empty schema, using an explicit
     * instance of Joe.
     * @param joe The Joe interpreter
     */
    public NeroDatabase(Joe joe) {
        this(new Nero(joe), new Schema());
    }

    /**
     * Creates a new database with a pre-defined schema, using an anonymous
     * instance of Joe.
     * @param schema The schema
     */
    public NeroDatabase(Schema schema) {
        this(new Nero(), schema);
    }

    /**
     * Creates a new database with a pre-defined schema, using an explicit
     * instance of Joe.
     * @param joe The Joe interpreter
     * @param schema The schema
     */
    public NeroDatabase(Joe joe, Schema schema) {
        this.nero = new Nero(joe);
        this.schema = schema;
    }

    private NeroDatabase(Nero nero, Schema schema) {
        this.nero = nero;
        this.schema = schema;
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

    //------------------------------------------------------------------------
    // Operations

    /**
     * Updates the content of the database given the Nero script.
     * @param script The Nero script
     * @return The database
     */
    public NeroDatabase update(String script) {
        return update(new SourceBuffer("*java*", script));
    }

    /**
     * Updates the content of the database given the Nero script.
     * @param source The Nero source
     * @return The database
     * @throws SyntaxError on any Nero compilation error, including schema
     *         mismatch
     * @throws JoeError on any Nero error.
     */
    public NeroDatabase update(SourceBuffer source) {
        var ruleset = Nero.compile(schema, source);
        nero.with(ruleset).debug(debug).update(db);
        schema = ruleset.schema();

        return this;
    }

    /**
     * Updates the content of the database given the Nero file.
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
     * Queries the database given the Nero script.
     * @param script The Nero script
     * @return The inferred facts
     */
    public FactSet query(String script) {
        var ruleset = Nero.compile(schema, new SourceBuffer("*nero*", script));
        return nero.with(ruleset).debug(debug).query(db);
    }

    /**
     * Adds all Facts from another NeroDatabase into the database, checking
     * for schema mismatches.
     * @param other The other database
     * @return this
     * @throws JoeError if there is a schema mismatch
     */
    public NeroDatabase addFacts(NeroDatabase other) {
        schema.merge(other.schema);
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
        // Throws error
        schema.merge(Schema.inferSchema(facts));
        db.addAll(facts);
        return this;
    }

    /**
     * Drops the relation from the database, if it exists.
     * @param relation The relation
     * @return this
     */
    public NeroDatabase drop(String relation) {
        db.drop(relation);
        schema.drop(relation);
        return this;
    }

    //------------------------------------------------------------------------
    // Queries

    /**
     * Gets a copy of the database's schema
     * @return The schema
     */
    public Schema schema() {
        return new Schema(schema);
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
     * Converts the content of the database to a Nero script.
     * Throws an error if a fact in the database contains a data value that
     * cannot be represented in Nero syntax.
     * @return The script
     * @throws JoeError on non-Nero data.
     */
    public String toNeroScript() {
        return Nero.toNeroScript(db);
    }
}
