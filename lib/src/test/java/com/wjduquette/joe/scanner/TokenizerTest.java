package com.wjduquette.joe.scanner;

import com.wjduquette.joe.Keyword;
import com.wjduquette.joe.Ted;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.wjduquette.joe.scanner.TokenType.*;
import static com.wjduquette.joe.scanner.TokenType.WHILE;
import static com.wjduquette.joe.checker.Checker.check;

public class TokenizerTest extends Ted {
    private Tokenizer tokenizer;
    private final List<String> details = new ArrayList<>();

    //-------------------------------------------------------------------------
    // Reserved Words

    @Test
    public void testReserved() {
        test("testReserved)");
        check(scanType("assert")).eq(ASSERT);
        check(scanType("break")).eq(BREAK);
        check(scanType("case")).eq(CASE);
        check(scanType("class")).eq(CLASS);
        check(scanType("continue")).eq(CONTINUE);
        check(scanType("default")).eq(DEFAULT);
        check(scanType("else")).eq(ELSE);
        check(scanType("extends")).eq(EXTENDS);
        check(scanType("false")).eq(FALSE);
        check(scanType("for")).eq(FOR);
        check(scanType("foreach")).eq(FOREACH);
        check(scanType("function")).eq(FUNCTION);
        check(scanType("if")).eq(IF);
        check(scanType("in")).eq(IN);
        check(scanType("method")).eq(METHOD);
        check(scanType("ni")).eq(NI);
        check(scanType("null")).eq(NULL);
        check(scanType("return")).eq(RETURN);
        check(scanType("static")).eq(STATIC);
        check(scanType("super")).eq(SUPER);
        check(scanType("switch")).eq(SWITCH);
        check(scanType("this")).eq(THIS);
        check(scanType("throw")).eq(THROW);
        check(scanType("true")).eq(TRUE);
        check(scanType("var")).eq(VAR);
        check(scanType("while")).eq(WHILE);
    }

    //-------------------------------------------------------------------------
    // One and two character tokens

    @Test
    public void testSimple() {
        test("testSimple");
        check(scanType("(")).eq(LEFT_PAREN);
        check(scanType(")")).eq(RIGHT_PAREN);
        check(scanType("{")).eq(LEFT_BRACE);
        check(scanType("}")).eq(RIGHT_BRACE);
        check(scanType(";")).eq(SEMICOLON);
        check(scanType(",")).eq(COMMA);
        check(scanType(".")).eq(DOT);
        check(scanType("-")).eq(MINUS);
        check(scanType("+")).eq(PLUS);
        check(scanType("/")).eq(SLASH);
        check(scanType("*")).eq(STAR);
        check(scanType("!")).eq(BANG);
        check(scanType("!=")).eq(BANG_EQUAL);
        check(scanType("=")).eq(EQUAL);
        check(scanType("==")).eq(EQUAL_EQUAL);
        check(scanType("<")).eq(LESS);
        check(scanType("<=")).eq(LESS_EQUAL);
        check(scanType(">")).eq(GREATER);
        check(scanType(">=")).eq(GREATER_EQUAL);
        check(scanType("&&")).eq(AND);
        check(scanType("||")).eq(OR);
    }

    //-------------------------------------------------------------------------
    // Identifiers

    @Test
    public void testIdentifier() {
        test("testIdentifier");
        var token = scanToken("abc123");
        check(token.type()).eq(IDENTIFIER);
        check(token.lexeme()).eq("abc123");

        token = scanToken("_abc123");
        check(token.type()).eq(IDENTIFIER);
        check(token.lexeme()).eq("_abc123");
    }

    //-------------------------------------------------------------------------
    // Keywords

    @Test
    public void testKeyword() {
        test("testKeyword");
        var token = scanToken("#abc123");
        check(token.type()).eq(KEYWORD);
        check(token.literal()).eq(new Keyword("abc123"));

        token = scanToken("#_abc123");
        check(token.type()).eq(KEYWORD);
        check(token.literal()).eq(new Keyword("_abc123"));
    }

    //-------------------------------------------------------------------------
    // Numeric Literals

    @Test
    public void testNumbers() {
        test("testNumbers");
        check(scanLiteral("123")).eq(123.0);
        check(scanLiteral("123.4")).eq(123.4);
        check(scanLiteral("123.4e2")).eq(12340.0);
        check(scanLiteral("123.4E2")).eq(12340.0);
        check(scanLiteral("123.4e+2")).eq(12340.0);
        check(scanLiteral("123.4e-2")).eq(1.234);
        check(scanLiteral("0xff")).eq(255.0);
        check(scanLiteral("0xFF")).eq(255.0);
    }

    //-------------------------------------------------------------------------
    // String Literals

    @Test
    public void testString_escapes() {
        test("testString_escapes");
        check(scanString("\"abc\"")).eq("abc");
        check(scanString("\"-\\\\-\"")).eq("-\\-");
        check(scanString("\"-\\t-\"")).eq("-\t-");
        check(scanString("\"-\\b-\"")).eq("-\b-");
        check(scanString("\"-\\n-\"")).eq("-\n-");
        check(scanString("\"-\\r-\"")).eq("-\r-");
        check(scanString("\"-\\f-\"")).eq("-\f-");
        check(scanString("\"-\\\"-\"")).eq("-\"-");
        check(scanString("\"-\\u2192-\"")).eq("-→-");
    }

    @Test
    public void testString_unterminated() {
        test("testString_unterminated");
        check(scanError("\"abc"))
            .hasString("At end, unterminated string.");
    }

    @Test
    public void testString_unescapedNewline() {
        test("testString_unescapedNewline");
        check(scanError("\"a\nbc\""))
            .hasString("At '\"a', unescaped newline in single-line string.");
    }

    @Test
    public void testString_invalidEscape() {
        test("testString_invalidEscape");
        check(scanError("\"\\x\""))
            .hasString("At '\"\\x', invalid escape.");
    }

    @Test
    public void testString_incompleteUnicodeEscape() {
        test("testString_incompleteUnicodeEscape");
        check(scanError("\"\\u123\""))
            .hasString("At '\"\\u123', incomplete Unicode escape.");
    }

    //-------------------------------------------------------------------------
    // Raw Text Strings

    @Test
    public void testRawString_ok() {
        test("testRawString_ok");
        check(scanString("'abc'")).eq("abc");
        check(scanString("'-\\\\-'")).eq("-\\\\-");
        check(scanString("'-\\x-'")).eq("-\\x-");
        check(scanString("'-\\t-'")).eq("-\\t-");
        check(scanString("'-\\b-'")).eq("-\\b-");
        check(scanString("'-\\n-'")).eq("-\\n-");
        check(scanString("'-\\r-'")).eq("-\\r-");
        check(scanString("'-\\f-'")).eq("-\\f-");
        check(scanString("'-\\\"-'")).eq("-\\\"-");
        check(scanString("'-\\u2192-'")).eq("-\\u2192-");
    }

    @Test
    public void testRawString_unterminated() {
        test("testRawString_unterminated");
        check(scanError("'abc"))
            .hasString("At end, unterminated raw string.");
    }

    @Test
    public void testRawString_newline() {
        test("testRawString_newline");
        check(scanError("'a\nbc'"))
            .hasString("At ''a\n', newline in raw string.");
    }

    //-------------------------------------------------------------------------
    // Text Blocks

    // Adds text block boilerplate to a simple text string.
    private String block(String text) {
        return "\"\"\"\n" + text + "\n\"\"\"";
    }

    @Test
    public void testTextBlock_outdent() {
        test("testTextBlock_outdent");
        check(scanString(block("      First\n  Second")))
            .eq("""
                    First
                Second
                """.stripTrailing());
    }

    @Test
    public void testTextBlock_escapes() {
        test("testTextBlock_escapes");
        check(scanString(block("abc"))).eq("abc");
        check(scanString(block("-\\\\-"))).eq("-\\-");
        check(scanString(block("-\\t-"))).eq("-\t-");
        check(scanString(block("-\\b-"))).eq("-\b-");
        check(scanString(block("-\\n-"))).eq("-\n-");
        // stripIndent mungs '\r', quite reasonably.
        check(scanString(block("-\\r-"))).eq("-\n-");
        check(scanString(block("-\\f-"))).eq("-\f-");
        check(scanString(block("-\\\"-"))).eq("-\"-");
        check(scanString(block("-\\u2192-"))).eq("-→-");
    }

    @Test
    public void testTextBlock_unterminated() {
        test("testTextBlock_unterminated");
        check(scanError("\"\"\"abc"))
            .hasString("At end, unterminated text block.");
    }

    @Test
    public void testTextBlock_invalidEscape() {
        test("testTextBlock_invalidEscape");
        check(scanError(block("\\x")))
            .hasString("At '\"\"\"\n\\x', invalid escape.");
    }

    @Test
    public void testTextBlock_incompleteUnicodeEscape() {
        test("testTextBlock_incompleteUnicodeEscape");
        check(scanError(block("\\u123")))
            .hasString("At '\"\"\"\n\\u123', incomplete Unicode escape.");
    }

    //-------------------------------------------------------------------------
    // Raw Text Blocks

    // Adds raw text block boilerplate to a simple text string.
    private String raw(String text) {
        return "'''\n" + text + "\n'''";
    }

    @Test
    public void testRawBlock_outdent() {
        test("testRawBlock_outdent");
        check(scanString(raw("      First\n  Second")))
            .eq("""
                    First
                Second
                """.stripTrailing());
    }

    @Test
    public void testRawBlock_escapes() {
        test("testRawBlock_escapes");
        check(scanString(raw("abc"))).eq("abc");
        check(scanString(raw("-\\n-"))).eq("-\\n-");
    }

    @Test
    public void testRawBlock_unterminated() {
        test("testRawBlock_unterminated");
        check(scanError("'''abc"))
            .hasString("At end, unterminated raw text block.");
    }

    //-------------------------------------------------------------------------
    // Miscellaneous Errors

    @Test
    public void testError_incompleteAnd() {
        test("testError_incompleteAnd");
        check(scanError("&-"))
            .hasString("At '&', unexpected character.");
    }

    @Test
    public void testError_incompleteOr() {
        test("testError_incompleteOr");
        check(scanError("|-"))
            .hasString("At '|', unexpected character.");
    }

    @Test
    public void testError_unexpectedChar() {
        test("testError_unexpectedChar");
        check(scanError("^1"))
            .hasString("At '^', unexpected character.");
    }

    //-------------------------------------------------------------------------
    // Helpers

    private void scanInput(String input) {
        details.clear();

        var source = new SourceBuffer("-", input);
        tokenizer = new Tokenizer(source);
    }

    private Token next() {
        Token token;
        do {
            token = tokenizer.scanToken();
            if (token.type() == ERROR) {
                System.out.println("detail: " +
                    token.span() + " " + token.literal());
                details.add((String)token.literal());
            }
        } while (token.type() == ERROR);

        return token;
    }

    // Scans and returns the first error
    private String scanError(String input) {
        scanInput(input);

        // Scan until end.
        Token token;
        do {
            token = next();
        } while (token.type() != TokenType.EOF);

        return details.isEmpty() ? null : details.getFirst();
    }

    private Token scanToken(String input) {
        scanInput(input);
        return next();
    }

    private Object scanLiteral(String input) {
        scanInput(input);
        return next().literal();
    }

    private TokenType scanType(String input) {
        scanInput(input);
        var token = next();

        return token.type();
    }

    private String scanString(String input) {
        println("scanString[" + input + "]");
        scanInput(input);
        var token = next();

        return token.type() == TokenType.STRING
            ? (String)token.literal()
            : null;
    }

    @SuppressWarnings("unused")
    private void dumpChars(String title, String chars) {
        println("dumpChars " + title);
        for (var i = 0; i < chars.length(); i++) {
            var c = chars.charAt(i);
            println("  [" + i + "] = " + (int)c);
        }
    }
}
