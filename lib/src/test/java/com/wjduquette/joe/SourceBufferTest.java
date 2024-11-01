package com.wjduquette.joe;

import org.junit.Test;

import static com.wjduquette.joe.checker.Checker.*;

public class SourceBufferTest extends Ted {
    SourceBuffer buff;

    @Test
    public void testBasics() {
        buff = new SourceBuffer("*script*", "abc");
        check(buff.filename()).eq("*script*");
        check(buff.source()).eq("abc");
        check(buff.lineCount() == 1);
    }

    @Test
    public void testLines_empty() {
        buff = new SourceBuffer("-", "");
        check(buff.lineCount()).eq(1);
        check(buff.line(1)).eq("");
    }

    @Test
    public void testLines_one() {
        buff = new SourceBuffer("-", "abc");
        check(buff.lineCount()).eq(1);
        check(buff.line(1)).eq("abc");
    }

    @Test
    public void testLines_onePlusNewLine() {
        buff = new SourceBuffer("-", "abc\n");
        check(buff.lineCount()).eq(1);
        check(buff.line(1)).eq("abc");
    }

    @Test
    public void testLines_two() {
        buff = new SourceBuffer("-", "abc\ndef");
        check(buff.lineCount()).eq(2);
        check(buff.line(1)).eq("abc");
        check(buff.line(2)).eq("def");
    }

    @Test
    public void testIndex2line() {
        buff = new SourceBuffer("-", "abc\ndef");
//        check(buff.index2line(0)).eq(1);
//        check(buff.index2line(1)).eq(1);
//        check(buff.index2line(2)).eq(1);
//        check(buff.index2line(3)).eq(1);
//        check(buff.index2line(4)).eq(2);
//        check(buff.index2line(5)).eq(2);
//        check(buff.index2line(6)).eq(2);
//        check(buff.index2line(buff.source().length())).eq(2);
    }
}
