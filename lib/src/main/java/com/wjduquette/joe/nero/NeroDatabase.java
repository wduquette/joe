package com.wjduquette.joe.nero;

import com.wjduquette.joe.Joe;
import com.wjduquette.joe.JoeError;
import com.wjduquette.joe.SourceBuffer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * A convenience layer for interacting with collections of Nero facts
 * at the Java level.
 */
public class NeroDatabase {
    //-------------------------------------------------------------------------
    // Instance Variables

    // The Joe instance, which is needed for string conversions.
    private final Joe joe;

    // The accumulated schema
    private Schema schema = new Schema();

    // The accumulated facts
    private FactSet db = new FactSet();

    // The debugging flag for inference.
    private boolean debug = false;

    //------------------------------------------------------------------------
    // Constructor

    public NeroDatabase() {
        this(new Joe());
    }

    public NeroDatabase(Joe joe) {
        this.joe = joe;
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
     * @throws JoeError on schema mismatch.
     * @throws JoeError on any Nero error.
     */
    public NeroDatabase update(SourceBuffer source) {
//        var nero = new Nero(joe);
//
//        // Throws error on Nero compilation or stratification error.
//        var ruleset = nero.compile(source);
//
//        // Throws error on schema incompatibility
//        schema.merge(ruleset.schema());
//
        // TODO
//        RuleEngine.with(joe, ruleset).debug(debug).infer(db);
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
//        var nero = new Nero(joe);
//
//        // Throws error on Nero compilation or stratification error.
//        var ruleset = nero.compile(new SourceBuffer("*java*", script));
//
//        // TODO
        return null;
//        return RuleEngine.with(joe, ruleset)
//            .debug(debug)
//            .infer(new FactSet(db));
    }
}
