package com.wjduquette.joe.tools.nero;

import com.wjduquette.joe.JoeError;
import com.wjduquette.joe.SourceBuffer;
import com.wjduquette.joe.Trace;
import com.wjduquette.joe.nero.*;
import com.wjduquette.joe.parser.ASTRuleSet;
import com.wjduquette.joe.parser.Parser;

import java.util.ArrayList;
import java.util.LinkedHashMap;

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
        var ast = parse();

        var ruleset = new RuleSet();

        for (var f : ast.facts()) ruleset.add(ast2fact(f));
        for (var r : ast.rules()) ruleset.add(ast2rule(r));

        return ruleset;
    }

    public ASTRuleSet parse() {
        var parser = new Parser(source, this::errorHandler);
        var ast = parser.parseNero();
        if (gotError) throw new JoeError("Error in Nero input.");
        return ast;
    }

    private void errorHandler(Trace trace, boolean incomplete) {
        gotError = true;
        System.out.println("line " + trace.line() + ": " +
            trace.message());
    }

    //-------------------------------------------------------------------------
    // Compilation

    private ConcreteFact ast2fact(ASTRuleSet.ASTIndexedAtom atom) {
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
            ast2head(rule.head()),
            rule.body().stream().map(this::ast2body).toList(),
            rule.negations().stream().map(this::ast2body).toList(),
            rule.constraints().stream().map(this::ast2constraint).toList()
        );
    }

    private HeadAtom ast2head(ASTRuleSet.ASTIndexedAtom atom) {
        return new HeadAtom(
            atom.relation().lexeme(),
            atom.terms().stream().map(this::ast2term).toList()
        );
    }

    private BodyAtom ast2body(ASTRuleSet.ASTAtom atom) {
        return switch (atom) {
            case ASTRuleSet.ASTIndexedAtom a -> new OrderedAtom(
                a.relation().lexeme(),
                a.terms().stream().map(this::ast2term).toList()
            );
            case ASTRuleSet.ASTNamedAtom a -> {
                var terms = new LinkedHashMap<String,Term>();
                for (var e : a.termMap().entrySet()) {
                    terms.put(e.getKey().lexeme(), ast2term(e.getValue()));
                }
                yield new NamedAtom(a.relation().lexeme(), terms);
            }
        };
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
