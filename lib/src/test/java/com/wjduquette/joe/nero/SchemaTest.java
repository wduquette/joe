package com.wjduquette.joe.nero;

import com.wjduquette.joe.Joe;
import com.wjduquette.joe.SourceBuffer;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.wjduquette.joe.checker.Checker.check;
import static com.wjduquette.joe.checker.Checker.checkThrow;

/**
 * Tests the Schema class.
 */
public class SchemaTest extends Ted {
    private Schema schema;

    @Before
    public void setup() {
        schema = new Schema();
    }

    @Test
    public void testEmpty() {
        test("testEmpty");
        check(schema.getRelations().isEmpty()).eq(true);
        check(schema.hasRelation("Person")).eq(false);
        check(schema.get("Person")).eq(null);
    }

    //-------------------------------------------------------------------------
    // Transience

    @Test public void testTransience() {
        test("testTransience");

        check(schema.isTransient("Foo")).eq(false);
        schema.setTransient("Foo", true);
        check(schema.isTransient("Foo")).eq(true);
        schema.setTransient("Foo", false);
        check(schema.isTransient("Foo")).eq(false);
    }


    //-------------------------------------------------------------------------
    // checkAndAdd(shape)

    // A shape is always accepted if the relation isn't previously known.
    @Test public void testCheckAndAdd_shape_new() {
        test("testCheckAndAdd_shape_new");

        var shape = new Shape.ListShape("Person", 2);
        check(schema.checkAndAdd(shape)).eq(true);

        check(schema.getRelations()).eq(Set.of("Person"));
        check(schema.hasRelation("Person")).eq(true);
        check(schema.get("Person")).eq(shape);
    }

    // A shape is accepted if it matches the defined shape for the relation
    @Test public void testCheckAndAdd_shape_match() {
        test("testCheckAndAdd_shape_match");

        var shape = new Shape.ListShape("Person", 2);
        check(schema.checkAndAdd(shape)).eq(true);
        check(schema.checkAndAdd(shape)).eq(true);

        check(schema.getRelations()).eq(Set.of("Person"));
        check(schema.hasRelation("Person")).eq(true);
        check(schema.get("Person")).eq(shape);
    }

    // A shape is rejected if it doesn't match the defined shape for the
    // relation.
    @Test public void testCheckAndAdd_shape_reject() {
        test("testCheckAndAdd_shape_reject");

        var shape = new Shape.ListShape("Person", 2);
        check(schema.checkAndAdd(shape)).eq(true);

        var shape2 = new Shape.MapShape("Person");
        check(schema.checkAndAdd(shape2)).eq(false);

        // Previous definition is unchanged.
        check(schema.getRelations()).eq(Set.of("Person"));
        check(schema.hasRelation("Person")).eq(true);
        check(schema.get("Person")).eq(shape);
    }

    //-------------------------------------------------------------------------
    // checkAndAdd(Fact)

    // A fact's shape is always accepted if the relation isn't previously known.
    @Test public void testCheckAndAdd_fact_new() {
        test("testCheckAndAdd_fact_new");

        var fact = new ListFact("Person", List.of("Joe", 90));
        var shape = Shape.inferShape(fact);
        check(schema.checkAndAdd(fact)).eq(true);

        check(schema.getRelations()).eq(Set.of("Person"));
        check(schema.hasRelation("Person")).eq(true);
        check(schema.get("Person")).eq(shape);
    }

    // A fact's shape is accepted if it matches the defined shape for the
    // relation
    @Test public void testCheckAndAdd_fact_match() {
        test("testCheckAndAdd_fact_match");

        var shape = new Shape.ListShape("Person", 2);
        check(schema.checkAndAdd(shape)).eq(true);

        var fact = new ListFact("Person", List.of("Joe", 90));
        check(schema.checkAndAdd(fact)).eq(true);

        check(schema.getRelations()).eq(Set.of("Person"));
        check(schema.hasRelation("Person")).eq(true);
        check(schema.get("Person")).eq(shape);
    }

    // A fact's shape is rejected if it doesn't match the defined shape for
    // the relation.
    @Test public void testCheckAndAdd_fact_reject() {
        test("testCheckAndAdd_fact_reject");

        var shape = new Shape.MapShape("Person");
        check(schema.checkAndAdd(shape)).eq(true);

        var fact = new ListFact("Person", List.of("Joe", 90));
        check(schema.checkAndAdd(fact)).eq(false);

        // Previous definition is unchanged.
        check(schema.getRelations()).eq(Set.of("Person"));
        check(schema.hasRelation("Person")).eq(true);
        check(schema.get("Person")).eq(shape);
    }

    //-------------------------------------------------------------------------
    // checkAndAdd(ASTATom)

    // A head atom's shape is always accepted if the relation isn't previously
    // known.
    @Test public void testCheckAndAdd_head_new() {
        test("testCheckAndAdd_head_new");

        var ast = parse("Person(#a, #b);");
        var head = new ArrayList<>(ast.axioms()).getFirst();
        var shape = Shape.inferDefaultShape(head);
        check(schema.checkAndAdd(head)).eq(true);

        check(schema.getRelations()).eq(Set.of("Person"));
        check(schema.hasRelation("Person")).eq(true);
        check(schema.get("Person")).eq(shape);
    }

    // For a known list shape, a head atom must be ordered with the correct
    // arity.
    @Test public void testCheckAndAdd_head_list_ok() {
        test("testCheckAndAdd_head_list_ok");

        var shape = new Shape.ListShape("Person", 2);
        schema.checkAndAdd(shape);

        var ast = parse("Person(#a, #b);");
        var head = new ArrayList<>(ast.axioms()).getFirst();
        check(schema.checkAndAdd(head)).eq(true);

        check(schema.getRelations()).eq(Set.of("Person"));
        check(schema.hasRelation("Person")).eq(true);
        check(schema.get("Person")).eq(shape);
    }

    // For a known list shape, a head atom must be ordered with the correct
    // arity.
    @Test public void testCheckAndAdd_head_list_mismatch() {
        test("testCheckAndAdd_head_list_mismatch");

        var shape = new Shape.ListShape("Person", 2);
        schema.checkAndAdd(shape);

        var ast = parse("Person(#a, #b, #c);");
        var head = new ArrayList<>(ast.axioms()).getFirst();
        check(schema.checkAndAdd(head)).eq(false);

        check(schema.getRelations()).eq(Set.of("Person"));
        check(schema.hasRelation("Person")).eq(true);
        check(schema.get("Person")).eq(shape);
    }

    // For a known map shape, a head atom must be named.
    @Test public void testCheckAndAdd_head_map_ok() {
        test("testCheckAndAdd_head_map_ok");

        var shape = new Shape.MapShape("Thing");
        schema.checkAndAdd(shape);

        var ast = parse("Thing(a: #a, b: #b);");
        var head = new ArrayList<>(ast.axioms()).getFirst();
        check(schema.checkAndAdd(head)).eq(true);

        check(schema.getRelations()).eq(Set.of("Thing"));
        check(schema.hasRelation("Thing")).eq(true);
        check(schema.get("Thing")).eq(shape);
    }

    // For a known map shape, a head atom must be named.
    @Test public void testCheckAndAdd_head_map_mismatch() {
        test("testCheckAndAdd_head_map_mismatch");

        var shape = new Shape.MapShape("Thing");
        schema.checkAndAdd(shape);

        var ast = parse("Thing(#a, #b);");
        var head = new ArrayList<>(ast.axioms()).getFirst();
        check(schema.checkAndAdd(head)).eq(false);

        check(schema.getRelations()).eq(Set.of("Thing"));
        check(schema.hasRelation("Thing")).eq(true);
        check(schema.get("Thing")).eq(shape);
    }

    // For a known pair shape, a head atom must be ordered with the correct
    // arity.
    @Test public void testCheckAndAdd_head_pair_ok() {
        test("testCheckAndAdd_head_pair_ok");

        var shape = new Shape.PairShape("Person", List.of("a", "b"));
        schema.checkAndAdd(shape);

        var ast = parse("Person(#a, #b);");
        var head = new ArrayList<>(ast.axioms()).getFirst();
        check(schema.checkAndAdd(head)).eq(true);

        check(schema.getRelations()).eq(Set.of("Person"));
        check(schema.hasRelation("Person")).eq(true);
        check(schema.get("Person")).eq(shape);
    }

    // For a known list shape, a head atom must be ordered with the correct
    // arity.
    @Test public void testCheckAndAdd_head_pair_mismatch() {
        test("testCheckAndAdd_head_pair_mismatch");

        var shape = new Shape.PairShape("Person", List.of("a", "b"));
        schema.checkAndAdd(shape);

        var ast = parse("Person(#a, #b, #c);");
        var head = new ArrayList<>(ast.axioms()).getFirst();
        check(schema.checkAndAdd(head)).eq(false);

        check(schema.getRelations()).eq(Set.of("Person"));
        check(schema.hasRelation("Person")).eq(true);
        check(schema.get("Person")).eq(shape);
    }

    //-------------------------------------------------------------------------
    // inferSchema

    @Test public void testInfer_schema_ok() {
        test("testInfer_schema_ok");
        List<Fact> facts = List.of(
            new ListFact("Person", List.of("a", "b")),
            new ListFact("Place", List.of("c")),
            new ListFact("Thing", List.of("d", "e", "f"))
        );

        schema = Schema.inferSchema(facts);

        check(schema.getRelations()).eq(Set.of("Person", "Place", "Thing"));
        check(schema.get("Person")).eq(new Shape.ListShape("Person", 2));
        check(schema.get("Place")).eq(new Shape.ListShape("Place", 1));
        check(schema.get("Thing")).eq(new Shape.ListShape("Thing", 3));
    }

    @Test public void testInfer_schema_mismatch() {
        test("testInfer_schema_mismatch");
        List<Fact> facts = List.of(
            new ListFact("Person", List.of("a", "b")),
            new ListFact("Place", List.of("c")),
            new ListFact("Person", List.of("d", "e", "f"))
        );

        checkThrow(() -> Schema.inferSchema(facts))
            .containsString("Shape mismatch for fact:");
    }

    //-------------------------------------------------------------------------
    // Helpers

    @SuppressWarnings("unused")
    private NeroRuleSet parse(String text) {
        var source = new SourceBuffer("*test*", text);
        return new Nero(new Joe()).parse(source);
    }
}
