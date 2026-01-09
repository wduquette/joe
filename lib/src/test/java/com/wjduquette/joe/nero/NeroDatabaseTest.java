package com.wjduquette.joe.nero;

import com.wjduquette.joe.*;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Set;

import static com.wjduquette.joe.checker.Checker.*;

/**
 * Tests for the NewNero class.
 */
public class NeroDatabaseTest extends Ted {
    private NeroDatabase db;

    @Before
    public void setup() {
        db = new NeroDatabase();
    }

    //-------------------------------------------------------------------------
    // Creation

    // Verify that the database is empty with an empty schema.
    @Test public void testCreation() {
        test("testCreation");
        check(db.isDebug()).eq(false);
        check(db.currentSchema().isEmpty()).eq(true);
        check(db.all()).eq(Set.of());
    }

    //-------------------------------------------------------------------------
    // update()
    //
    // Note: the update(String, Schema) and update(String) flavors exercise
    // all the others.

    // Verify that we can add data to an empty database via update(String).
    // There are no schema mismatches because there's no current content.
    @Test public void testUpdate_empty() {
        test("testUpdate_empty");
        db.update("""
            define A/x;
            A(1);
            A(2);
            """);
        var content = """
            define A/x;
            A(1);
            A(2);
            """;
        check(db.toNeroScript()).eq(content);
        check(db.toNeroScript(db.all())).eq(content);
        check(db.toNeroScript(db.relation("A"))).eq(content);
    }

    // Verify that we can add data to a non-empty database via update(String)
    // provided that there are no schema mismatches with the current
    // content.
    @Test public void testUpdate_nonEmpty_good() {
        test("testUpdate_nonEmpty_good");
        db.update("""
            define A/x;
            A(1);
            A(2);
            """);
        db.update("""
            define A/x;
            define B/x;
            A(3);
            B(4);
            """);
        var content = """
            define A/x;
            A(1);
            A(2);
            A(3);
            
            define B/x;
            B(4);
            """;
        check(db.toNeroScript()).eq(content);
        check(db.toNeroScript(db.all())).eq(content);
    }

    // Verify that update(String) detects mismatches with the current
    // content.
    @Test public void testUpdate_nonEmpty_bad() {
        test("testUpdate_nonEmpty_bad");
        var inputs = """
            define A/x;
            A(1);
            """;
        db.update(inputs);
        var badScript = """
            define A/x, y;
            A(2, 3);
            """;
        checkThrow(() -> db.update(badScript))
            .containsString("Rule set is incompatible with current content, " +
                "expected 'A/x', got: 'A/x,y'.");

        // Unchanged:
        check(db.toNeroScript()).eq(inputs);
    }

    //-------------------------------------------------------------------------
    // query()

    // Verify that we can query without updating the database.
    @Test public void testQuery_ok() {
        test("testQuery_ok");
        db.update("""
            define A/x;
            A(1);
            A(2);
            """);

        var inferred = db.query("""
            define B/x;
            B(x) :- A(x);
            """);

        check(db.toNeroScript(inferred)).eq("""
            define B/x;
            B(1);
            B(2);
            """);
        check(db.toNeroScript()).eq("""
            define A/x;
            A(1);
            A(2);
            """);
    }

    // Verify that queries must be compatible with the current content.
    @Test public void testQuery_mismatch() {
        test("testQuery_mismatch");
        db.update("""
            define A/x;
            A(1);
            A(2);
            """);

        var badScript = """
            define A/x,y;
            A(3, 4);
            """;
        checkThrow(() -> db.query(badScript))
            .containsString("Rule set is incompatible with current content, " +
                "expected 'A/x', got: 'A/x,y'.");
    }

    //------------------------------------------------------------------------
    // withScript
    //
    // Note: the simple update() and query() calls use this pipeline;
    // here we're checking optional behavior.

    @Test public void testWithScript_check_ok() {
        test("testWithScript_check_ok");
        var script = """
            define A/x;
            A(1);
            A(2);
            """;
        var schema = new Schema();
        schema.add(new Shape("A", List.of("x")));
        db.withScript(script)
            .check(schema)
            .update();
        var content = """
            define A/x;
            A(1);
            A(2);
            """;
        check(db.toNeroScript()).eq(content);
    }

    @Test public void testWithScript_check_failed() {
        test("testWithScript_check_failed");
        var script = """
            define A/x;
            A(1);
            A(2);
            """;
        var schema = new Schema();
        schema.add(new Shape("A", List.of("x", "y")));
        checkThrow(() -> db.withScript(script).check(schema).update())
            .containsString("Rule set is incompatible with given schema, expected 'A/x,y', got: 'A/x'.");
    }

    //------------------------------------------------------------------------
    // addFacts()

    // Verify that we can add an arbitrary collection of facts provided
    // that they match the current content.
    @Test public void testAddFacts_factSet_ok() {
        test("testAddFacts_factSet_ok");
        db.update("""
            define A/x;
            A(1);
            A(2);
            """);
        var other = new NeroDatabase().update("""
            define B/x;
            B(1);
            """);

        // Also tests addFacts(Collection<Fact>)
        db.addFacts(new FactSet(other.all()));

        check(db.toNeroScript()).eq("""
            define A/x;
            A(1);
            A(2);
            
            define B/x;
            B(1);
            """);
    }

    // Verify that we check the schema while adding an arbitrary collection of
    // facts.
    @Test public void testAddFacts_factSet_mismatch() {
        test("testAddFacts_factSet_mismatch");
        db.update("""
            define A/x;
            A(1);
            A(2);
            """);
        var other = new NeroDatabase().update("""
            define A/x,y;
            A(3, 4);
            """);

        try {
            // Also tests addFacts(Collection<Fact>)
            db.addFacts(new FactSet(other.all()));
            fail("Expected error.");
        } catch (JoeError ex) {
            check(ex.getMessage())
                .eq("Added fact is incompatible with current content, " +
                    "expected shape 'A/x', got fact: 'Fact[A/x,y, {x=3.0, y=4.0}]'.");
        }

        check(db.toNeroScript()).eq("""
            define A/x;
            A(1);
            A(2);
            """);
    }

    // Verify that we can add the contents of another database, updating the
    // schema, provided that the new facts are compatible with the current
    // content.
    @Test public void testAddFacts_db_ok() {
        test("testAddFacts_db_ok");
        db.update("""
            define A/x;
            A(1);
            A(2);
            """);
        var other = new NeroDatabase().update("""
            define B/x;
            B(1);
            """);

        // Also tests addFacts(Collection<Fact>)
        db.addFacts(other);

        check(db.toNeroScript()).eq("""
            define A/x;
            A(1);
            A(2);
            
            define B/x;
            B(1);
            """);
    }

    // Verify that we check the schema while adding the contents of another
    // database.
    @Test public void testAddFacts_db_mismatch() {
        test("testAddFacts_db_mismatch");
        db.update("""
            define A/x;
            A(1);
            A(2);
            """);
        var other = new NeroDatabase().update("""
            define A/x,y;
            A(3, 4);
            """);

        try {
            // Also tests addFacts(Collection<Fact>)
            db.addFacts(other);
            fail("Expected error.");
        } catch (JoeError ex) {
            check(ex.getMessage())
                .eq("Added fact is incompatible with current content, " +
                    "expected shape 'A/x', got fact: 'Fact[A/x,y, {x=3.0, y=4.0}]'.");
        }

        check(db.toNeroScript()).eq("""
            define A/x;
            A(1);
            A(2);
            """);
    }

    //------------------------------------------------------------------------
    // drop()

    @Test public void testDrop() {
        test("testAddFacts_db_mismatch");
        db.update("""
            define A/x;
            define B/x;
            A(1); B(2);
            """);

        check(db.currentSchema().get("A")).eq(new Shape("A", List.of("x")));
        check(db.currentSchema().get("B")).eq(new Shape("B", List.of("x")));

        db.drop("B");
        check(db.currentSchema().get("A")).eq(new Shape("A", List.of("x")));
        check(db.currentSchema().get("B")).eq(null);
        check(db.toNeroScript()).eq("""
            define A/x;
            A(1);
            """);
    }
}
