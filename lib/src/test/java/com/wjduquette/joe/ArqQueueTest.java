package com.wjduquette.joe;

import org.junit.Test;

import static com.wjduquette.joe.checker.Checker.check;

public class ArqQueueTest extends Ted {
    Args args;

    @Test
    public void testEmpty() {
        args = new Args();
        check(args.size()).eq(0);
        check(args.isEmpty()).eq(true);
        check(args.numRemaining()).eq(0);
        check(args.hasRemaining()).eq(false);
    }
}
