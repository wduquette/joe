package com.wjduquette.joe.nero;


/**
 * Ted, the Test Execution Deputy.
 * This is a base class for test classes.
 */
public class Ted {
    //-------------------------------------------------------------------------
    // Package-specific



    //-------------------------------------------------------------------------
    // Output

    public void test(String name) {
        println("------------------------------");
        println("Test: " + name);
    }

    public void println(String text) {
        System.out.println(text);
    }
}
