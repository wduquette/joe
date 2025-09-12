package com.wjduquette.joe.nero;

import com.wjduquette.joe.SourceBuffer;
import com.wjduquette.joe.Ted;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.wjduquette.joe.checker.Checker.check;

public class ShapeTest extends Ted {
    private final ListFact LIST_FACT =
        new ListFact("List", List.of("a", "b"));
    private final MapFact MAP_FACT =
        new MapFact("Map", Map.of("a", "x", "b", "y"));
    private final PairFact PAIR_FACT =
        new PairFact("Pair", List.of("a", "b"), List.of("x", "y"));

    @Test
    public void testInfer_fact_list() {
        test("testInfer_fact_list");
        var shape = (Shape.ListShape)Shape.inferShape(LIST_FACT);
        check(shape.relation()).eq("List");
        check(shape.arity()).eq(2);
        check(shape.toString()).eq("ListShape[relation=List, arity=2]");
        check(shape.toSpec()).eq("List/2");
    }

    @Test
    public void testInfer_fact_map() {
        test("testInfer_fact_map");
        var shape = (Shape.MapShape)Shape.inferShape(MAP_FACT);
        check(shape.relation()).eq("Map");
        check(shape.arity()).eq(-1);
        check(shape.toString()).eq("MapShape[relation=Map]");
        check(shape.toSpec()).eq("Map/...");
    }

    @Test
    public void testInfer_fact_pair() {
        test("testInfer_fact_pair");
        var shape = (Shape.PairShape)Shape.inferShape(PAIR_FACT);
        check(shape.relation()).eq("Pair");
        check(shape.arity()).eq(2);
        check(shape.toString()).eq("PairShape[relation=Pair, fieldNames=[a, b]]");
        check(shape.toSpec()).eq("Pair/a,b");
    }

    @Test
    public void testInfer_head_list() {
        test("testInfer_head_list");

        var ast = parse("List(#a, #b);");
        var axioms = new ArrayList<>(ast.axioms());
        var shape = (Shape.ListShape)Shape.inferDefaultShape(axioms.getFirst());

        check(shape.relation()).eq("List");
        check(shape.arity()).eq(2);
        check(shape.toString()).eq("ListShape[relation=List, arity=2]");
    }

    @Test
    public void testInfer_head_map() {
        test("testInfer_head_map");

        var ast = parse("Map(x: #a, y: #b);");
        var axioms = new ArrayList<>(ast.axioms());
        var shape = (Shape.MapShape)Shape.inferDefaultShape(axioms.getFirst());

        check(shape.relation()).eq("Map");
        check(shape.toString()).eq("MapShape[relation=Map]");
    }

    private NeroRuleSet parse(String text) {
        var source = new SourceBuffer("*test*", text);
        return Nero.parse(source);
    }
}
