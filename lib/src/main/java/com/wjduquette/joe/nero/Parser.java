package com.wjduquette.joe.nero;
import com.wjduquette.joe.Trace;

import java.util.*;

import static com.wjduquette.joe.nero.TokenType.*;
import static com.wjduquette.joe.nero.NeroAST.*;

class Parser {
    //-------------------------------------------------------------------------
    // Instance Variables

    private final List<Token> tokens;
    private final ErrorReporter reporter;
    private int current = 0;

    //-------------------------------------------------------------------------
    // Constructor

    Parser(
        List<Token> tokens,
        ErrorReporter reporter)
    {
        this.reporter = reporter;
        this.tokens = tokens;
    }

    //-------------------------------------------------------------------------
    // Public API

    public List<HornClause> parse() {
        List<HornClause> clauses = new ArrayList<>();

        while (!isAtEnd()) {
            clauses.add(clause());
        }

        return clauses;
    }

    //-------------------------------------------------------------------------
    // Productions

    private HornClause clause() {
        try {
            var head = atom(false);

            if (match(DOT)) {
                return fact(head);
            } else if (match(COLON_MINUS)) {
                return rule(head);
            } else {
                advance();
                throw errorSync(previous(), "expected rule or fact.");
            }
        } catch (ErrorSync error) {
            synchronize();
            return null;
        }
    }

    private HornClause fact(AtomItem head) {
        // Verify that there are no variable terms.
        for (var term : head.terms()) {
            if (term instanceof VariableToken v) {
                error(v.name(), "fact contains a variable term.");
            }
        }
        return new FactClause(head);
    }

    private HornClause rule(AtomItem head) {
        var body = new ArrayList<AtomItem>();
        var constraints = new ArrayList<ConstraintItem>();

        var bodyVar = new HashSet<String>();
        do {
            var atom = atom(true);
            body.add(atom);
            if (!atom.negated()) {
                bodyVar.addAll(atom.getVariableNames());
            }
        } while (match(COMMA));

        if (match(WHERE)) {
            constraints.add(constraint(bodyVar));
        } while (match(COMMA));

        consume(DOT, "expected '.' after rule body.");

        // Verify that the head contains only body variables
        // from positive body items.
        head.terms().stream()
            .filter(t -> t instanceof VariableToken)
            .map(t -> (VariableToken)t)
            .filter(t -> !bodyVar.contains(t.toString()))
            .forEach(v -> error(v.name(),
                "head variable not found in positive body atom."));

        return new RuleClause(head, body, constraints);
    }

    private ConstraintItem constraint(Set<String> bodyVar) {
        var term = term();
        Token op;
        VariableToken a = null;

        switch (term) {
            case ConstantToken c ->
                error(c.value(), "expected bound variable.");
            case VariableToken v -> {
                a = v;
                if (!bodyVar.contains(v.name().lexeme())) {
                    error(v.name(), "expected bound variable.");
                }
            }
        }

        if (match(
            BANG_EQUAL, EQUAL_EQUAL,
            GREATER, GREATER_EQUAL,
            LESS, LESS_EQUAL)
        ) {
            op = previous();
        } else {
            throw errorSync(previous(), "expected comparison operator.");
        }

        var b = term();

        if (b instanceof VariableToken v) {
            if (!bodyVar.contains(v.name().lexeme())) {
                error(v.name(), "expected bound variable.");
            }
        }

        return new ConstraintItem(a, op, b);
    }

    private AtomItem atom(boolean inBody) {
        // FIRST, if this is a body atom, check for "not"
        var negated = inBody && match(NOT);

        // NEXT, parse the literal proper
        var predicate = consume(IDENTIFIER, "expected predicate.");
        consume(LEFT_PAREN, "expected '(' after predicate.");

        var terms = new ArrayList<TermToken>();

        do {
            terms.add(term());
        } while (match(COMMA));

        consume(RIGHT_PAREN, "expected ')' after terms.");

        return new AtomItem(predicate, terms, negated);
    }

    private TermToken term() {
        if (match(IDENTIFIER)) {
            return new VariableToken(previous());
        } else if (match(KEYWORD, NUMBER, STRING)) {
            return new ConstantToken(previous());
        } else {
            advance();
            throw errorSync(previous(), "expected term.");
        }
    }


    //-------------------------------------------------------------------------
    // Primitives

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }

        return false;
    }

    private Token consume(TokenType type, String message) {
        if (check(type)) return advance();

        throw errorSync(peek(), message);
    }

    private boolean check(TokenType type) {
        if (isAtEnd()) return false;
        return peek().type() == type;
    }

    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    private boolean isAtEnd() {
        return peek().type() == EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    // Saves the error detail, with no synchronization.
    void error(Token token, String message) {
        var msg = token.type() == TokenType.EOF
            ? "Error at end: " + message
            : "Error at '" + token.lexeme() + "': " + message;
        reporter.reportError(new Trace(token.span(), msg));
    }

    // Saves the error detail, with synchronization.
    private ErrorSync errorSync(Token token, String message) {
        error(token, message);
        return new ErrorSync(message);
    }

    // Discard tokens until we are at the beginning of the next statement.
    private void synchronize() {
        // Discard this token
        advance();

        // Complete the clause.
        while (!isAtEnd()) {
            // If we see we just completed a clause, return.
            if (previous().type() == DOT) return;

            // Discard this token.
            advance();
        }
    }

    /**
     * An exception used to synchronize errors.
     */
    private static class ErrorSync extends RuntimeException {
        ErrorSync(String message) {
            super(message);
        }
    }
}
