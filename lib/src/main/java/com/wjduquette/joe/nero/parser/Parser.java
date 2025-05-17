package com.wjduquette.joe.nero.parser;
import com.wjduquette.joe.Trace;

import java.util.*;

import static com.wjduquette.joe.nero.parser.TokenType.*;
import static com.wjduquette.joe.nero.parser.NeroAST.*;

public class Parser {
    //-------------------------------------------------------------------------
    // Instance Variables

    private final List<Token> tokens;
    private final ErrorReporter reporter;
    private int current = 0;

    //-------------------------------------------------------------------------
    // Constructor

    public Parser(List<Token> tokens, ErrorReporter reporter) {
        this.reporter = reporter;
        this.tokens = tokens;
    }

    //-------------------------------------------------------------------------
    // Public API

    public NeroAST parse() {
        List<ASTFact> facts = new ArrayList<>();
        List<ASTRule> rules = new ArrayList<>();

        while (!isAtEnd()) {
            try {
                var head = atom();

                if (match(SEMICOLON)) {
                    facts.add(fact(head));
                } else if (match(COLON_MINUS)) {
                    rules.add(rule(head));
                } else {
                    advance();
                    throw errorSync(previous(), "expected fact or rule.");
                }
            } catch (ErrorSync error) {
                synchronize();
            }
        }

        return new NeroAST(facts, rules);
    }

    //-------------------------------------------------------------------------
    // Productions

    private ASTFact fact(ASTAtom head) {
        // Verify that there are no variable terms.
        for (var term : head.terms()) {
            if (!(term instanceof ASTConstant)) {
                error(term.token(), "axiom contains a non-constant term.");
            }
        }
        return new ASTFact(head);
    }

    private ASTRule rule(ASTAtom head) {
        var body = new ArrayList<ASTAtom>();
        var negations = new ArrayList<ASTAtom>();
        var constraints = new ArrayList<ConstraintItem>();

        var bodyVar = new HashSet<String>();
        do {
            var negated = match(NOT);

            var atom = atom();
            if (negated) {
                for (var term : atom.terms()) {
                    if (term instanceof ASTVariable) {
                        if (!bodyVar.contains(term.token().lexeme())) {
                            error(term.token(),
                                "negated atom contains an unbound variable.");
                        }
                    }
                }
                negations.add(atom);
            } else {
                body.add(atom);
                bodyVar.addAll(atom.getVariableNames());
            }
        } while (match(COMMA));

        if (match(WHERE)) {
            constraints.add(constraint(bodyVar));
        } while (match(COMMA));

        consume(SEMICOLON, "expected ';' after rule body.");

        // Verify that the head contains only valid terms.
        for (var term : head.terms()) {
            switch (term) {
                case ASTConstant ignored -> {}
                case ASTVariable v -> {
                    if (!bodyVar.contains(v.token().lexeme())) {
                        error(v.token(),
                            "head variable not found in positive body atom.");
                    }
                }
                case ASTWildcard w -> error(w.token(),
                    "wildcard found in rule head.");
            }
        }

        return new ASTRule(head, body, negations, constraints);
    }

    private ConstraintItem constraint(Set<String> bodyVar) {
        var term = term();
        Token op;
        ASTVariable a = null;

        switch (term) {
            case ASTConstant c ->
                error(c.token(), "expected bound variable.");
            case ASTVariable v -> {
                a = v;
                if (!bodyVar.contains(v.token().lexeme())) {
                    error(v.token(), "expected bound variable.");
                }
            }
            case ASTWildcard c ->
                error(c.token(), "expected bound variable.");
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

        if (b instanceof ASTVariable v) {
            if (!bodyVar.contains(v.token().lexeme())) {
                error(v.token(), "expected bound variable.");
            }
        } else if (b instanceof ASTWildcard w) {
            error(w.token(), "expected bound variable or constant.");
        }

        return new ConstraintItem(a, op, b);
    }

    private ASTAtom atom() {
        // NEXT, parse the literal proper
        var predicate = consume(IDENTIFIER, "expected predicate.");
        consume(LEFT_PAREN, "expected '(' after predicate.");

        var terms = new ArrayList<ASTTerm>();

        do {
            terms.add(term());
        } while (match(COMMA));

        consume(RIGHT_PAREN, "expected ')' after terms.");

        return new ASTAtom(predicate, terms);
    }

    private ASTTerm term() {
        if (match(IDENTIFIER)) {
            var name = previous();
            if (name.lexeme().startsWith("_")) {
                return new ASTWildcard(name);
            } else {
                return new ASTVariable(name);
            }
        } else if (match(KEYWORD, NUMBER, STRING)) {
            return new ASTConstant(previous());
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
            if (previous().type() == SEMICOLON) return;

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
