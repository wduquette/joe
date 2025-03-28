package com.wjduquette.joe.nero;
import com.wjduquette.joe.Trace;

import java.util.*;

import static com.wjduquette.joe.nero.TokenType.*;

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

    public List<Clause> parse() {
        List<Clause> clauses = new ArrayList<>();

        while (!isAtEnd()) {
            clauses.add(clause());
        }

        return clauses;
    }

    //-------------------------------------------------------------------------
    // Productions

    private Clause clause() {
        try {
            var head = literal();

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

    private Clause fact(Literal head) {
        // Verify that there are no variable terms.
        for (var term : head.terms()) {
            if (term instanceof Literal.Term.Variable v) {
                error(v.name(), "fact contains a variable term.");
            }
        }
        return new Clause.Fact(head);
    }

    private Clause rule(Literal head) {
        var body = new ArrayList<Literal>();

        var bodyVar = new HashSet<String>();
        do {
            var literal = literal();
            body.add(literal);
            bodyVar.addAll(literal.getVariableNames());
        } while (match(COMMA));

        consume(DOT, "expected '.' after rule body.");

        // Verify that the head contains only body variables.
        head.terms().stream()
            .filter(t -> t instanceof Literal.Term.Variable)
            .map(t -> (Literal.Term.Variable)t)
            .filter(t -> !bodyVar.contains(t.toString()))
            .forEach(v -> error(v.name(),
                "head variable not found in body."));

        return new Clause.Rule(head, body);
    }

    private Literal literal() {
        var predicate = consume(IDENTIFIER, "expected predicate.");
        consume(LEFT_PAREN, "expected '(' after predicate.");

        var terms = new ArrayList<Literal.Term>();

        do {
            terms.add(term());
        } while (match(COMMA));

        consume(RIGHT_PAREN, "expected ')' after terms.");

        return new Literal(predicate, terms);
    }

    private Literal.Term term() {
        if (match(IDENTIFIER)) {
            return new Literal.Term.Variable(previous());
        } else if (match(KEYWORD, NUMBER, STRING)) {
            return new Literal.Term.Constant(previous());
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
