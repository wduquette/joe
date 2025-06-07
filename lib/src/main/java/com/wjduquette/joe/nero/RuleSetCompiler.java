package com.wjduquette.joe.nero;

import com.wjduquette.joe.JoeError;
import com.wjduquette.joe.parser.ASTRuleSet;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Compiles a Nero {@link RuleSet} from an
 * {@link com.wjduquette.joe.parser.ASTRuleSet} given its configuration.
 */
public class RuleSetCompiler {
    /**
     * The default fact factory used by the compiler; it creates
     * {@link ConcreteFact} objects.
     */
    public static final FactFactory DEFAULT_FACT_FACTORY = ConcreteFact::new;

    //-------------------------------------------------------------------------
    // Instance Variables

    // The AST
    private final ASTRuleSet ast;

    // The factory to use to create fact values
    private FactFactory factFactory = DEFAULT_FACT_FACTORY;

    //-------------------------------------------------------------------------
    // Constructor

    public RuleSetCompiler(ASTRuleSet ast) {
        this.ast = ast;
    }

    //-------------------------------------------------------------------------
    // Public API


    @SuppressWarnings("unused")
    public FactFactory getFactFactory() {
        return factFactory;
    }

    public void setFactFactory(FactFactory factFactory) {
        this.factFactory = factFactory;
    }

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
                terms.add(c.token().literal());
            } else {
                throw new IllegalStateException(
                    "Invalid fact; Atom contains a non-constant term.");
            }
        }

        return factFactory.create(atom.relation().lexeme(), terms);
    }

    private Rule ast2rule(ASTRuleSet.ASTRule rule) {
        return new Rule(
            ast2head(rule.head()),
            rule.body().stream().map(this::ast2body).toList(),
            rule.negations().stream().map(this::ast2body).toList(),
            rule.constraints().stream().map(this::ast2constraint).toList()
        );
    }

    private HeadAtom ast2head(ASTRuleSet.ASTOrderedAtom atom) {
        return new HeadAtom(
            atom.relation().lexeme(),
            atom.terms().stream().map(this::ast2term).toList()
        );
    }

    private BodyAtom ast2body(ASTRuleSet.ASTAtom atom) {
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
            case ASTRuleSet.ASTConstant c -> new Constant(c.token().literal());
            case ASTRuleSet.ASTVariable v -> new Variable(v.token().lexeme());
            case ASTRuleSet.ASTWildcard w -> new Wildcard(w.token().lexeme());
        };
    }
}
