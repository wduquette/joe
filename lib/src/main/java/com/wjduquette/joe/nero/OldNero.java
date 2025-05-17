package com.wjduquette.joe.nero;

import com.wjduquette.joe.*;
import com.wjduquette.joe.nero.parser.ASTRuleSet;
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

    // TODO: Move to Compiler
    public RuleSet compile(SourceBuffer buff) {
        gotError = false;

        var scanner = new Scanner(buff, this::errorHandler);
        var tokens = scanner.scanTokens();
        if (gotError) throw new JoeError("Error in Nero input.");

        var parser = new Parser(tokens, this::errorHandler);
        var ast = parser.parse();
        if (gotError) throw new JoeError("Error in Nero input.");

        System.out.println("Program:");
        System.out.println(ast.toString().indent(2));

        var ruleset = new RuleSet();

        for (var f : ast.facts()) ruleset.add(ast2fact(f));
        for (var r : ast.rules()) ruleset.add(ast2rule(r));

        return ruleset;
    }

    private Fact ast2fact(ASTRuleSet.ASTAtom atom) {
        var terms = new ArrayList<>();
        for (var t : atom.terms()) {
            if (t instanceof ASTRuleSet.ASTConstant c) {
                terms.add(c.token().literal());
            } else {
                throw new IllegalStateException(
                    "Invalid fact; Atom contains a non-constant term.");
            }
        }

        return new Fact(atom.relation().lexeme(), terms);
    }

    private Rule ast2rule(ASTRuleSet.ASTRule rule) {
        return new Rule(
            ast2atom(rule.head()),
            rule.body().stream().map(this::ast2atom).toList(),
            rule.negations().stream().map(this::ast2atom).toList(),
            rule.constraints().stream().map(this::ast2constraint).toList()
        );
    }

    private Atom ast2atom(ASTRuleSet.ASTAtom atom) {
        return new Atom(
            atom.relation().lexeme(),
            atom.terms().stream().map(this::ast2term).toList()
        );
    }

    private Constraint ast2constraint(ASTRuleSet.ASTConstraint constraint) {
        var realOp = switch (constraint.op().type()) {
            case BANG_EQUAL -> Constraint.Op.NE;
            case EQUAL_EQUAL -> Constraint.Op.EQ;
            case GREATER -> Constraint.Op.GT;
            case GREATER_EQUAL -> Constraint.Op.GE;
            case LESS -> Constraint.Op.LT;
            case LESS_EQUAL -> Constraint.Op.LE;
            default -> throw new IllegalStateException(
                "Unknown operator token: " + constraint.op());
        };
        return new Constraint(
            (Variable)ast2term(constraint.a()),
            realOp,
            ast2term(constraint.b())
        );
    }

    private Term ast2term(ASTRuleSet.ASTTerm term) {
        return switch (term) {
            case ASTRuleSet.ASTConstant c -> new Constant(c.token().literal());
            case ASTRuleSet.ASTVariable v -> new Variable(v.token().lexeme());
            case ASTRuleSet.ASTWildcard w -> new Wildcard(w.token().lexeme());
        };
    }

    /**
     * Just a convenient entry point for getting some source code into
     * the module.  This will undoubtedly change a lot over time.
     *
     * @param buff The Nero source.
     * @throws JoeError if the script could not be compiled.
     */
    public void execute(SourceBuffer buff) {
        // FIRST, compile the source.
        var ruleset = compile(buff);

        // Will throw JoeError if the rules aren't stratified.
        var engine = new Engine(ruleset);

        try {
            engine.infer();
            System.out.println("\nKnown facts:");
            engine.getKnownFacts().stream().map(Fact::toString).sorted()
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
    public void executeFile(String scriptPath)
        throws IOException, SyntaxError
    {
        var path = Paths.get(scriptPath);
        byte[] bytes = Files.readAllBytes(path);
        var script = new String(bytes, Charset.defaultCharset());

        execute(new SourceBuffer(path.getFileName().toString(), script));
    }

    private void errorHandler(Trace trace) {
        gotError = true;
        System.out.println("line " + trace.line() + ": " +
            trace.message());
    }
}
