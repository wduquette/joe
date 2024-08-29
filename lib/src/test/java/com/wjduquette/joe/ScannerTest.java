package com.wjduquette.joe;

import org.junit.Before;
import org.junit.Test;

import static com.wjduquette.joe.checker.Checker.check;

public class ScannerTest extends Ted {
    private Joe joe;

    @Before public void setup() {
        joe = new Joe();
    }

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
    }

    private String scanString(String input) {
        var scanner = new Scanner(joe, input);
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
