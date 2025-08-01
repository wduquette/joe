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
        Set<Fact> facts = ast.axioms().stream().map(this::ast2fact)
            .collect(Collectors.toSet());

        return new RuleSet(ast.schema(), facts, ast.rules());
    }

    //-------------------------------------------------------------------------
    // Compilation

    private Fact ast2fact(Atom atom) {
        return switch (atom) {
            case OrderedAtom a -> {
                var terms = new ArrayList<>();
                for (var t : a.terms()) {
                    if (t instanceof Constant c) {
                        terms.add(c.value());
                    } else {
                        throw new IllegalStateException(
                            "Invalid axiom; atom contains a non-constant term.");
                    }
                }
                var shape = ast.schema().get(a.relation());
                if (shape instanceof Shape.PairShape ps) {
                    yield new PairFact(a.relation(),
                        ps.fieldNames(), terms);
                } else {
                    yield new ListFact(a.relation(), terms);
                }
            }
            case NamedAtom a -> {
                var termMap = new LinkedHashMap<String,Object>();
                for (var e : a.termMap().entrySet()) {
                    var t = e.getValue();
                    if (t instanceof Constant c) {
                        termMap.put(e.getKey(), c.value());
                    } else {
                        throw new IllegalStateException(
                            "Invalid axiom; atom contains a non-constant term.");
                    }
                }
                yield new MapFact(a.relation(), termMap);
            }
        };
    }
}
