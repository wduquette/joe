package com.wjduquette.joe;

import org.junit.Test;

import static com.wjduquette.joe.checker.Checker.*;

public class ArgsTest extends Ted {
    Args args;

    @Test
    public void testEmpty() {
        args = new Args();
        check(args.size()).eq(0);
        check(args.isEmpty()).eq(true);
        check(args.remaining()).eq(0);
        check(args.hasRemaining()).eq(false);
        check(args.asArray().length).eq(0);
        check(args.remainderAsArray().length).eq(0);
        check(args.asList().size()).eq(0);
        check(args.remainderAsList().size()).eq(0);

        checkThrow(() -> args.next())
            .containsString("next() called when Args queue is empty.");
        checkThrow(() -> args.getRemaining(0))
            .containsString("Expected index in range 0");
    }

    @Test
    public void testArgs() {
        args = Args.of(1, 2, 3);

        check(args.size()).eq(3);
        check(args.isEmpty()).eq(false);
        check(args.remaining()).eq(3);
        check(args.hasRemaining()).eq(true);
        check(args.asArray().length).eq(3);
        check(args.remainderAsArray().length).eq(3);
        check(args.asList().size()).eq(3);
        check(args.remainderAsList().size()).eq(3);
        checkList(args.remainderAsList()).items(1, 2, 3);

        check(args.next()).eq(1);

        check(args.remaining()).eq(2);
        check(args.hasRemaining()).eq(true);
        check(args.remainderAsArray().length).eq(2);
        check(args.remainderAsList().size()).eq(2);
        checkList(args.remainderAsList()).items(2, 3);

        check(args.next()).eq(2);

        check(args.remaining()).eq(1);
        check(args.hasRemaining()).eq(true);
        check(args.remainderAsArray().length).eq(1);
        check(args.remainderAsList().size()).eq(1);
        checkList(args.remainderAsList()).items(3);

        check(args.next()).eq(3);

        check(args.remaining()).eq(0);
        check(args.hasRemaining()).eq(false);
        check(args.remainderAsArray().length).eq(0);
        check(args.remainderAsList().size()).eq(0);
    }
}
