package com.wjduquette.joe.nero;

import org.junit.Test;

import static com.wjduquette.joe.checker.Checker.*;

public class TermTest extends Ted {
    private final Constant ONE = new Constant(1);
    private final Constant ALSO_ONE = new Constant(1);
    private final Constant TWO = new Constant(2);
    private final Constant ABC = new Constant("abc");
    private final Variable X = new Variable("x");
    private final Variable ALSO_X = new Variable("x");
    private final Variable Y = new Variable("y");

    @Test
    public void testConstant() {
        check(ONE.value()).eq(1);
        check(ONE.toString()).eq("1");

        check(ABC.value()).eq("abc");
        check(ABC.toString()).eq("\"abc\"");
    }

    @Test
    public void testConstant_equals() {
        check(ONE).eq(ONE);
        check(ONE).eq(ALSO_ONE);
        check(ONE).ne(TWO);
        check(ONE).ne(ABC);
    }

    @Test
    public void testVariable() {
        check(X.name()).eq("x");
        check(X.toString()).eq("x");
    }

    @Test
    public void testVariable_equals() {
        check(X).eq(X);
        check(X).eq(ALSO_X);
        check(X).ne(Y);
    }
}
