package com.wjduquette.joe;


import com.wjduquette.joe.types.ListValue;
import com.wjduquette.joe.types.MapValue;
import com.wjduquette.joe.types.SetValue;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Ted, the Test Execution Deputy.
 * This is a base class for test classes.
 */
public class Ted {
    public void test(String name) {
        println("------------------------------");
        println("Test: " + name);
    }

    public void println(String text) {
        System.out.println(text);
    }

    // List.of doesn't allow nulls
    public List<Object> listOf(Object... values) {
        var result = new ListValue();
        result.addAll(Arrays.asList(values));
        return result;
    }

    // Set.of doesn't allow nulls
    public Set<Object> setOf(Object... values) {
        var result = new SetValue();
        result.addAll(Arrays.asList(values));
        return result;
    }

    // Map.of doesn't allow nulls
    public Map<Object, Object> mapOf(Object... values) {
        var result = new MapValue();
        for (var i = 0; i < values.length; i += 2) {
            result.put(values[i], values[i + 1]);
        }
        return result;
    }
}
