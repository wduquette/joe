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

        var p = new Pattern.Constant(5);
        check(p.toString()).eq("$5");
    }

    @Test
    public void testWildcard() {
        test("testWildcard");

        var p = new Pattern.Wildcard("_xyz");
        check(p.toString()).eq("_xyz");
    }

    @Test
    public void testValueBinding() {
        test("testValueBinding");

        var p = new Pattern.ValueBinding(5);
        check(p.toString()).eq("?5");
    }

    @Test
    public void testPatternBinding() {
        test("testPatternBinding");

        var p = new Pattern.PatternBinding(5,
            new Pattern.Wildcard("_xyz"));
        check(p.toString()).eq("?5 = _xyz");
    }

    @Test
    public void testListPattern() {
        test("testListPattern");

        var p1 = new Pattern.ListPattern(List.of(
            new Pattern.Constant(3),
            new Pattern.Wildcard("_xyz")
        ), null);
        check(p1.toString()).eq("[$3, _xyz]");

        var p2 = new Pattern.ListPattern(List.of(
            new Pattern.Constant(3),
            new Pattern.Wildcard("_xyz")
        ), 5);
        check(p2.toString()).eq("[$3, _xyz | ?5]");
    }

    @Test
    public void testMapPattern() {
        test("testMapPattern");

        var map = new LinkedHashMap<Pattern.Constant, Pattern>();
        map.put(new Pattern.Constant(3), new Pattern.Wildcard("_xyz"));
        map.put(new Pattern.Constant(4), new Pattern.ValueBinding(7));

        var p = new Pattern.MapPattern(map);
        check(p.toString()).eq("{$3: _xyz, $4: ?7}");
    }

    @Test
    public void testInstancePattern() {
        test("testInstancePattern");

        var map = new LinkedHashMap<Pattern.Constant, Pattern>();
        map.put(new Pattern.Constant(3), new Pattern.Wildcard("_xyz"));
        map.put(new Pattern.Constant(4), new Pattern.ValueBinding(7));

        var p = new Pattern.InstancePattern("Thing", new Pattern.MapPattern(map));
        check(p.toString()).eq("Thing{$3: _xyz, $4: ?7}");
    }

    @Test
    public void testRecordPattern() {
        test("testRecordPattern");

        var map = new LinkedHashMap<Pattern.Constant, Pattern>();
        map.put(new Pattern.Constant(3), new Pattern.Wildcard("_xyz"));
        map.put(new Pattern.Constant(4), new Pattern.ValueBinding(7));

        var p = new Pattern.RecordPattern("Thing", List.of(
            new Pattern.Wildcard("_xyz"),
            new Pattern.ValueBinding(7)
        ));
        check(p.toString()).eq("Thing(_xyz, ?7)");
    }
}
