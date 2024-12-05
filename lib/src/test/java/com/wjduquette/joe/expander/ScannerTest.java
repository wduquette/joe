package com.wjduquette.joe.expander;

import com.wjduquette.joe.Joe;
import com.wjduquette.joe.JoeError;
import com.wjduquette.joe.Ted;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static com.wjduquette.joe.checker.Checker.*;
import static com.wjduquette.joe.expander.TokenType.*;

public class ScannerTest extends Ted {
    Joe joe;
    Expander expander;

    @Before
    public void setup() {
        this.joe = new Joe();
        this.expander = new Expander(joe);
    }

    @Test
    public void testSimple() {
        test("testSimple");
        var tokens = scan("Foo <<bar>> baz.");
        check(tokens.size()).eq(6);
        check(tokens.get(0).type()).eq(TEXT);
        check(tokens.get(0).text()).eq("Foo ");
        check(tokens.get(1).type()).eq(START);
        check(tokens.get(1).text()).eq("<<");
        check(tokens.get(2).type()).eq(MACRO);
        check(tokens.get(2).text()).eq("bar");
        check(tokens.get(3).type()).eq(END);
        check(tokens.get(3).text()).eq(">>");
        check(tokens.get(4).type()).eq(TEXT);
        check(tokens.get(4).text()).eq(" baz.");
        check(tokens.get(5).type()).eq(EOF);
    }

    @Test
    public void testNoLeadingText() {
        test("testNoLeadingText");
        var tokens = scan("<<bar>> baz.");
        check(tokens.size()).eq(6);
        check(tokens.get(0).type()).eq(TEXT);
        check(tokens.get(0).text()).eq("");
        check(tokens.get(1).type()).eq(START);
        check(tokens.get(1).text()).eq("<<");
        check(tokens.get(2).type()).eq(MACRO);
        check(tokens.get(2).text()).eq("bar");
        check(tokens.get(3).type()).eq(END);
        check(tokens.get(3).text()).eq(">>");
        check(tokens.get(4).type()).eq(TEXT);
        check(tokens.get(4).text()).eq(" baz.");
        check(tokens.get(5).type()).eq(EOF);
    }

    @Test
    public void testNoTrailingText() {
        test("testNoTrailingText");
        var tokens = scan("Foo <<bar>>");
        check(tokens.size()).eq(5);
        check(tokens.get(0).type()).eq(TEXT);
        check(tokens.get(0).text()).eq("Foo ");
        check(tokens.get(1).type()).eq(START);
        check(tokens.get(1).text()).eq("<<");
        check(tokens.get(2).type()).eq(MACRO);
        check(tokens.get(2).text()).eq("bar");
        check(tokens.get(3).type()).eq(END);
        check(tokens.get(3).text()).eq(">>");
        check(tokens.get(4).type()).eq(EOF);
    }

    @Test
    public void testUnterminatedMacro() {
        test("testUnterminatedMacro");
        checkThrow(() -> scan("Foo <<bar> baz."))
            .containsString("Unterminated macro at (1,7) in source.");
    }

    @Test
    public void testIgnoreUnexpectedStart() {
        test("testIgnoreUnexpectedStart");
        var tokens = scan("Foo <<bar<<quux>> baz.");
        check(tokens.size()).eq(6);
        check(tokens.get(0).type()).eq(TEXT);
        check(tokens.get(0).text()).eq("Foo ");
        check(tokens.get(1).type()).eq(START);
        check(tokens.get(1).text()).eq("<<");
        check(tokens.get(2).type()).eq(MACRO);
        check(tokens.get(2).text()).eq("bar<<quux");
        check(tokens.get(3).type()).eq(END);
        check(tokens.get(3).text()).eq(">>");
        check(tokens.get(4).type()).eq(TEXT);
        check(tokens.get(4).text()).eq(" baz.");
        check(tokens.get(5).type()).eq(EOF);
    }

    @Test
    public void testIgnoreUnexpectedEnd() {
        test("testIgnoreUnexpectedENd");
        var tokens = scan("Foo <<bar>> quux>>baz.");
        check(tokens.size()).eq(6);
        check(tokens.get(0).type()).eq(TEXT);
        check(tokens.get(0).text()).eq("Foo ");
        check(tokens.get(1).type()).eq(START);
        check(tokens.get(1).text()).eq("<<");
        check(tokens.get(2).type()).eq(MACRO);
        check(tokens.get(2).text()).eq("bar");
        check(tokens.get(3).type()).eq(END);
        check(tokens.get(3).text()).eq(">>");
        check(tokens.get(4).type()).eq(TEXT);
        check(tokens.get(4).text()).eq(" quux>>baz.");
        check(tokens.get(5).type()).eq(EOF);
    }

    private List<Token> scan(String source) throws JoeError {
        var scanner = new Scanner(expander, "*scan*", source);
        return scanner.getTokens();
    }
}
