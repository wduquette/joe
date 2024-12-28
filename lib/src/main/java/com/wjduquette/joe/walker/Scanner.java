package com.wjduquette.joe.walker;

import com.wjduquette.joe.*;

import java.util.*;
import java.util.function.Consumer;

import static com.wjduquette.joe.walker.TokenType.*;

class Scanner {
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
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0;
    private int current = 0;

    //-------------------------------------------------------------------------
    // Constructor

    /**
     * Creates a new scanner for the given filename and source text, using
     * the given error reporter.  The *filename* is usually the bare filename
     * of the source script, but can be any string meaningful to the
     * application.
     *
     * @param filename The filename
     * @param source The source text
     * @param reporter The error reporter
     */
    Scanner(
        String filename,
        String source,
        Consumer<Trace> reporter
    ) {
        this.source = source;
        this.buffer = new SourceBuffer(filename, source);
        this.reporter = reporter;
    }

    //-------------------------------------------------------------------------
    // Public API

    /**
     * Scans the source text and returns a list of tokens.
     * @return The list
     */
    List<Token> scanTokens() {
        while (!isAtEnd()) {
            // We are at the beginning of the next lexeme.
            start = current;
            scanToken();
        }

        var endSpan = buffer.span(source.length(), source.length());
        tokens.add(new Token(EOF, endSpan, null));
        return tokens;
    }

    /**
     * Returns the source buffer.
     * @return the buffer.
     */
    SourceBuffer buffer() {
        return buffer;
    }


    //-------------------------------------------------------------------------
    // The Scanner

    private void scanToken() {
        char c = advance();
        switch (c) {
            case '('  -> addToken(LEFT_PAREN);
            case ')'  -> addToken(RIGHT_PAREN);
            case '{'  -> addToken(LEFT_BRACE);
            case '}'  -> addToken(RIGHT_BRACE);
            case '@'  -> addToken(AT);
            case '\\' -> addToken(BACK_SLASH);
            case ':'  -> addToken(COLON);
            case ','  -> addToken(COMMA);
            case '.'  -> addToken(DOT);
            case '-'  -> {
                if (match('=')) {
                    addToken(MINUS_EQUAL);
                } else if (match('>')) {
                    addToken(MINUS_GREATER);
                } else if (match('-')) {
                    addToken(MINUS_MINUS);
                } else {
                    addToken(MINUS);
                }
            }
            case '+'  -> {
                if (match('=')) {
                    addToken(PLUS_EQUAL);
                } else if (match('+')) {
                    addToken(PLUS_PLUS);
                } else {
                    addToken(PLUS);
                }
            }
            case '?'  -> addToken(QUESTION);
            case ';'  -> addToken(SEMICOLON);
            case '*'  -> addToken(match('=') ? STAR_EQUAL : STAR);
            case '!'  -> addToken(match('=') ? BANG_EQUAL : BANG);
            case '='  -> addToken(match('=') ? EQUAL_EQUAL : EQUAL);
            case '<'  -> addToken(match('=') ? LESS_EQUAL : LESS);
            case '>'  -> addToken(match('=') ? GREATER_EQUAL : GREATER);
            case '/'  -> {
                if (match('/')) {
                    // A comment goes until the end of the line.
                    while (peek() != '\n' && !isAtEnd()) advance();
                } else if (match('=')) {
                    addToken(SLASH_EQUAL);
                } else {
                    addToken(SLASH);
                }
            }
            case '&' -> {
                if (match('&')) {
                    addToken(AND);
                } else {
                    error("Expected '&&', got: '&'.");
                }
            }
            case '|' -> {
                if (match('|')) {
                    addToken(OR);
                } else {
                    error("Expected '||', got: '|'.");
                }
            }
            case ' ', '\r', '\t' -> {
                // Ignore whitespace.
            }
            case '\n' -> {}
            case '"' -> string();
            case '\'' -> rawString();
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
        if (matchNext("\"\"")) {
            textBlock();
            return;
        }

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

    private void textBlock() {
        var buff = new StringBuilder();

        while (!isAtEnd()) {
            var c = peek();

            switch (c) {
                case '"' -> {
                    if (matchNext("\"\"\"")) {
                        // Add the unescaped string.
                        addToken(STRING, outdent(buff.toString()));
                        return;
                    } else {
                        buff.append(advance());
                    }
                }
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
                default -> buff.append(advance());
            }
        }

        error("Unterminated text block.");
    }

    private void rawString() {
        if (matchNext("''")) {
            rawTextBlock();
            return;
        }

        while (peek() != '\'' && !isAtEnd()) {
            var c = advance();

            if (c == '\n') {
                error("Newline in raw string.");
                return;
            }
        }

        if (isAtEnd()) {
            error("Unterminated raw string.");
            return;
        }

        // The closing quote
        advance();

        // Add the raw string.
        addToken(STRING, source.substring(start+1,current-1));
    }

    private void rawTextBlock() {
        while (!isAtEnd()) {
            var c = peek();

            switch (c) {
                case '\'' -> {
                    if (matchNext("'''")) {
                        // Add the string.
                        var string = source.substring(start+3,current-3);
                        addToken(STRING, outdent(string));
                        return;
                    } else {
                        advance();
                    }
                }
                default -> advance();
            }
        }

        error("Unterminated raw text block.");
    }

    private String outdent(String text) {
        // FIRST, remove leading blank lines.
        while (true) {
            var ndx = text.indexOf('\n');

            if (ndx >= 0 && text.substring(0, ndx).isBlank()) {
                text = text.substring(ndx + 1);
            } else {
                break;
            }
        }

        // NEXT, strip the indent and return.
        return text.stripIndent().stripTrailing();
    }

    private boolean matchNext(String chars) {
        for (int i = 0; i < chars.length(); i++) {
            if (peekNext(i) != chars.charAt(i)) {
                return false;
            }
        }
        current += chars.length();
        return true;
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

    private char peekNext(int delta) {
        if (current + delta >= source.length()) return '\0';
        return source.charAt(current + delta);
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

    private void error(String message) {
        reporter.accept(
            new Trace(buffer.span(start, current), message));
    }
}
