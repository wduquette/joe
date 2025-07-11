package com.wjduquette.joe.patterns;

import com.wjduquette.joe.Ted;
import org.junit.Before;
import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.List;

import static com.wjduquette.joe.checker.Checker.check;

public class PatternTest extends Ted {
    @Before public void setup() {
    }

    @Test
    public void testConstant() {
        test("testConstant");

        var p = new Pattern.Expression(5);
        check(p.toString()).eq("$5");
    }

    @Test
    public void testListPattern() {
        test("testListPattern");

        var p1 = new Pattern.ListPattern(List.of(
            new Pattern.Expression(3),
            new Pattern.Wildcard("_xyz")
        ), null);
        check(p1.toString()).eq("[$3, _xyz]");

        var p2 = new Pattern.ListPattern(List.of(
            new Pattern.Expression(3),
            new Pattern.Wildcard("_xyz")
        ), "tail");
        check(p2.toString()).eq("[$3, _xyz | ?tail]");
    }

    @Test
    public void testMapPattern() {
        test("testMapPattern");

        var map = new LinkedHashMap<Pattern.Expression, Pattern>();
        map.put(new Pattern.Expression(3), new Pattern.Wildcard("_xyz"));
        map.put(new Pattern.Expression(4), new Pattern.ValueBinding("a"));

        var p = new Pattern.MapPattern(map);
        check(p.toString()).eq("{$3: _xyz, $4: ?a}");
    }

    @Test
    public void testNamedFieldPattern() {
        test("testNamedFieldPattern");

        var map = new LinkedHashMap<String, Pattern>();
        map.put("id", new Pattern.Wildcard("_xyz"));
        map.put("color", new Pattern.ValueBinding("a"));

        var p = new Pattern.NamedFieldPattern("Thing", map);
        check(p.toString()).eq("Thing(id: _xyz, color: ?a)");
    }

    @Test
    public void testOrderedFieldPattern_normal() {
        test("testOrderedFieldPattern");

        var p = new Pattern.OrderedFieldPattern("Thing", List.of(
            new Pattern.Wildcard("_xyz"),
            new Pattern.ValueBinding("a")
        ));
        check(p.toString()).eq("Thing(_xyz, ?a)");
    }

    @Test
    public void testPatternBinding() {
        test("testPatternBinding");

        var p = new Pattern.PatternBinding("a",
            new Pattern.Wildcard("_xyz"));
        check(p.toString()).eq("?a@_xyz");
    }

    @Test
    public void testTypeName() {
        test("testTypeName");

        var p = new Pattern.TypeName("Thing");
        check(p.toString()).eq("Thing()");
    }

    @Test
    public void testValueBinding() {
        test("testValueBinding");

        var p = new Pattern.ValueBinding("a");
        check(p.toString()).eq("?a");
    }

    @Test
    public void testWildcard() {
        test("testWildcard");

        var p = new Pattern.Wildcard("_xyz");
        check(p.toString()).eq("_xyz");
    }

}
