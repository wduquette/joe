package com.wjduquette.joe;

import com.wjduquette.joe.scanner.SourceBuffer;
import org.junit.Test;

import static com.wjduquette.joe.checker.Checker.*;
import static com.wjduquette.joe.scanner.SourceBuffer.Position;

public class SourceBufferTest extends Ted {
    SourceBuffer buff;

    @Test
    public void testBasics() {
        buff = new SourceBuffer("*script*", "abc");
        check(buff.filename()).eq("*script*");
        check(buff.source()).eq("abc");
        check(buff.lineCount()).eq(1);
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
        check(buff.index2line(0)).eq(1);
        check(buff.index2line(1)).eq(1);
        check(buff.index2line(2)).eq(1);
        check(buff.index2line(3)).eq(1);
        check(buff.index2line(4)).eq(2);
        check(buff.index2line(5)).eq(2);
        check(buff.index2line(6)).eq(2);
        check(buff.index2line(buff.source().length())).eq(2);
    }

    @Test
    public void testIndex2Position() {
        buff = new SourceBuffer("-", "abc\ndef");
        check(buff.index2position(0)).eq(new Position(1, 1));
        check(buff.index2position(1)).eq(new Position(1, 2));
        check(buff.index2position(2)).eq(new Position(1, 3));
        check(buff.index2position(3)).eq(new Position(1, 4));
        check(buff.index2position(4)).eq(new Position(2, 1));
        check(buff.index2position(5)).eq(new Position(2, 2));
        check(buff.index2position(6)).eq(new Position(2, 3));
        check(buff.index2position(buff.source().length()))
            .eq(new Position(2, 4));
    }

    @Test
    public void testSpan() {
        buff = new SourceBuffer("-", "abc\ndef");
        var span = buff.span(1,3);
        check(span.buffer()).eq(buff);
        check(span.filename()).eq("-");
        check(span.text()).eq("bc");
        check(span.start()).eq(1);
        check(span.end()).eq(3);

        var span2 = buff.span(1,5);
        check(span2.startLine()).eq(1);
        check(span2.endLine()).eq(2);
        check(span2.startPosition()).eq(new Position(1, 2));
        check(span2.endPosition()).eq(new Position(2, 2));

        var all = buff.span(0, buff.source().length());
        check(all.text()).eq("abc\ndef");
    }

    @Test
    public void testLineSpan() {
        buff = new SourceBuffer("-", "abc\ndef");
        check(buff.lineSpan(1).text()).eq("abc\n");
        check(buff.lineSpan(2).text()).eq("def");
    }
}
