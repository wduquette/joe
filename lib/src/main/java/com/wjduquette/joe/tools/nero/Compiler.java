package com.wjduquette.joe.tools.nero;

import com.wjduquette.joe.JoeError;
import com.wjduquette.joe.SourceBuffer;
import com.wjduquette.joe.Trace;
import com.wjduquette.joe.nero.*;
import com.wjduquette.joe.nero.parser.ASTRuleSet;
import com.wjduquette.joe.nero.parser.Parser;
import com.wjduquette.joe.nero.parser.Scanner;

import java.util.ArrayList;

/**
 * Compiles a Nero {@link com.wjduquette.joe.nero.RuleSet} from source.
 */
public class Compiler {
    //-------------------------------------------------------------------------
    // Instance Variables

    // The input source
    private final SourceBuffer source;
    private boolean gotError = false;

    //-------------------------------------------------------------------------
    // Constructor

    public Compiler(SourceBuffer source) {
        this.source = source;
    }

    //-------------------------------------------------------------------------
    // Public API

    /**
     * Compiles the Nero source, producing a rule set.
     * @return The RuleSet
     * @throws JoeError on compilation failure.
     */
    public RuleSet compile() {
        gotError = false;

        var scanner = new Scanner(source, this::errorHandler);
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

    private void errorHandler(Trace trace) {
        gotError = true;
        System.out.println("line " + trace.line() + ": " +
            trace.message());
    }

    //-------------------------------------------------------------------------
    // Compilation

    private ConcreteFact ast2fact(ASTRuleSet.ASTAtom atom) {
        var terms = new ArrayList<>();
        for (var t : atom.terms()) {
            if (t instanceof ASTRuleSet.ASTConstant c) {
                terms.add(c.token().literal());
            } else {
                throw new IllegalStateException(
                    "Invalid fact; Atom contains a non-constant term.");
            }
        }

        return new ConcreteFact(atom.relation().lexeme(), terms);
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
}
