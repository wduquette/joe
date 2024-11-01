package com.wjduquette.joe.walker;

import com.wjduquette.joe.Ted;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.wjduquette.joe.checker.Checker.check;

public class ScannerTest extends Ted {
    private final List<String> details = new ArrayList<>();

    @Test
    public void testStringEscapes() {
        test("testStringEscapes");
        check(scanString("\"abc\"")).eq("abc");
        check(scanString("\"-\\\\-\"")).eq("-\\-");
        check(scanString("\"-\\t-\"")).eq("-\t-");
        check(scanString("\"-\\b-\"")).eq("-\b-");
        check(scanString("\"-\\n-\"")).eq("-\n-");
        check(scanString("\"-\\r-\"")).eq("-\r-");
        check(scanString("\"-\\f-\"")).eq("-\f-");
        check(scanString("\"-\\\"-\"")).eq("-\"-");
        check(scanString("\"-\\u2192-\"")).eq("-→-");

        check(scanString("\"-\\\"")).eq(null);
    }

    @Test
    public void testError_incompleteAnd() {
        test("testError_incompleteAnd");
        check(scan("&-"))
            .hasString("Expected '&&', got: '&'.");
    }

    @Test
    public void testError_incompleteOr() {
        test("testError_incompleteOr");
        check(scan("|-"))
            .hasString("Expected '||', got: '|'.");
    }

    @Test
    public void testError_unexpectedChar() {
        test("testError_unexpectedChar");
        check(scan("^"))
            .hasString("Unexpected character: '^'.");
    }

    @Test
    public void testError_unexpectedEscape() {
        test("testError_unexpectedEscape");
        check(scan("\"\\x\""))
            .hasString("Unexpected escape: '\\x'.");
    }

    @Test
    public void testError_unterminatedString() {
        test("testError_unterminatedString");
        check(scan("\"abc"))
            .hasString("Unterminated string.");
    }

    @Test
    public void testError_incompleteUnicodeEscape() {
        test("testError_incompleteUnicodeEscape");
        check(scan("\"\\u123\""))
            .hasString("Incomplete Unicode escape: '\\u123'.");
    }

    // Scans and returns the first error
    private String scan(String input) {
        details.clear();
        var scanner = new Scanner(input, detail -> {
            System.out.println("detail: " + detail);
            details.add(detail.message());
        });
        scanner.scanTokens();
        return details.isEmpty() ? null : details.getFirst();
    }

    private String scanString(String input) {
        var scanner = new Scanner(input, detail -> {});
        var result = scanner.scanTokens();
        if (result.isEmpty()) {
            return null;
        }

        var token = result.getFirst();
        if (token.type() == TokenType.STRING) {
            return (String)token.literal();
        } else {
            return null;
        }
    }
}
