package com.wjduquette.joe.nero;

import com.wjduquette.joe.Ted;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static com.wjduquette.joe.checker.Checker.check;

public class ShapeTest extends Ted {
    private final MapFact MAP_FACT =
        new MapFact("Map", Map.of("a", "x", "b", "y"));
    private final PairFact PAIR_FACT =
        new PairFact("Pair", List.of("a", "b"), List.of("x", "y"));

    @Test
    public void testInfer_unordered() {
        test("testInfer_unordered");
        var shape = Shape.inferShape(MAP_FACT);
        check(shape.relation()).eq("Map");
        check(shape.arity()).eq(0);
        check(shape.toString()).eq("Map/...");
        check(shape.toSpec()).eq("Map/...");
    }

    @Test
    public void testInfer_ordered() {
        test("testInfer_ordered");
        var shape = Shape.inferShape(PAIR_FACT);
        check(shape.relation()).eq("Pair");
        check(shape.arity()).eq(2);
        check(shape.toString()).eq("Pair/a,b");
        check(shape.toSpec()).eq("Pair/a,b");
    }
}
