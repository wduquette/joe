package com.wjduquette.joe.nero;

import com.wjduquette.joe.*;
import com.wjduquette.joe.nero.parser.Parser;
import com.wjduquette.joe.nero.parser.Scanner;
import com.wjduquette.joe.SourceBuffer;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

/**
 * Nero is the public entry point for parsing and executing Nero
 * code.
 */
public class OldNero {
    //-------------------------------------------------------------------------
    // Instance Variables

    private boolean gotError = false;

    //-------------------------------------------------------------------------
    // Constructor

    public OldNero() {
        // Nothing to do.
    }

    //-------------------------------------------------------------------------
    // Public API

    /**
     * Just a convenient entry point for getting some source code into
     * the module.  This will undoubtedly change a lot over time.
     *
     * @param buff The Nero source.
     * @throws JoeError if the script could not be compiled.
     */
    public void parse(SourceBuffer buff) {
        gotError = false;

        var scanner = new Scanner(buff, this::errorHandler);
        var tokens = scanner.scanTokens();
        if (gotError) throw new JoeError("Error in Nero input.");

        var parser = new Parser(tokens, this::errorHandler);
        var ast = parser.parse();
        if (gotError) throw new JoeError("Error in Nero input.");

        var baseFacts = new ArrayList<Fact>();
        var rules = new ArrayList<Rule>();

        System.out.println("Input program:");
        for (var fact : ast.facts()) {
            System.out.println("  " + fact);
            baseFacts.add(fact.asFact());
        }
        for (var rule : ast.rules()) {
            System.out.println("  " + rule);
            rules.add(rule.asRule());
        }

        // Will throw JoeError if the rules aren't stratified.
        var ruleset = new Engine(rules, baseFacts);

        // Could add more facts here, in theory.

        try {
            ruleset.ponder();
            System.out.println("\nKnown facts:");
            ruleset.getKnownFacts().stream().map(Fact::toString).sorted()
                .forEach(f -> System.out.println("  " + f));

        } catch (Exception ex) {
            System.out.println("Error in ruleset: " + ex);
        }
    }

    /**
     * Processes the given file in some way.
     * @param scriptPath The file's path
     * @throws IOException if the file cannot be read.
     * @throws JoeError if the script could not be compiled.
     */
    public void parseFile(String scriptPath)
        throws IOException, SyntaxError
    {
        var path = Paths.get(scriptPath);
        byte[] bytes = Files.readAllBytes(path);
        var script = new String(bytes, Charset.defaultCharset());

        parse(new SourceBuffer(path.getFileName().toString(), script));
    }

    private void errorHandler(Trace trace) {
        gotError = true;
        System.out.println("line " + trace.line() + ": " +
            trace.message());
    }
}
