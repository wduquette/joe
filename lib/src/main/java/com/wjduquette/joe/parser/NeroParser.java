package com.wjduquette.joe.parser;

import com.wjduquette.joe.nero.Schema;
import com.wjduquette.joe.nero.Shape;
import com.wjduquette.joe.scanner.Token;

import java.util.*;
import java.util.function.Supplier;

import static com.wjduquette.joe.scanner.TokenType.*;

/**
 * A parser for Nero, which is embedded in the Joe parser.
 * Intentionally package-private.
 */
class NeroParser extends EmbeddedParser {
    private static final String DEFINE = "define";

    //-------------------------------------------------------------------------
    // Types

    /**
     * Whether Nero is parsed as a standalone language, or as embedded in Joe.
     */
    public enum Mode {
        /** Mode */ STANDALONE,
        /** Mode */ EMBEDDED
    }

    //-------------------------------------------------------------------------
    // Instance Variables

    // Standalone or embedded
    private final Mode mode;

    //-------------------------------------------------------------------------
    // Constructor

    public NeroParser(Parser parent, Mode mode) {
        super(parent);
        this.mode = mode;
    }

    //-------------------------------------------------------------------------
    // API

    /**
     * Parses Nero code, using an appropriate condition for returning control
     * to the client.
     * @return The rule set
     */
    @SuppressWarnings("Convert2MethodRef")
    public ASTRuleSet parse() {
        Supplier<Boolean> endCondition = mode == Mode.STANDALONE
            ? () -> scanner.isAtEnd()
            : () -> scanner.match(RIGHT_BRACE);

        return parse(endCondition);
    }

    //-------------------------------------------------------------------------
    // The Parser

    private ASTRuleSet parse(Supplier<Boolean> endCondition) {
        List<ASTRuleSet.ASTAtom> facts = new ArrayList<>();
        List<ASTRuleSet.ASTRule> rules = new ArrayList<>();
        var schema = new Schema();

        while (!endCondition.get()) {
            try {
                if (scanner.matchIdentifier(DEFINE)) {
                    defineDeclaration(schema);
                    continue;
                }
                var headToken = scanner.peek();
                var head = atom();

                if (scanner.match(SEMICOLON)) {
                    if (!schema.checkAndAdd(head)) {
                        error(headToken,
                            "axiom's shape is incompatible with previous definitions for this relation.");
                    }
                    facts.add(axiom(head));
                } else if (scanner.match(COLON_MINUS)) {
                    if (!schema.checkAndAdd(head)) {
                        error(headToken,
                            "rule head's shape is incompatible with previous definitions for this relation.");
                    }
                    rules.add(rule(head));
                } else {
                    scanner.advance();
                    throw errorSync(scanner.previous(),
                        "expected axiom or rule.");
                }
            } catch (Parser.ErrorSync error) {
                synchronize();
            }
        }

        // No exports; return an empty map.
        return new ASTRuleSet(schema, facts, rules);
    }

    private void defineDeclaration(Schema schema) {
        scanner.consume(IDENTIFIER, "expected relation after 'define'.");
        var relation = scanner.previous();
        scanner.consume(SLASH, "expected '/' after relation.");

        Shape shape;

        if (scanner.match(NUMBER)) {
            var arity = (Double)scanner.previous().literal();
            if (arity - arity.intValue() != 0.0) {
                error(scanner.previous(), "expected integer arity.");
            }
            if (arity <= 0) {
                error(scanner.previous(), "expected positive arity.");
            }
            shape = new Shape.ListShape(relation.lexeme(), arity.intValue());
        } else if (scanner.match(DOT_DOT_DOT)) {
            shape = new Shape.MapShape(relation.lexeme());
        } else if (scanner.check(IDENTIFIER)) {
            var names = new ArrayList<String>();
            do {
                scanner.consume(IDENTIFIER, "expected field name.");
                var name = scanner.previous().lexeme();
                if (names.contains(name)) {
                    error(scanner.previous(), "duplicate field name.");
                }
                names.add(name);
            } while (scanner.match(COMMA));

            shape = new Shape.PairShape(relation.lexeme(), names);
        } else {
            scanner.advance();
            throw errorSync(scanner.previous(),
                "expected arity, '...', or field names.");
        }
        scanner.consume(SEMICOLON, "expected ';' after definition.");

        if (!schema.checkAndAdd(shape)) {
            error(relation, "definition clashes with earlier entry.");
        }
    }

    private ASTRuleSet.ASTAtom axiom(ASTRuleSet.ASTAtom head) {
        // Verify that there are no non-constant terms.
        for (var term : head.getTerms()) {
            if (!(term instanceof ASTRuleSet.ASTConstant)) {
                error(term.token(), "fact contains a non-constant term.");
            }
        }
        return head;
    }

    private ASTRuleSet.ASTRule rule(ASTRuleSet.ASTAtom head) {
        var body = new ArrayList<ASTRuleSet.ASTAtom>();
        var negations = new ArrayList<ASTRuleSet.ASTAtom>();
        var constraints = new ArrayList<ASTRuleSet.ASTConstraint>();

        var bodyVar = new HashSet<String>();
        do {
            var negated = scanner.match(NOT);

            var atom = atom();
            if (negated) {
                for (var name : atom.getVariableTokens()) {
                    if (!bodyVar.contains(name.lexeme())) {
                        error(name, "negated body atom contains unbound variable.");
                    }
                }
                negations.add(atom);
            } else {
                body.add(atom);
                bodyVar.addAll(atom.getVariableNames());
            }
        } while (scanner.match(COMMA));

        if (scanner.match(WHERE)) {
            do {
                constraints.add(constraint(bodyVar));
            } while (scanner.match(COMMA));
        }

        scanner.consume(SEMICOLON, "expected ';' after rule body.");

        // Verify that the head contains only valid terms.
        for (var term : head.getTerms()) {
            switch (term) {
                case ASTRuleSet.ASTConstant ignored -> {}
                case ASTRuleSet.ASTVariable v -> {
                    if (!bodyVar.contains(v.token().lexeme())) {
                        error(v.token(),
                            "head atom contains unbound variable.");
                    }
                }
                case ASTRuleSet.ASTWildcard w -> error(w.token(),
                    "head atom contains wildcard.");
            }
        }

        return new ASTRuleSet.ASTRule(head, body, negations, constraints);
    }

    private ASTRuleSet.ASTConstraint constraint(Set<String> bodyVar) {
        var term = term();
        Token op;
        ASTRuleSet.ASTVariable a = null;

        switch (term) {
            case ASTRuleSet.ASTConstant c ->
                error(c.token(), "expected bound variable.");
            case ASTRuleSet.ASTVariable v -> {
                a = v;
                if (!bodyVar.contains(v.token().lexeme())) {
                    error(v.token(), "expected bound variable.");
                }
            }
            case ASTRuleSet.ASTWildcard c ->
                error(c.token(), "expected bound variable.");
        }

        if (scanner.match(
            BANG_EQUAL, EQUAL_EQUAL,
            GREATER, GREATER_EQUAL,
            LESS, LESS_EQUAL)
        ) {
            op = scanner.previous();
        } else {
            scanner.advance();
            throw errorSync(scanner.previous(), "expected comparison operator.");
        }

        var b = term();

        if (b instanceof ASTRuleSet.ASTVariable v) {
            if (!bodyVar.contains(v.token().lexeme())) {
                error(v.token(), "expected bound variable or constant.");
            }
        } else if (b instanceof ASTRuleSet.ASTWildcard w) {
            error(w.token(), "expected bound variable or constant.");
        }

        return new ASTRuleSet.ASTConstraint(a, op, b);
    }

    private ASTRuleSet.ASTAtom atom() {
        // NEXT, parse the atom.
        scanner.consume(IDENTIFIER, "expected relation.");
        var relation = scanner.previous();
        scanner.consume(LEFT_PAREN, "expected '(' after relation.");

        if (scanner.checkTwo(IDENTIFIER, COLON)) {
            return namedAtom(relation);
        } else {
            return orderedAtom(relation);
        }
    }

    private ASTRuleSet.ASTOrderedAtom orderedAtom(Token relation) {
        var terms = new ArrayList<ASTRuleSet.ASTTerm>();

        do {
            terms.add(term());
        } while (scanner.match(COMMA));

        scanner.consume(RIGHT_PAREN, "expected ')' after terms.");

        return new ASTRuleSet.ASTOrderedAtom(relation, terms);
    }

    private ASTRuleSet.ASTNamedAtom namedAtom(Token relation) {
        var terms = new LinkedHashMap<Token,ASTRuleSet.ASTTerm>();

        do {
            scanner.consume(IDENTIFIER, "expected field name.");
            var name = scanner.previous();
            scanner.consume(COLON, "expected ':' after field name.");
            terms.put(name, term());
        } while (scanner.match(COMMA));

        scanner.consume(RIGHT_PAREN, "expected ')' after terms.");

        return new ASTRuleSet.ASTNamedAtom(relation, terms);
    }

    private ASTRuleSet.ASTTerm term() {
        if (scanner.match(IDENTIFIER)) {
            var name = scanner.previous();
            if (name.lexeme().startsWith("_")) {
                return new ASTRuleSet.ASTWildcard(name);
            } else {
                return new ASTRuleSet.ASTVariable(name);
            }
        } else if (scanner.match(MINUS)) {
            scanner.consume(NUMBER, "expected number after '-'.");
            var number = (Double)scanner.previous().literal();
            return new ASTRuleSet.ASTConstant(scanner.previous(), -number);
        } else if (scanner.match(TRUE)) {
            return new ASTRuleSet.ASTConstant(scanner.previous(), true);
        } else if (scanner.match(FALSE)) {
            return new ASTRuleSet.ASTConstant(scanner.previous(), false);
        } else if (scanner.match(NULL)) {
            return new ASTRuleSet.ASTConstant(scanner.previous(), null);
        } else if (scanner.match(KEYWORD, NUMBER, STRING)) {
            return new ASTRuleSet.ASTConstant(
                scanner.previous(), scanner.previous().literal());
        } else {
            scanner.advance();
            throw errorSync(scanner.previous(), "expected term.");
        }
    }

    // Discard tokens until we are at the beginning of the next statement.
    // For Nero code, that just means whatever follows the next semicolon.
    private void synchronize() {
        try {
            parent.setSynchronizing(true);

            // Discard this token
            scanner.advance();

            while (!scanner.isAtEnd()) {
                // If we see we just completed a statement, return.
                if (scanner.previous().type() == SEMICOLON) return;

                // Discard this token.
                scanner.advance();
            }
        } finally {
            parent.setSynchronizing(false);
        }
    }

}
