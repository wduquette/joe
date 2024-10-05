package com.wjduquette.joe;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static com.wjduquette.joe.checker.Checker.check;
import static com.wjduquette.joe.checker.Checker.checkThrow;

public class StringFormatterTest extends Ted {
    private Joe joe;

    @Before public void setup() {
        this.joe = new Joe();
    }

    @Test
    public void testOK() {
        test("testOK");
        check(fmt("%s", "a")).eq("a");
        check(fmt("%s", 123)).eq("123");
        check(fmt("%S", "a")).eq("A");
        check(fmt("%3s", "a")).eq("  a");
        check(fmt("%-3s", "a")).eq("a  ");
        check(fmt("%b", true)).eq("true");
        check(fmt("%B", true)).eq("TRUE");
        check(fmt("%b", 5)).eq("true");
        check(fmt("%B", 5)).eq("TRUE");
        check(fmt("%d", 1.2)).eq("1");
        check(fmt("%x", 15.2)).eq("f");
        check(fmt("%X", 15.2)).eq("F");
        check(fmt("%.1f", 1.2)).eq("1.2");
        check(fmt("%.1e", 1.2)).eq("1.2e+00");
        check(fmt("%.1E", 1.2)).eq("1.2E+00");
        check(fmt("%.1g", 123456789.1234567)).eq("1e+08");
        check(fmt("%.1G", 123456789.1234567)).eq("1E+08");
        check(fmt("%h", "abcd")).eq("2d9442");
        check(fmt("%H", "abcd")).eq("2D9442");
        check(fmt("%n")).eq(System.lineSeparator());
        check(fmt("%%")).eq("%");

        check(fmt("***%s***", "a")).eq("***a***");
    }

    @Test
    public void testParseError() {
        checkThrow(() -> fmt("%#s"))
            .containsString("Invalid character in conversion: '#'.");
    }

    @Test
    public void testTypeError_wrongNumber() {
        checkThrow(() -> fmt("%s%s", "a"))
            .containsString("Expected 2 values to format, got: 1.");
        checkThrow(() -> fmt("%f", "abc"))
            .containsString(
                "Conversion expected a number, got: String 'abc'");
        checkThrow(() -> fmt("%d", "abc"))
            .containsString(
                "Conversion expected a number, got: String 'abc'");
    }

    @Test
    public void testJavaError() {
        checkThrow(() -> fmt("%3.2d", 1))
            .containsString("Invalid format string: '%3.2d'.");
    }

    private String fmt(String format, Object... values) {
        return StringFormatter.format(joe, format, List.of(values));
    }
}
