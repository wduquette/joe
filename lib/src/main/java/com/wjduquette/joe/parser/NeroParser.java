package com.wjduquette.joe.parser;

import com.wjduquette.joe.nero.*;
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
        var headToken = head.relation();
        for (var term : head.getTerms()) {
            if (!(term instanceof Constant)) {
                error(headToken,
                    "axiom contains a non-constant term: '" +
                    term + "'.");
            }
        }
        return head;
    }

    private ASTRuleSet.ASTRule rule(ASTRuleSet.ASTAtom head) {
        var headToken = head.relation();
        var body = new ArrayList<ASTRuleSet.ASTAtom>();
        var negations = new ArrayList<ASTRuleSet.ASTAtom>();
        var constraints = new ArrayList<Constraint>();
        var bodyVar = new HashSet<String>();

        do {
            var negated = scanner.match(NOT);

            var token = scanner.peek();
            var atom = atom();
            if (negated) {
                for (var name : atom.getVariableNames()) {
                    if (!bodyVar.contains(name)) {
                        error(token,
                            "negated body atom contains unbound variable: '" +
                            name + "'.");
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
                case Constant ignored -> {}
                case Variable v -> {
                    if (!bodyVar.contains(v.name())) {
                        error(headToken,
                            "head atom contains unbound variable: '" +
                            v.name() + "'.");
                    }
                }
                case Wildcard w -> error(headToken,
                    "head atom contains wildcard: '" + w.name() + "'.");
            }
        }

        return new ASTRuleSet.ASTRule(head, body, negations, constraints);
    }

    private Constraint constraint(Set<String> bodyVar) {
        var term = term();
        Constraint.Op op;
        Variable a = null;

        switch (term) {
            case Constant ignored ->
                error(scanner.previous(), "expected bound variable.");
            case Variable v -> {
                a = v;
                if (!bodyVar.contains(v.name())) {
                    error(scanner.previous(), "expected bound variable.");
                }
            }
            case Wildcard ignored ->
                error(scanner.previous(), "expected bound variable.");
        }

        if (scanner.match(BANG_EQUAL)) {
            op = Constraint.Op.NE;
        } else if (scanner.match(EQUAL_EQUAL)) {
            op = Constraint.Op.EQ;
        } else if (scanner.match(GREATER)) {
            op = Constraint.Op.GT;
        } else if (scanner.match(GREATER_EQUAL)) {
            op = Constraint.Op.GE;
        } else if (scanner.match(LESS)) {
            op = Constraint.Op.LT;
        } else if (scanner.match(LESS_EQUAL)) {
            op = Constraint.Op.LE;
        } else {
            scanner.advance();
            throw errorSync(scanner.previous(), "expected comparison operator.");
        }

        var b = term();

        if (b instanceof Variable v) {
            if (!bodyVar.contains(v.name())) {
                error(scanner.previous(),
                    "expected bound variable or constant.");
            }
        } else if (b instanceof Wildcard) {
            error(scanner.previous(), "expected bound variable or constant.");
        }

        return new Constraint(a, op, b);
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
        var terms = new ArrayList<Term>();

        do {
            terms.add(term());
        } while (scanner.match(COMMA));

        scanner.consume(RIGHT_PAREN, "expected ')' after terms.");

        return new ASTRuleSet.ASTOrderedAtom(relation, terms);
    }

    private ASTRuleSet.ASTNamedAtom namedAtom(Token relation) {
        var terms = new LinkedHashMap<Token,Term>();

        do {
            scanner.consume(IDENTIFIER, "expected field name.");
            var name = scanner.previous();
            scanner.consume(COLON, "expected ':' after field name.");
            terms.put(name, term());
        } while (scanner.match(COMMA));

        scanner.consume(RIGHT_PAREN, "expected ')' after terms.");

        return new ASTRuleSet.ASTNamedAtom(relation, terms);
    }

    private Term term() {
        if (scanner.match(IDENTIFIER)) {
            var name = scanner.previous();
            if (name.lexeme().startsWith("_")) {
                return new Wildcard(name.lexeme());
            } else {
                return new Variable(name.lexeme());
            }
        } else if (scanner.match(MINUS)) {
            scanner.consume(NUMBER, "expected number after '-'.");
            var number = (Double)scanner.previous().literal();
            return new Constant(-number);
        } else if (scanner.match(TRUE)) {
            return new Constant(true);
        } else if (scanner.match(FALSE)) {
            return new Constant(false);
        } else if (scanner.match(NULL)) {
            return new Constant(null);
        } else if (scanner.match(KEYWORD, NUMBER, STRING)) {
            return new Constant(scanner.previous().literal());
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
