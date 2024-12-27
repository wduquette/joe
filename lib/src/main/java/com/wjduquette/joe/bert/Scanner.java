package com.wjduquette.joe.bert;

import com.wjduquette.joe.Joe;
import com.wjduquette.joe.Keyword;
import com.wjduquette.joe.SourceBuffer;
import com.wjduquette.joe.Trace;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static com.wjduquette.joe.bert.TokenType.*;

public class Scanner {
    private static final Map<String, TokenType> reserved;

    static {
        reserved = new HashMap<>();
        reserved("assert",   ASSERT);
        reserved("break",    BREAK);
        reserved("case",     CASE);
        reserved("class",    CLASS);
        reserved("continue", CONTINUE);
        reserved("default",  DEFAULT);
        reserved("else",     ELSE);
        reserved("extends",  EXTENDS);
        reserved("false",    FALSE);
        reserved("for",      FOR);
        reserved("foreach",  FOREACH);
        reserved("function", FUNCTION);
        reserved("if",       IF);
        reserved("in",       IN);
        reserved("method",   METHOD);
        reserved("ni",       NI);
        reserved("null",     NULL);
        reserved("return",   RETURN);
        reserved("static",   STATIC);
        reserved("super",    SUPER);
        reserved("switch",   SWITCH);
        reserved("this",     THIS);
        reserved("throw",    THROW);
        reserved("true",     TRUE);
        reserved("var",      VAR);
        reserved("while",    WHILE);
    }

    private static void reserved(String word, TokenType token) {
        if (!Joe.RESERVED_WORDS.contains(word)) {
            throw new IllegalStateException("Not reserved: '" + word + "'.");
        }
        reserved.put(word, token);
    }

    //-------------------------------------------------------------------------
    // Instance Variables

    private final String source;
    private final SourceBuffer buffer;
    private final Consumer<Trace> reporter;
    private int start = 0;
    private int current = 0;

    //-------------------------------------------------------------------------
    // Constructor

    /**
     * Creates a new scanner for the given source buffer, using
     * the given error reporter.
     *
     * @param buffer The SourceBuffer
     * @param reporter The error handler.
     */
    Scanner(
        SourceBuffer buffer,
        Consumer<Trace> reporter
    ) {
        this.buffer = buffer;
        this.source = buffer.source();
        this.reporter = reporter;
    }

    Token scanToken() {
        skipWhitespace();
        start = current;

        if (isAtEnd()) {
            return new Token(EOF, buffer.span(start, current), null);
        }

        char c = advance();

        if (isAlpha(c)) {
            return identifier();
        }
        if (isDigit(c)) {
            return number();
        }

        return switch (c) {
            case '(' -> makeToken(LEFT_PAREN);
            case ')' -> makeToken(RIGHT_PAREN);
            case '{' -> makeToken(LEFT_BRACE);
            case '}' -> makeToken(RIGHT_BRACE);
            case ';' -> makeToken(SEMICOLON);
            case ',' -> makeToken(COMMA);
            case '.' -> makeToken(DOT);
            case '-' -> makeToken(MINUS);
            case '+' -> makeToken(PLUS);
            case '/' -> makeToken(SLASH);
            case '*' -> makeToken(STAR);
            case '!' -> makeToken(match('=') ? BANG_EQUAL : BANG);
            case '=' -> makeToken(match('=') ? EQUAL_EQUAL : EQUAL);
            case '<' -> makeToken(match('=') ? LESS_EQUAL : LESS);
            case '>' -> makeToken(match('=') ? GREATER_EQUAL : GREATER);
            case '&' -> {
                if (match('&')) {
                    yield makeToken(AND);
                } else {
                    yield errorToken("unexpected character.");
                }
            }
            case '|' -> {
                if (match('|')) {
                    yield makeToken(OR);
                } else {
                    yield errorToken("unexpected character.");
                }
            }
            case '"' -> string();
            case '#' -> keyword();
            default -> errorToken("unexpected character.");
        };
    }

    private void skipWhitespace() {
        for (;;) {
            var c = peek();
            switch (c) {
                case ' ', '\n', '\r', '\t' -> advance();
                case '/' -> {
                    if (peekNext() == '/') {
                        while (peek() != '\n' && !isAtEnd()) advance();
                    } else {
                        return;
                    }
                }
                default -> { return; }
            }
        }
    }

    private Token string() {
        while (peek() != '"' && !isAtEnd()) advance();

        if (isAtEnd()) {
            return errorToken("unterminated string.");
        }

        // Closing quote
        advance();
        return makeToken(STRING, source.substring(start + 1, current - 1));
    }

    private Token number() {
        while (isDigit(peek())) advance();

        if (peek() == '.' && isDigit(peekNext())) {
            // Consume the '.'
            advance();
            while (isDigit(peek())) advance();
        }

        return makeToken(NUMBER,
            Double.valueOf(source.substring(start, current)));
    }

    private Token identifier() {
        while (isAlpha(peek()) || isDigit(peek())) advance();

        var lexeme = source.substring(start, current);
        var type = reserved.get(lexeme);

        return makeToken(type != null ? type : IDENTIFIER);
    }

    private Token keyword() {
        if (!isAlpha(peek())) {
            return errorToken("Expected keyword name.");
        }
        while (isAlpha(peek()) || isDigit(peek())) advance();

        var keyword = new Keyword(
            source.substring(start + 1, current));

        return makeToken(KEYWORD, keyword);
    }

    private boolean isAtEnd() {
        return current == source.length();
    }

    private char peek() {
        return isAtEnd() ? '\0' : source.charAt(current);
    }

    private char peekNext() {
        return isAtEnd() ? '\0' : source.charAt(current + 1);
    }

    private char advance() {
        current++;
        return source.charAt(current - 1);
    }

    private boolean match(char expected) {
        if (isAtEnd()) return false;
        if (source.charAt(current) != expected) return false;
        current ++;
        return true;
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z')
            || (c >= 'A' && c <= 'Z')
            || c == '_';
    }

    private Token makeToken(TokenType type) {
        return makeToken(type, null);
    }

    private Token makeToken(TokenType type, Object literal) {
        return new Token(type, buffer.span(start, current), literal);
    }

    private Token errorToken(String message) {
        var where = isAtEnd() ? "end"
            : "'" + source.substring(start, current) + "'";

        reporter.accept(new Trace(buffer.span(start, current),
            "At " + where + ", " + message));
        return new Token(ERROR, null, null);
    }
}
