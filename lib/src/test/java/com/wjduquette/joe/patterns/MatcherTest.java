package com.wjduquette.joe.patterns;

import com.wjduquette.joe.Ted;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.wjduquette.joe.checker.Checker.check;

public class MatcherTest extends Ted {
    private List<Object> constants;
    private final Map<Integer,Object> bindings = new LinkedHashMap<>();

    @Before public void setup() {
        constants = new ArrayList<>();
        bindings.clear();
    }

    private boolean bind(Pattern pattern, Object value) {
        return Matcher.bind(pattern, value, constants::get, bindings::put);
    }

    @Test
    public void testConstant_bad() {
        test("testConstant_bad");

        constants = List.of("abc");
        var pattern = new Pattern.Constant(0);
        var value = "xyz";

        check(bind(pattern, value)).eq(false);
    }

    @Test
    public void testConstant_good() {
        test("testConstant_good");

        constants = List.of("abc");
        var pattern = new Pattern.Constant(0);
        var value = "abc";

        check(bind(pattern, value)).eq(true);
        check(bindings.isEmpty()).eq(true);
    }

    @Test
    public void testWildcard() {
        test("testWildcard");

        var pattern = new Pattern.Wildcard("_");
        var value = "abc";

        check(bind(pattern, value)).eq(true);
        check(bindings.isEmpty()).eq(true);
    }

    @Test
    public void testValueBinding() {
        test("testValueBinding");

        var pattern = new Pattern.ValueBinding(0);
        var value = "abc";

        check(bind(pattern, value)).eq(true);
        check(bindings.get(0)).eq("abc");
    }

    @Test
    public void testPatternBinding_bad() {
        test("testPatternBinding_bad");

        constants = List.of("abc");
        var pattern = new Pattern.PatternBinding(0,
            new Pattern.Constant(0));
        var value = "xyz";

        check(bind(pattern, value)).eq(false);
    }

    @Test
    public void testPatternBinding_good() {
        test("testPatternBinding_good");

        constants = List.of("abc");
        var pattern = new Pattern.PatternBinding(0,
            new Pattern.Constant(0));
        var value = "abc";

        check(bind(pattern, value)).eq(true);
        check(bindings.get(0)).eq("abc");
    }

    @Test
    public void testListPattern_notList() {
        test("testListPattern_notList");

        constants = List.of("abc");
        var pattern = new Pattern.ListPattern(List.of(
            new Pattern.Constant(0)
        ), null);
        var value = "abc";

        check(bind(pattern, value)).eq(false);
    }

    @Test
    public void testListPattern_tooLong() {
        test("testListPattern_tooLong");

        constants = List.of("abc", "def", "ghi");
        var pattern = new Pattern.ListPattern(List.of(
            new Pattern.Constant(0),
            new Pattern.Constant(1),
            new Pattern.Constant(2)
        ), null);
        var value = List.of("abc", "def");

        check(bind(pattern, value)).eq(false);
    }

    @Test
    public void testListPattern_tooShort() {
        test("testListPattern_tooShort");

        constants = List.of("abc");
        var pattern = new Pattern.ListPattern(List.of(
            new Pattern.Constant(0)
        ), null);
        var value = List.of("abc", "def");

        check(bind(pattern, value)).eq(false);
    }

    @Test
    public void testListPattern_badItem() {
        test("testListPattern_badItem");

        constants = List.of("abc", "def");
        var pattern = new Pattern.ListPattern(List.of(
            new Pattern.Constant(0),
            new Pattern.Constant(1)
        ), null);
        var value = List.of("abc", "xyz");

        check(bind(pattern, value)).eq(false);
    }

    @Test
    public void testListPattern_good_emptyList() {
        test("testListPattern_good_emptyList");

        var pattern = new Pattern.ListPattern(List.of(
        ), null);
        var value = List.of();

        check(bind(pattern, value)).eq(true);
    }

    @Test
    public void testListPattern_good_noTail() {
        test("testListPattern_good_noTail");

        constants = List.of("abc", "def");
        var pattern = new Pattern.ListPattern(List.of(
            new Pattern.Constant(0),
            new Pattern.Constant(1)
        ), null);
        var value = List.of("abc", "def");

        check(bind(pattern, value)).eq(true);
    }

    @Test
    public void testListPattern_good_tailEmpty() {
        test("testListPattern_good_tailEmpty");

        constants = List.of("abc", "def");
        var pattern = new Pattern.ListPattern(List.of(
            new Pattern.Constant(0),
            new Pattern.Constant(1)
        ), 0);
        var value = List.of("abc", "def");

        check(bind(pattern, value)).eq(true);
        check(bindings.get(0)).eq(List.of());
    }

    @Test
    public void testListPattern_good_tailNotEmpty() {
        test("testListPattern_good_tailNotEmpty");

        constants = List.of("abc", "def");
        var pattern = new Pattern.ListPattern(List.of(
            new Pattern.Constant(0),
            new Pattern.Constant(1)
        ), 0);
        var value = List.of("abc", "def", "ghi", "jkl");

        check(bind(pattern, value)).eq(true);
        check(bindings.get(0)).eq(List.of("ghi", "jkl"));
    }

    @Test
    public void testMapPattern_bad_noMap() {
        test("testMapPattern_bad_missingKey");

        constants = List.of("k1", "v1", "k2", "v2");
        var pattern = new Pattern.MapPattern(Map.of(
            new Pattern.Constant(0),
            new Pattern.Constant(1),
            new Pattern.Constant(2),
            new Pattern.Constant(3)
        ));
        var value = "abc";

        check(bind(pattern, value)).eq(false);
    }

    @Test
    public void testMapPattern_bad_missingKey() {
        test("testMapPattern_bad_missingKey");

        constants = List.of("k1", "v1", "k2", "v2");
        var pattern = new Pattern.MapPattern(Map.of(
            new Pattern.Constant(0),
            new Pattern.Constant(1),
            new Pattern.Constant(2),
            new Pattern.Constant(3)
        ));
        var value = Map.of("k1", "v1");

        check(bind(pattern, value)).eq(false);
    }

    @Test
    public void testMapPattern_bad_wrongValue() {
        test("testMapPattern_bad_wrongValue");

        constants = List.of("k1", "v1", "k2", "v2");
        var pattern = new Pattern.MapPattern(Map.of(
            new Pattern.Constant(0),
            new Pattern.Constant(1),
            new Pattern.Constant(2),
            new Pattern.Constant(3)
        ));
        var value = Map.of("k1", "v1", "k2", "nonesuch");

        check(bind(pattern, value)).eq(false);
    }

    @Test
    public void testMapPattern_good_emptyMap() {
        test("testMapPattern_good_emptyMap");

        var pattern = new Pattern.MapPattern(Map.of(
        ));
        var value = Map.of();

        check(bind(pattern, value)).eq(true);
    }

    @Test
    public void testMapPattern_good_noExtraKey() {
        test("testMapPattern_good_noExtraKey");

        constants = List.of("k1", "v1", "k2", "v2");
        var pattern = new Pattern.MapPattern(Map.of(
            new Pattern.Constant(0),
            new Pattern.Constant(1),
            new Pattern.Constant(2),
            new Pattern.Constant(3)
        ));
        var value = Map.of("k1", "v1", "k2", "v2");

        check(bind(pattern, value)).eq(true);
    }

    @Test
    public void testMapPattern_good_extraKey() {
        test("testMapPattern_good_extraKey");

        constants = List.of("k1", "v1", "k2", "v2");
        var pattern = new Pattern.MapPattern(Map.of(
            new Pattern.Constant(0),
            new Pattern.Constant(1),
            new Pattern.Constant(2),
            new Pattern.Constant(3)
        ));
        var value = Map.of("k1", "v1", "k2", "v2", "k3", "v3");

        check(bind(pattern, value)).eq(true);
    }
}
