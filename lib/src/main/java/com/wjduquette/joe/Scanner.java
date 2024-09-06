package com.wjduquette.joe;

import java.util.*;
import java.util.function.Consumer;

import static com.wjduquette.joe.TokenType.*;

public class Scanner {
    public static final Set<String> RESERVED_WORDS;
    private static final Map<String, TokenType> reserved;

    static {
        reserved = new HashMap<>();
        reserved.put("assert",   ASSERT);
        reserved.put("class",    CLASS);
        reserved.put("else",     ELSE);
        reserved.put("extends",  EXTENDS);
        reserved.put("false",    FALSE);
        reserved.put("for",      FOR);
        reserved.put("function", FUNCTION);
        reserved.put("if",       IF);
        reserved.put("method",   METHOD);
        reserved.put("null",     NULL);
        reserved.put("return",   RETURN);
        reserved.put("super",    SUPER);
        reserved.put("this",     THIS);
        reserved.put("true",     TRUE);
        reserved.put("var",      VAR);
        reserved.put("while",    WHILE);
    }

    static {
        var words = new TreeSet<String>(reserved.keySet());
        RESERVED_WORDS = Collections.unmodifiableSet(words);
    }

    //-------------------------------------------------------------------------
    // Instance Variables

    private final String source;
    private final Consumer<SyntaxError.Detail> reporter;
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0;
    private int current = 0;
    private int line = 1;

    //-------------------------------------------------------------------------
    // Constructor

    Scanner(String source, Consumer<SyntaxError.Detail> reporter) {
        this.source = source;
        this.reporter = reporter;
    }

    //-------------------------------------------------------------------------
    // Public API

    List<Token> scanTokens() {
        while (!isAtEnd()) {
            // We are at the beginning of the next lexeme.
            start = current;
            scanToken();
        }

        tokens.add(new Token(EOF, "", null, line));
        return tokens;
    }

    private void scanToken() {
        char c = advance();
        switch (c) {
            case '(' -> addToken(LEFT_PAREN);
            case ')' -> addToken(RIGHT_PAREN);
            case '{' -> addToken(LEFT_BRACE);
            case '}' -> addToken(RIGHT_BRACE);
            case ':' -> addToken(COLON);
            case ',' -> addToken(COMMA);
            case '.' -> addToken(DOT);
            case '-' -> addToken(MINUS);
            case '+' -> addToken(PLUS);
            case '?' -> addToken(QUESTION);
            case ';' -> addToken(SEMICOLON);
            case '*' -> addToken(STAR);
            case '!' -> addToken(match('=') ? BANG_EQUAL : BANG);
            case '=' -> addToken(match('=') ? EQUAL_EQUAL : EQUAL);
            case '<' -> addToken(match('=') ? LESS_EQUAL : LESS);
            case '>' -> addToken(match('=') ? GREATER_EQUAL : GREATER);
            case '/' -> {
                if (match('/')) {
                    // A comment goes until the end of the line.
                    while (peek() != '\n' && !isAtEnd()) advance();
                } else {
                    addToken(SLASH);
                }
            }
            case '&' -> {
                if (match('&')) {
                    addToken(AND);
                } else {
                    error(line, "Expected '&&', got: '&'.");
                }
            }
            case '|' -> {
                if (match('|')) {
                    addToken(OR);
                } else {
                    error(line, "Expected '||', got: '|'.");
                }
            }
            case ' ', '\r', '\t' -> {
                // Ignore whitespace.
            }
            case '\n' -> line++;
            case '"' -> string();
            case '#' -> keyword();
            default -> {
                if (isDigit(c)) {
                    number();
                } else if (isAlpha(c)) {
                    identifier();
                } else {
                    error(line, "Unexpected character: '" + c + "'.");
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
            error(line, "Expected keyword name.");
            return;
        }
        while (isAlphaNumeric(peek())) advance();

        var keyword = new Keyword(
            source.substring(start + 1, current));

        addToken(KEYWORD, keyword);
    }

    private void number() {
        while (isDigit(peek())) advance();

        // Look for a fractional part.
        if (peek() == '.' && isDigit(peekNext())) {
            // Consume the "."
            advance();

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
                            default -> error(line,
                                "Unexpected escape: '\\" + escape + "'.");
                        }
                    }
                }
                case '\n' -> {
                    line++;
                    buff.append(advance());
                }
                default -> buff.append(advance());
            }
        }

        if (isAtEnd()) {
            error(line, "Unterminated string.");
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

        if (current - mark == 4) {
            var hexCode = source.substring(mark, current);
            var hex = Integer.parseInt(hexCode, 16);
            buff.append(unicodeToString(hex));
        } else {
            var hexCode = source.substring(mark, current);
            error(line, "Incomplete Unicode escape: '\\u" + hexCode + "'.");
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
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }

    private void error(int line, String message) {
        reporter.accept(new SyntaxError.Detail(line, message));
    }
}
