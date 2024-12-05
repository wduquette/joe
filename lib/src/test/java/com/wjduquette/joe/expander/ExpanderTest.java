package com.wjduquette.joe.expander;

import com.wjduquette.joe.Joe;
import com.wjduquette.joe.JoeError;
import com.wjduquette.joe.Ted;
import org.junit.Before;
import org.junit.Test;

import static com.wjduquette.joe.checker.Checker.*;

public class ExpanderTest extends Ted {
    Joe joe;
    Expander expander;

    @Before
    public void setup() {
        this.joe = new Joe();
        this.expander = new Expander(joe);
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
    public void testExpander_specialDelimiters() {
        test("testExpander_specialDelimiters");
        expander.setMacroStart("(*");
        expander.setMacroEnd("*)");

        var out = expand("<<A(*5*)B>>");
        check(out).eq("<<A5B>>");
    }

    @Test
    public void testExpander_macroError() {
        test("testExpander_macroError");

        try {
            expand("A<<Number.nonesuch(5)>>B");
            fail("Should have thrown an error");
        } catch (JoeError ex) {
            check(ex.getMessage()).eq("Undefined property 'nonesuch'.");
            check(ex.getTraceReport()).eq("At (1,4) in source");
        }
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
    public void testEdgarPass_single() {
        check(expand("passes=<<Edgar.getPassCount()>>")).eq("passes=1");
        check(expand("pass=<<Edgar.getPass()>>")).eq("pass=1");
        check(expand("isPass=<<Edgar.isPass(1)>>")).eq("isPass=true");
        check(expand("isPass=<<Edgar.isPass(2)>>")).eq("isPass=false");
    }

    @Test
    public void testEdgarPass_double() {
        expander.setPassCount(2);
        check(expand("passes=<<Edgar.getPassCount()>>")).eq("passes=2");
        check(expand("pass=<<Edgar.getPass()>>")).eq("pass=2");
        check(expand("isPass=<<Edgar.isPass(1)>>")).eq("isPass=false");
        check(expand("isPass=<<Edgar.isPass(2)>>")).eq("isPass=true");
    }

    @Test
    public void testLoadConfiguration() {
        var config = """
            Edgar.setPassCount(2)
                 .setMacroStart("{{")
                 .setMacroEnd("}}");
            var title = "Howdy!";
            """;
        var source = """
            Title: {{title}} on pass {{Edgar.getPass()}}.
            """;
        expander.loadConfiguration("*config*", config);
        var out = expand(source).strip();
        check(out).eq("Title: Howdy! on pass 2.");
    }

    private String expand(String source) {
        return expander.expand("*expand*", source);
    }
}
