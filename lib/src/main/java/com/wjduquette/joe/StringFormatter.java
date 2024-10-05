package com.wjduquette.joe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StringFormatter {
    private static final Map<String,List<ArgType>> formatCache = new HashMap<>();

    public static String format(
        Joe joe,
        String fmt,
        List<Object> args
    ) {
        // FIRST, parse the format for the argument types, caching the
        // result for later.
        var types = formatCache.computeIfAbsent(fmt,
            f -> new FormatParser(f).parse());

        // NEXT, do we have the right number of arguments?
        if (types.size() != args.size() - 1) {
            throw new JoeError("Expected " + types.size() +
                " values to format, got: " + (args.size() - 1));
        }

        // NEXT, build the array of values, doing any needed checks and
        // conversions.
        var values = new Object[types.size()];
        for (var i = 0; i < types.size(); i++) {
            var arg = args.get(i + 1);
            values[i] = switch (types.get(i)) {
                case ANY -> arg;
                case DOUBLE -> {
                    if (arg instanceof Double) {
                        yield arg;
                    } else if (arg instanceof Integer num) {
                        yield (double)num;
                    }
                    throw new JoeError(
                        "Conversion " + i + " expected a number, got: " +
                            joe.typeName(arg) + " '" +
                            joe.stringify(arg) + "'.",
                        "In format '" + fmt + "'.");
                }
                case INT -> {
                    if (arg instanceof Double d) {
                        yield d.intValue();
                    } else if (arg instanceof Integer) {
                        yield arg;
                    }
                    throw new JoeError(
                        "Conversion " + i + " expected a number, got: " +
                            joe.typeName(arg) + " '" +
                            joe.stringify(arg) + "'.",
                        "In format '" + fmt + "'.");
                }
            };
        }

        // NEXT, format the string.
        return String.format(fmt, values);
    }

    //-------------------------------------------------------------------------
    // Format Parser

    // The Java type expected by a conversion in the format string:
    // Any value at all, a double, or an int.
    private enum ArgType {
        ANY,
        DOUBLE,
        INT
    }

    private enum TokenType {
        // Single character tokens
        PERCENT,
        FLAG,
        DOT,
        CONVERSION,

        // Multi-character tokens
        NUMBER,  // Width or precision

        EOF
    }

    private record Token(TokenType type, String lexeme) {}

    private static class Scanner {
        private final String source;
        private final List<Token> tokens = new ArrayList<>();
        private int start = 0;
        private int current = 0;

        Scanner(String source) {
            this.source = source;
        }

        List<Token> scanTokens() {
            // FIRST, get leading text.
            text();

            // NEXT, scan for content.
            while (!isAtEnd()) {
                start = current;
                scanToken();
            }

            tokens.add(new Token(TokenType.EOF, null));
            return tokens;
        }

        private void scanToken() {
            char c = advance();
            switch (c) {
                case '-', '+', ' ', ',', '(' -> addToken(TokenType.FLAG);
                case 'b', 'B', 'd', 'e', 'E', 'f', 'g', 'G', 'h', 'H', 'n',
                     's', 'S', '%' -> addToken(TokenType.CONVERSION);
                default -> {
                    if (isDigit(c)) {
                        number();
                    } else {
                        // Anything else is invalid in a conversion; skip to
                        // the next %.
                        text();
                    }
                }
            }
        }

        private void text() {
            while (!isAtEnd() && peek() != '%') advance();

            // Just skip the token; we don't care about it.
            start = current;

            if (peek() == '%') {
                advance();
                addToken(TokenType.PERCENT);
            }
        }

        private void number() {
            while (isDigit(peek())) advance();
            addToken(TokenType.NUMBER);
        }

        private boolean isDigit(char c) {
            return c >= '0' && c <= '9';
        }

        private char peek() {
            if (isAtEnd()) return '\0';
            return source.charAt(current);
        }

        private boolean isAtEnd() {
            return current >= source.length();
        }

        private char advance() {
            return source.charAt(current++);
        }

        private void addToken(TokenType type) {
            tokens.add(new Token(type, source.substring(start, current)));
        }
    }

    private static class FormatParser {
        private final List<Token> tokens;
        private int current = 0;

        FormatParser(String source) {
            this.tokens = new Scanner(source).scanTokens();
            System.out.println("Got tokens: " + tokens);
        }

        List<ArgType> parse() {
            var argTypes = new ArrayList<ArgType>();

            while (!isAtEnd()) {
                var argType = argType();
                if (argType != null) {
                    argTypes.add(argType());
                }
            }

            return argTypes;
        }

        private ArgType argType() {
            consume(TokenType.PERCENT, "Expected '%'.");

            var gotFlag = false;
            var gotWidth = false;
            var gotPrecision = false;

            while (match(TokenType.FLAG)) {
                gotFlag = true;
            }

            if (check(TokenType.NUMBER)) {
                gotWidth = true;
                advance();
            }

            if (check(TokenType.DOT)) {
                gotPrecision = true;
                consume(TokenType.NUMBER,
                    "Expected precision after '.'.");
            }

            var conv = consume(TokenType.CONVERSION,
                "Expected conversion character.");
            return switch (conv.lexeme().charAt(0)) {
                case 'b', 'B', 'h', 'H', 's', 'S' -> ArgType.ANY;
                case 'd', 'x', 'X' -> {
                    if (gotPrecision) {
                        throw new JoeError(
                        "The precision field is not allowed for this conversion: '" +
                            conv.lexeme() + "'.");
                    }
                    yield ArgType.INT;
                }
                case 'e', 'E', 'f', 'g', 'G' -> ArgType.DOUBLE;
                default -> {
                    if (gotFlag || gotWidth || gotPrecision) {
                        throw new JoeError(
                            "Flags, width, etc., are not allowed for this conversion: '" +
                            conv.lexeme() + "'.");
                    }
                    yield null;
                }
            };
        }

        private Token peek() {
            return tokens.get(current);
        }

        private Token previous() {
            return tokens.get(current - 1);
        }

        private Token consume(TokenType type, String message) {
            if (check(type)) return advance();

            throw new JoeError(message);
        }

        private Token advance() {
            if (!isAtEnd()) current++;
            return previous();
        }

        private boolean match(TokenType... types) {
            for (TokenType type : types) {
                if (check(type)) {
                    advance();
                    return true;
                }
            }

            return false;
        }

        private boolean check(TokenType type) {
            if (isAtEnd()) return false;
            return peek().type() == type;
        }

        private boolean isAtEnd() {
            return peek().type() == TokenType.EOF;
        }
    }


}
