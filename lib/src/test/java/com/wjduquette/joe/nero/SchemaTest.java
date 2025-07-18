package com.wjduquette.joe.nero;

import com.wjduquette.joe.SourceBuffer;
import com.wjduquette.joe.parser.ASTRuleSet;
import org.junit.Before;
import org.junit.Test;

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
        var shape = Shape.infer(fact);
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

    // TODO: Add checkAndAdd(ASTAtom) tests once `RuleSet` includes a schema.

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
    private ASTRuleSet parse(String text) {
        var source = new SourceBuffer("*test*", text);
        return new Nero().parse(source);
    }
}
