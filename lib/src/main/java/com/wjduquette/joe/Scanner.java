package com.wjduquette.joe;

import java.util.*;
import java.util.function.Consumer;

import static com.wjduquette.joe.TokenType.*;

class Scanner {
    public static final Set<String> RESERVED_WORDS;
    private static final Map<String, TokenType> reserved;

    static {
        reserved = new HashMap<>();
        reserved.put("assert",   ASSERT);
        reserved.put("break",    BREAK);
        reserved.put("case",     CASE);
        reserved.put("class",    CLASS);
        reserved.put("continue", CONTINUE);
        reserved.put("default",  DEFAULT);
        reserved.put("else",     ELSE);
        reserved.put("extends",  EXTENDS);
        reserved.put("false",    FALSE);
        reserved.put("for",      FOR);
        reserved.put("foreach",  FOREACH);
        reserved.put("function", FUNCTION);
        reserved.put("if",       IF);
        reserved.put("in",       IN);
        reserved.put("method",   METHOD);
        reserved.put("ni",       NI);
        reserved.put("null",     NULL);
        reserved.put("return",   RETURN);
        reserved.put("static",   STATIC);
        reserved.put("super",    SUPER);
        reserved.put("switch",   SWITCH);
        reserved.put("this",     THIS);
        reserved.put("throw",    THROW);
        reserved.put("true",     TRUE);
        reserved.put("var",      VAR);
        reserved.put("while",    WHILE);
    }

    static {
        var words = new TreeSet<>(reserved.keySet());
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
            case '('  -> addToken(LEFT_PAREN);
            case ')'  -> addToken(RIGHT_PAREN);
            case '{'  -> addToken(LEFT_BRACE);
            case '}'  -> addToken(RIGHT_BRACE);
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

    private void hexNumber() {
        advance();  // Consume the x
        while (isHexDigit(peek())) advance();

        try {
            var num = Integer.parseInt(source.substring(start + 2, current), 16);
            addToken(NUMBER, (double)num);
        } catch (Exception ex) {
            error(line, "Invalid hex literal.");
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
                error(line, "Expected exponent.");
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
                            default -> error(line,
                                "Unexpected escape: '\\" + escape + "'.");
                        }
                    }
                }
                case '\n' -> {
                    error(line, "Newline in single-line string.");
                    return;
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

        error(line, "Unterminated text block.");
    }

    private void rawString() {
        if (matchNext("''")) {
            rawTextBlock();
            return;
        }

        while (peek() != '\'' && !isAtEnd()) {
            var c = advance();

            if (c == '\n') {
                error(line, "Newline in raw string.");
                return;
            }
        }

        if (isAtEnd()) {
            error(line, "Unterminated raw string.");
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
                case '\n' -> {
                    line++;
                    advance();
                }
                default -> advance();
            }
        }

        error(line, "Unterminated raw text block.");
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
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }

    private void error(int line, String message) {
        reporter.accept(new SyntaxError.Detail(line, message));
    }
}
