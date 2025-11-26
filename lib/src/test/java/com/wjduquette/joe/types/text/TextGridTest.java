package com.wjduquette.joe.types.text;

import com.wjduquette.joe.Ted;
import org.junit.Before;
import org.junit.Test;

import static com.wjduquette.joe.checker.Checker.check;

public class TextGridTest extends Ted {
    private TextGrid grid;

    @Before public void setup() {
        grid = new TextGrid();
    }

    @Test
    public void testEmptyGrid() {
        test("testEmptyGrid()");
        check(grid.toString()).eq("");
    }

    @Test
    public void testNoGaps() {
        test("testNoGaps()");
        grid.put(0, 0, """
            123
            12
            1
            """);
        grid.put(1, 0, """
            abc
            ab
            a
            """);
        grid.put(0, 1, """
            1
            12
            123
            """);
        grid.put(1, 1, """
            a
            ab
            abc
            """);

        println("-------");
        println(grid.toString());
        println("-------");
        check(grid.toString()).eq(
            "123abc\n" +
            "12 ab \n" +
            "1  a  \n" +
            "1  a  \n" +
            "12 ab \n" +
            "123abc"
        );
    }

    @Test
    public void testColumnGap() {
        test("testColumnGap()");
        grid.setColumnGap(3);
        grid.put(0, 0, """
            123
            12
            1
            """);
        grid.put(1, 0, """
            abc
            ab
            a
            """);
        grid.put(0, 1, """
            1
            12
            123
            """);
        grid.put(1, 1, """
            a
            ab
            abc
            """);

        println("-------");
        println(grid.toString());
        println("-------");
        check(grid.toString()).eq(
            "123   abc\n" +
            "12    ab \n" +
            "1     a  \n" +
            "1     a  \n" +
            "12    ab \n" +
            "123   abc"
        );
    }

    @Test
    public void testRowGap() {
        test("testRowGap()");
        grid.setRowGap(1);
        grid.put(0, 0, """
            123
            12
            1
            """);
        grid.put(1, 0, """
            abc
            ab
            a
            """);
        grid.put(0, 1, """
            1
            12
            123
            """);
        grid.put(1, 1, """
            a
            ab
            abc
            """);

        println("-------");
        println(grid.toString());
        println("-------");
        check(grid.toString()).eq(
            "123abc\n" +
            "12 ab \n" +
            "1  a  \n" +
            "      \n" +
            "1  a  \n" +
            "12 ab \n" +
            "123abc"
        );
    }
}
