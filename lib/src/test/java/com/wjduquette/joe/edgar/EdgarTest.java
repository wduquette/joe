package com.wjduquette.joe.edgar;

import com.wjduquette.joe.Joe;
import com.wjduquette.joe.JoeError;
import com.wjduquette.joe.Ted;
import org.junit.Before;
import org.junit.Test;

import static com.wjduquette.joe.checker.Checker.*;

public class EdgarTest extends Ted {
    Joe joe;
    Edgar edgar;

    @Before
    public void setup() {
        this.joe = new Joe();
        this.edgar = new Edgar(joe);
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
        edgar.setMacroStart("(*");
        edgar.setMacroEnd("*)");

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
        edgar.setPassCount(2);
        check(expand("passes=<<Edgar.getPassCount()>>")).eq("passes=2");
        check(expand("pass=<<Edgar.getPass()>>")).eq("pass=2");
        check(expand("isPass=<<Edgar.isPass(1)>>")).eq("isPass=false");
        check(expand("isPass=<<Edgar.isPass(2)>>")).eq("isPass=true");
    }

    private String expand(String source) {
        return edgar.expand("*expand*", source);
    }
}
