package com.wjduquette.joe.nero;

import com.wjduquette.joe.SourceBuffer;
import com.wjduquette.joe.Ted;
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
    // check(atom)

    @Test public void testCheck_head_undefined() {
        test("testCheck_head_undefined");

        var head = new OrderedAtom("Person", List.of(new Constant("abc")));
        check(schema.check(head)).eq(false);
    }

    @Test public void testCheck_head_mismatch() {
        test("testCheck_head_mismatch");

        var shape = new Shape("Person", List.of("id", "name"));
        var head = new OrderedAtom("Person", List.of(new Constant("abc")));
        schema.add(shape);
        check(schema.check(head)).eq(false);
    }

    //-------------------------------------------------------------------------
    // inferSchema

    @Test public void testInfer_schema_ok() {
        test("testInfer_schema_ok");
        List<Fact> facts = List.of(
            new Fact("Person", List.of("x", "y"), List.of("a", "b")),
            new Fact("Place", List.of("x"), List.of("c")),
            new Fact("Thing", List.of("x", "y", "z"), List.of("d", "e", "f"))
        );

        schema = Schema.inferSchema(facts);

        check(schema.getRelations()).eq(Set.of("Person", "Place", "Thing"));
        check(schema.get("Person")).eq(new Shape("Person", List.of("x", "y")));
        check(schema.get("Place")).eq(new Shape("Place", List.of("x")));
        check(schema.get("Thing")).eq(new Shape("Thing", List.of("x", "y", "z")));
    }

    //-------------------------------------------------------------------------
    // merge()

    @Test
    public void testMerge_good() {
        var pairX2a = new Shape("X", List.of("a", "b"));
        var pairX2b = new Shape("X", List.of("a", "b"));
        var mapZ = new Shape("Z");

        var s1 = new Schema();
        s1.checkAndAdd(pairX2a);

        var s2 = new Schema();
        s2.checkAndAdd(pairX2b);
        s2.checkAndAdd(mapZ);

        s1.merge(s2);
        check(s1.get("X")).eq(pairX2a);
        check(s1.get("Z")).eq(mapZ);  // Retained from s2.
    }

    @Test
    public void testMerge_bad() {
        var pairX = new Shape("X", List.of("a"));
        var mapX = new Shape("X");

        var s1 = new Schema();
        s1.checkAndAdd(pairX);

        var s2 = new Schema();
        s2.checkAndAdd(mapX);

        // NOTE: This test presumes that promote() is working properly.
        checkThrow(() -> s1.merge(s2))
            .containsString("Schema mismatch for 'X', expected shape compatible with 'X/a', got: 'X/...'.");
        check(s1.get("X")).eq(pairX);  // Unchanged
    }

    @Test
    public void testToStaticSchema() {
        var schema = new Schema();
        schema.add(new Shape("X"));
        schema.add(new Shape("Y"));
        schema.setTransient("Y", true);
        schema.add(new Shape("X!"));

        var schema2 = schema.toStaticSchema();
        check(schema2.getTransients().isEmpty()).eq(true);
        check(schema2.getRelations()).eq(Set.of("X"));
    }

    @Test
    public void testIsCompatible() {
        var s1 = new Schema();
        s1.add(new Shape("X", List.of("x")));
        s1.add(new Shape("Y", List.of("x", "y")));

        var s2 = new Schema();
        s2.add(new Shape("X", List.of("x")));
        s2.add(new Shape("Z", List.of("z")));

        var s3 = new Schema();
        s3.add(new Shape("X", List.of("x")));
        s3.add(new Shape("Y", List.of("y")));

        check(s1.isCompatible(s1)).eq(true);
        check(s1.isCompatible(s2)).eq(true);
        check(s1.isCompatible(s3)).eq(false);
        check(s2.isCompatible(s3)).eq(true);
    }

    //-------------------------------------------------------------------------
    // Helpers

    @SuppressWarnings("unused")
    private NeroRuleSet parse(String text) {
        var source = new SourceBuffer("*test*", text);
        return Nero.parse(source);
    }
}
