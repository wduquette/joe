package com.wjduquette.joe.util;

import com.wjduquette.joe.Ted;
import org.junit.Before;
import org.junit.Test;


import static com.wjduquette.joe.checker.Checker.check;

public class BindingsTest extends Ted {
    private Bindings bindings;

    @Before public void setup() {
        bindings = new Bindings();
    }


    // Verify that Bindings preserves the order in which the variables were
    // bound.
    @Test public void testBindingOrder_simple() {
        test("testBindingOrder_simple()");

        bindings.bind("xyz", 1);
        bindings.bind("abc", 2);
        bindings.bind("qrs", 3);
        bindings.bind("def", 4);
        bindings.bind("lmn", 5);

        check(bindings.toString())
            .eq("Bindings[\"xyz\"=1,\"abc\"=2,\"qrs\"=3,\"def\"=4,\"lmn\"=5]");
    }

    // Verify that Binding order is preserved when adding variables from
    // another binding.
    @Test public void testBindingOrder_merge() {
        test("testBindingOrder_merge()");

        bindings.bind("xyz", 1);
        bindings.bind("abc", 2);
        bindings.bind("qrs", 3);
        bindings.bind("def", 4);
        bindings.bind("lmn", 5);

        var other = new Bindings(bindings);
        other.bind("ghi", 6);
        other.bind("tuv", 7);
        bindings.bindAll(other);

        check(bindings.toString())
            .eq("Bindings[\"xyz\"=1,\"abc\"=2,\"qrs\"=3,\"def\"=4,\"lmn\"=5,\"ghi\"=6,\"tuv\"=7]");
    }
}
