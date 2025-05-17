package com.wjduquette.joe.nero.parser;

import com.wjduquette.joe.Keyword;
import com.wjduquette.joe.SourceBuffer;
import com.wjduquette.joe.Trace;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.wjduquette.joe.nero.parser.TokenType.*;

public class Scanner {
    private static final Map<String, TokenType> reserved;

    static {
        reserved = new HashMap<>();
        reserved("not",   NOT);
        reserved("where", WHERE);
    }

    private static void reserved(String word, TokenType token) {
        reserved.put(word, token);
    }

    //-------------------------------------------------------------------------
    // Instance Variables

    private final String source;
    private final SourceBuffer buffer;
    private final ErrorReporter reporter;
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0;
    private int current = 0;

    //-------------------------------------------------------------------------
    // Constructor

    /**
     * Creates a new scanner for the given source buffer, using
     * the given error reporter.
     *
     * @param buffer The source buffer
     * @param reporter The error reporter
     */
    public Scanner(
        SourceBuffer buffer,
        ErrorReporter reporter
    ) {
        this.buffer = buffer;
        this.source = buffer.source();
        this.reporter = reporter;
    }

    //-------------------------------------------------------------------------
    // Public API

    /**
     * Scans the source text and returns a list of tokens.
     * @return The list
     */
    public List<Token> scanTokens() {
        while (!isAtEnd()) {
            // We are at the beginning of the next lexeme.
            start = current;
            scanToken();
        }

        var endSpan = buffer.span(source.length(), source.length());
        tokens.add(new Token(EOF, endSpan, null));
        return tokens;
    }

    //-------------------------------------------------------------------------
    // The Scanner

    private void scanToken() {
        char c = advance();
        switch (c) {
            // One-character tokens
            case '('  -> addToken(LEFT_PAREN);
            case ')'  -> addToken(RIGHT_PAREN);
            case ','  -> addToken(COMMA);
            case ';'  -> addToken(SEMICOLON);

            // One-or-two-character tokens
            case '!' -> {
                if (match('=')) addToken(BANG_EQUAL);
            }
            case ':'  -> {
                if (match('-')) addToken(COLON_MINUS);
            }
            case '=' -> {
                if (match('=')) addToken(EQUAL_EQUAL);
            }
            case '>' -> addToken(match('=') ? GREATER_EQUAL : GREATER);
            case '<' -> addToken(match('=') ? LESS_EQUAL : LESS);

            // Comments and whitespace
            case '/'  -> {
                if (match('/')) {
                    // A comment goes until the end of the line.
                    while (peek() != '\n' && !isAtEnd()) advance();
                }
            }
            case ' ', '\r', '\t', '\n' -> {
                // Ignore whitespace.
            }

            // Literals, identifiers, and reserved words
            case '"' -> string();
            case '#' -> keyword();
            default -> {
                if (c == '0' && peek() == 'x') {
                    hexNumber();
                } else if (isDigit(c)) {
                    number();
                } else if (isAlpha(c)) {
                    identifier();
                } else {
                    error("Unexpected character: '" + c + "'.");
                }
            }
        }
    }

    private void identifier() {
        while (isAlphaNumeric(peek())) advance();

        String text = source.substring(start, current);
        TokenType type = reserved.get(text);
        if (type == null) type = IDENTIFIER;

        addToken(type);
    }

    private void keyword() {
        if (!isAlpha(peek())) {
            error("Expected keyword name.");
            return;
        }
        while (isAlphaNumeric(peek())) advance();

        var keyword = new Keyword(
            source.substring(start + 1, current));

        addToken(KEYWORD, keyword);
    }

    private void hexNumber() {
        advance();  // Consume the x
        while (isHexDigit(peek())) advance();

        try {
            var num = Integer.parseInt(source.substring(start + 2, current), 16);
            addToken(NUMBER, (double)num);
        } catch (Exception ex) {
            error("Invalid hex literal.");
        }
    }

    private void number() {
        while (isDigit(peek())) advance();

        // Look for a fractional part.
        if (peek() == '.' && isDigit(peekNext())) {
            // Consume the "."
            advance();

            while (isDigit(peek())) advance();
        }

        // Look for an exponent.
        if (peek() == 'e' || peek() == 'E') {
            advance(); // Consume the "e"

            if (peek() == '+' || peek() == '-') {
                advance(); // Consume the sign
            }

            if (!isDigit(peek())) {
                error("Expected exponent.");
                return;
            }

            while (isDigit(peek())) advance();
        }

        addToken(NUMBER,
                Double.parseDouble(source.substring(start, current)));
    }

    private void string() {
        var buff = new StringBuilder();

        while (peek() != '"' && !isAtEnd()) {
            var c = peek();

            switch (c) {
                case '\\' -> {
                    if (!isAtEnd()) {
                        advance(); // Skip past the backslash
                        var escape = advance();
                        switch (escape) {
                            case '\\' -> buff.append('\\');
                            case 't' -> buff.append('\t');
                            case 'b' -> buff.append('\b');
                            case 'n' -> buff.append('\n');
                            case 'r' -> buff.append('\r');
                            case 'f' -> buff.append('\f');
                            case '"' -> buff.append('"');
                            case 'u' -> unicode(buff);
                            default -> error(
                                "Invalid escape: '\\" + escape + "'.");
                        }
                    }
                }
                case '\n' -> {
                    error("Newline in single-line string.");
                    return;
                }
                default -> buff.append(advance());
            }
        }

        if (isAtEnd()) {
            error("Unterminated string.");
            return;
        }

        // The closing quote
        advance();

        // Add the unescaped string.
        addToken(STRING, buff.toString());
    }

    private void unicode(StringBuilder buff) {
        var mark = current;

        while (current - mark < 4 && isHexDigit(peek())) {
            advance();
        }

        var hexCode = source.substring(mark, current);

        if (current - mark == 4) {
            var hex = Integer.parseInt(hexCode, 16);
            buff.append(unicodeToString(hex));
        } else {
            error("Incomplete Unicode escape: '\\u" + hexCode + "'.");
        }
    }

    private String unicodeToString(int hex) {
        try {
            return Character.toString(hex);
        } catch (Exception ex) {
            return "\uFFFD";
        }
    }

    private boolean match(char expected) {
        if (isAtEnd()) return false;
        if (source.charAt(current) != expected) return false;

        current++;
        return true;
    }

    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }

    private char peekNext() {
        if (current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') ||
                (c >= 'A' && c <= 'Z') ||
                c == '_';
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean isHexDigit(char c) {
        return (c >= '0' && c <= '9')
            || (c >= 'A' && c <= 'F')
            || (c >= 'a' && c <= 'f');
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

    private char advance() {
        return source.charAt(current++);
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        var span = buffer.span(start, current);
        tokens.add(new Token(type, span, literal));
    }

    // A normal syntax error
    private void error(String message) {
        reporter.reportError(
            new Trace(buffer.span(start, current), message));
    }
}
