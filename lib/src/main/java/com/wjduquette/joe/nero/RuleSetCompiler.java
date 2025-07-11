package com.wjduquette.joe.nero;

import com.wjduquette.joe.JoeError;
import com.wjduquette.joe.parser.ASTRuleSet;
import com.wjduquette.joe.scanner.TokenType;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Compiles a Nero {@link RuleSet} from an
 * {@link com.wjduquette.joe.parser.ASTRuleSet} given its configuration.
 */
public class RuleSetCompiler {
    //-------------------------------------------------------------------------
    // Instance Variables

    // The AST
    private final ASTRuleSet ast;

    //-------------------------------------------------------------------------
    // Constructor

    public RuleSetCompiler(ASTRuleSet ast) {
        this.ast = ast;
    }

    //-------------------------------------------------------------------------
    // Public API

    /**
     * Compiles the Nero source, producing a rule set.
     * @return The RuleSet
     * @throws JoeError on compilation failure.
     */
    public RuleSet compile() {
        Set<Fact> facts = ast.facts().stream().map(this::ast2fact)
            .collect(Collectors.toSet());
        Set<Rule> rules = ast.rules().stream().map(this::ast2rule)
            .collect(Collectors.toSet());

        return new RuleSet(facts, rules);
    }

    //-------------------------------------------------------------------------
    // Compilation

    private Fact ast2fact(ASTRuleSet.ASTOrderedAtom atom) {
        var terms = new ArrayList<>();
        for (var t : atom.terms()) {
            if (t instanceof ASTRuleSet.ASTConstant c) {
                terms.add(ast2constant(c).value());
            } else {
                throw new IllegalStateException(
                    "Invalid fact; Atom contains a non-constant term.");
            }
        }

        return new ListFact(atom.relation().lexeme(), terms);
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
        return switch (atom) {
            case ASTRuleSet.ASTOrderedAtom a -> new OrderedAtom(
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
            case ASTRuleSet.ASTConstant c -> ast2constant(c);
            case ASTRuleSet.ASTVariable v -> new Variable(v.token().lexeme());
            case ASTRuleSet.ASTWildcard w -> new Wildcard(w.token().lexeme());
        };
    }

    private Constant ast2constant(ASTRuleSet.ASTConstant constant) {
        var value = switch (constant.token().type()) {
            case TokenType.TRUE -> true;
            case TokenType.FALSE -> false;
            case TokenType.NULL -> null;
            default -> constant.token().literal();
        };

        return new Constant(value);
    }
}
