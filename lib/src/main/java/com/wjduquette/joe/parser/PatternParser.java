package com.wjduquette.joe.parser;

import com.wjduquette.joe.patterns.Pattern;
import com.wjduquette.joe.scanner.Token;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import static com.wjduquette.joe.scanner.TokenType.*;
import static com.wjduquette.joe.scanner.TokenType.AT;
import static com.wjduquette.joe.scanner.TokenType.COLON;
import static com.wjduquette.joe.scanner.TokenType.COMMA;
import static com.wjduquette.joe.scanner.TokenType.DOLLAR;
import static com.wjduquette.joe.scanner.TokenType.FALSE;
import static com.wjduquette.joe.scanner.TokenType.IDENTIFIER;
import static com.wjduquette.joe.scanner.TokenType.KEYWORD;
import static com.wjduquette.joe.scanner.TokenType.LEFT_PAREN;
import static com.wjduquette.joe.scanner.TokenType.MINUS;
import static com.wjduquette.joe.scanner.TokenType.NULL;
import static com.wjduquette.joe.scanner.TokenType.NUMBER;
import static com.wjduquette.joe.scanner.TokenType.RIGHT_BRACE;
import static com.wjduquette.joe.scanner.TokenType.RIGHT_BRACKET;
import static com.wjduquette.joe.scanner.TokenType.RIGHT_PAREN;
import static com.wjduquette.joe.scanner.TokenType.STRING;
import static com.wjduquette.joe.scanner.TokenType.TRUE;

/**
 * A parser for Joe's pattern syntax, which is embedded in both Joe itself
 * and in Nero.  Intentionally package-private.
 */
class PatternParser extends EmbeddedParser {
    //-------------------------------------------------------------------------
    // Constructor

    public PatternParser(Parser parent) {
        super(parent);
    }

    //-------------------------------------------------------------------------
    // API

    /**
     * Parses a Joe pattern from the token stream, returning an
     * ASTPattern.
     * @return The pattern
     */
    public ASTPattern parse() {
        var astPattern = new ASTPattern();
        var pattern = parsePattern(astPattern, true);
        astPattern.setPattern(pattern);
        return astPattern;
    }

    //-------------------------------------------------------------------------
    // The Parser

    // Parses a pattern into the AST. `var @ pattern` is allowed if
    // canPatternBind.
    private Pattern parsePattern(ASTPattern wp, boolean canPatternBind) {
        var constant = constantPattern(wp);

        if (constant != null) {
            return constant;
        }

        if (scanner.match(LEFT_BRACKET)) {
            return listPattern(wp);
        } else if (scanner.match(LEFT_BRACE)) {
            return mapPattern(wp);
        } else if (scanner.match(IDENTIFIER)) {
            var identifier = scanner.previous();

            if (identifier.lexeme().startsWith("_")) {
                return new Pattern.Wildcard(identifier.lexeme());
            } else if (scanner.match(LEFT_PAREN)) {
                if (scanner.match(RIGHT_PAREN)) {
                    return new Pattern.TypeName(identifier.lexeme());
                } else if (scanner.peekNext().type() == COLON) {
                    return namedFieldPattern(wp, identifier);
                } else {
                    return recordPattern(wp, identifier);
                }
            }

            wp.saveBinding(identifier);
            var name = identifier.lexeme();

            if (canPatternBind && scanner.match(AT)) {
                var subpattern = parsePattern(wp, false);
                return new Pattern.Subpattern(name, subpattern);
            } else {
                return new Pattern.Variable(name);
            }
        } else {
            throw errorSync(scanner.peek(), "Expected pattern.");
        }
    }

    private Pattern constantPattern(ASTPattern wp) {
        if (scanner.match(MINUS)) {
            scanner.consume(NUMBER, "Expected number after '-'.");
            var number = (Double)scanner.previous().literal();
            return new Pattern.Constant(-number);
        }

        if (scanner.match(TRUE)) {
            return new Pattern.Constant(true);
        } else if (scanner.match(FALSE)) {
            return new Pattern.Constant(false);
        } else if (scanner.match(NULL)) {
            return new Pattern.Constant(null);
        } else if (scanner.match(NUMBER) || scanner.match(STRING) || scanner.match(KEYWORD)) {
            return new Pattern.Constant(scanner.previous().literal());
        } else if (scanner.match(DOLLAR)) {
            if (scanner.match(IDENTIFIER)) {
                return wp.addVarExpr(scanner.previous());
            } else {
                scanner.consume(LEFT_PAREN, "Expected identifier or '(' after '$'.");
                var expr = parent.expression();
                scanner.consume(RIGHT_PAREN,
                    "Expected ')' after interpolated expression.");
                return wp.addExpr(expr);
            }
        } else {
            return null;
        }
    }

    private Pattern listPattern(ASTPattern wp) {
        var list = new ArrayList<Pattern>();

        if (scanner.match(RIGHT_BRACKET)) {
            return new Pattern.ListPattern(list, null);
        }

        do {
            if (scanner.check(RIGHT_BRACKET) || scanner.check(COLON)) {
                break;
            }
            list.add(parsePattern(wp, true));
        } while (scanner.match(COMMA));

        String tailName = null;
        if (scanner.match(COLON)) {
            scanner.consume(IDENTIFIER, "Expected binding variable for list tail.");
            var tailVar = scanner.previous();
            wp.saveBinding(tailVar);
            tailName = tailVar.lexeme();
        }
        scanner.consume(RIGHT_BRACKET, "Expected ']' after list pattern.");

        return new Pattern.ListPattern(list, tailName);
    }

    private Pattern.MapPattern mapPattern(ASTPattern wp) {
        var map = new LinkedHashMap<Pattern,Pattern>();

        if (scanner.match(COLON)) {
            scanner.consume(RIGHT_BRACE, "expected '}' after empty map pattern.");
            return new Pattern.MapPattern(map);
        }

        do {
            if (scanner.check(RIGHT_BRACE)) {
                break;
            }
            var key = constantPattern(wp);
            scanner.consume(COLON, "Expected ':' after map key.");
            var value = parsePattern(wp, true);
            map.put(key, value);
        } while (scanner.match(COMMA));

        scanner.consume(RIGHT_BRACE, "Expected '}' after map pattern.");

        return new Pattern.MapPattern(map);
    }

    private Pattern namedFieldPattern(ASTPattern wp, Token identifier) {
        var fieldMap = new LinkedHashMap<String, Pattern>();

        if (scanner.match(RIGHT_PAREN)) {
            return new Pattern.NamedFieldPattern(identifier.lexeme(), fieldMap);
        }

        do {
            if (scanner.check(RIGHT_PAREN)) {
                break;
            }

            scanner.consume(IDENTIFIER, "Expected field name.");
            var key = scanner.previous().lexeme();
            scanner.consume(COLON, "Expected ':' after field name.");
            var value = parsePattern(wp, true);
            fieldMap.put(key, value);
        } while (scanner.match(COMMA));

        scanner.consume(RIGHT_PAREN, "Expected ')' after field pattern.");

        return new Pattern.NamedFieldPattern(identifier.lexeme(), fieldMap);
    }

    private Pattern recordPattern(ASTPattern wp, Token identifier) {
        var list = new ArrayList<Pattern>();

        if (scanner.match(RIGHT_PAREN)) {
            return new Pattern.OrderedFieldPattern(identifier.lexeme(), list);
        }

        do {
            if (scanner.check(RIGHT_PAREN)) {
                break;
            }
            list.add(parsePattern(wp, true));
        } while (scanner.match(COMMA));

        scanner.consume(RIGHT_PAREN, "Expected ')' after record pattern.");

        return new Pattern.OrderedFieldPattern(identifier.lexeme(), list);
    }


}
