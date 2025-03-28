package com.wjduquette.joe.nero;

import com.wjduquette.joe.*;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

/**
 * Nero is the public entry point for parsing and executing Nero
 * code.
 */
public class Nero {
    //-------------------------------------------------------------------------
    // Instance Variables

    private final Joe joe;
    private boolean gotError = false;

    //-------------------------------------------------------------------------
    // Constructor

    public Nero(Joe joe) {
        this.joe = joe;
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
        var clauses = parser.parse();
        if (gotError) throw new JoeError("Error in Nero input.");

        var baseFacts = new ArrayList<Fact>();
        var rules = new ArrayList<Rule>();

        System.out.println("Input program:");
        for (var clause : clauses) {
            System.out.println("  " + clause);
            switch (clause) {
                case Clause.FactClause f -> baseFacts.add(f.asFact());
                case Clause.RuleClause f -> rules.add(f.asRule());
            }
        }

        var ruleset = new RuleSet(rules, baseFacts);
        // Could add more facts.

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
