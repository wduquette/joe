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

        var out = expander.expand("A<<5>>B");
        check(out).eq("A5B");
    }

    @Test
    public void testExpander_call() {
        test("testExpander_call");

        var out = expander.expand("A<<Number.abs(-5)>>B");
        check(out).eq("A5B");
    }

    @Test
    public void testExpander_specialDelimiters() {
        test("testExpander_specialDelimiters");
        expander.setTemplateStart("(*");
        expander.setTemplateEnd("*)");

        var out = expander.expand("<<A(*5*)B>>");
        check(out).eq("<<A5B>>");
    }

    @Test
    public void testExpander_macroError() {
        test("testExpander_macroError");

        try {
            expander.expand("A<<Number.nonesuch(5)>>B");
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
            expander.expand("A<<5>B");
            fail("Should have thrown an error");
        } catch (JoeError ex) {
            check(ex.getMessage()).eq("Unterminated macro at (1,4) in source.");
        }
    }
}
