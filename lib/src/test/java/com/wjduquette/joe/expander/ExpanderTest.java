package com.wjduquette.joe.expander;

import com.wjduquette.joe.JoeError;
import com.wjduquette.joe.Ted;
import org.junit.Before;
import org.junit.Test;

import static com.wjduquette.joe.checker.Checker.*;

public class ExpanderTest extends Ted {
    Expander expander;

    @Before
    public void setup() {
        this.expander = new Expander();
    }

    @Test
    public void testExpander_number() {
        test("testExpander_number");

        var out = expand("A<<5>>B");
        check(out).eq("A5B");
    }

    @Test
    public void testExpander_call() {
        test("testExpander_call");

        var out = expand("A<<Number.abs(-5)>>B");
        check(out).eq("A5B");
    }

    @Test
    public void testExpander_setBrackets() {
        test("testExpander_setBrackets");
        expander.setBrackets("(*", "*)");

        var out = expand("<<A(*5*)B>>");
        check(out).eq("<<A5B>>");
    }

    @Test
    public void testErrorMode_fail() {
        test("testErrorMode_fail");

        try {
            expand("A<<Number.nonesuch(5)>>B");
            fail("Should have thrown an error");
        } catch (JoeError ex) {
            check(ex.getMessage()).eq("Undefined property 'nonesuch'.");
            check(ex.getTraceReport())
                .eq("In macro 'Number.nonesuch(5)'\nAt (1,4) in source");
        }
    }

    @Test
    public void testErrorMode_macro() {
        test("testErrorMode_macro");

        expander.setErrorMode(Expander.ErrorMode.MACRO);
        var source = "A<<Number.nonesuch(5)>>B";
        check(expand(source)).eq(source);
    }

    @Test
    public void testErrorMode_ignore() {
        test("testErrorMode_ignore");

        expander.setErrorMode(Expander.ErrorMode.IGNORE);
        var source = "A<<Number.nonesuch(5)>>B";
        check(expand(source)).eq("AB");
    }

    @Test
    public void testExpander_unterminatedMacro() {
        test("testExpander_unterminatedMacro");

        try {
            expand("A<<5>B");
            fail("Should have thrown an error");
        } catch (JoeError ex) {
            check(ex.getMessage()).eq("Unterminated macro at (1,4) in source.");
        }
    }

    @Test
    public void testLoadConfiguration() {
        var config = """
            Expander.setBrackets("{{", "}}");
            var title = "Howdy!";
            """;
        var source = """
            Title: {{title}}.
            """;
        expander.loadConfiguration("*config*", config);
        var out = expand(source).strip();
        check(out).eq("Title: Howdy!.");
    }

    @Test
    public void testContextStack_empty() {
        test("testContextStack_empty");
        var source = """
            Current=[<<Expander.current()>>]
            """;
        check(expand(source).stripTrailing())
            .eq("Current=[null]");
    }

    @Test
    public void testContextStack_pushPop_ok() {
        test("testContextStack_pushPop_ok");
        var source = """
            +++<<Expander.push(#test)>>abcd<<Expander.pop(#test)>>+++
            """;
        check(expand(source).stripTrailing())
            .eq("+++abcd+++");
    }

    @Test
    public void testContextStack_pushPop_mismatch() {
        test("testContextStack_pushPop_mismatch");
        var source = """
            +++<<Expander.push(#a)>>abcd<<Expander.pop(#b)>>+++
            """;
        checkThrow(() -> expand(source))
            .containsString("Expected context '#a', got: Keyword '#b'");
    }

    private String expand(String source) {
        return expander.expand("*expand*", source);
    }
}
