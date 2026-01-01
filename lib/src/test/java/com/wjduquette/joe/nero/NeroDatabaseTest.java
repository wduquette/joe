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
        check(db.schema().isEmpty()).eq(true);
        check(db.all()).eq(Set.of());
    }

    //-------------------------------------------------------------------------
    // update()

    // Verify that we can add data to an empty database via update()
    @Test public void testUpdate_empty() {
        test("testUpdate_empty");
        db.update("""
            define A/x;
            A(1);
            A(2);
            """);
        check(db.schema().get("A")).eq(new Shape("A", List.of("x")));
        check(db.schema()).eq(Schema.inferSchema(db.all()));

        var content = """
            define A/x;
            A(1);
            A(2);
            """;
        check(db.toNeroScript()).eq(content);
        check(db.toNeroScript(db.all())).eq(content);
        check(db.toNeroScript(db.relation("A"))).eq(content);
    }

    // Verify that we can add data to an empty database via update(), and
    // the schema updates.
    @Test public void testUpdate_nonEmpty() {
        test("testUpdate_nonEmpty");
        db.update("""
            define A/x;
            A(1);
            A(2);
            """);
        db.update("""
            define A/x;
            define B/x;
            A(3);
            B(1);
            """);
        check(db.schema().get("A")).eq(new Shape("A", List.of("x")));
        check(db.schema().get("B")).eq(new Shape("B", List.of("x")));
        check(db.schema()).eq(Schema.inferSchema(db.all()));

        check(db.toNeroScript()).eq("""
            define A/x;
            A(1);
            A(2);
            A(3);
            
            define B/x;
            B(1);
            """);
    }

    // Verify that update() catches schema mismatches
    @Test public void testUpdate_mismatch() {
        test("testUpdate_mismatch");
        db.update("""
            define A/x;
            A(1);
            A(2);
            """);

        try {
            db.update("""
                define A/x,y;
                A(3, 4);
                """);
            fail("Expected error.");
        } catch (JoeError ex) {
            check(ex.getTraces().getFirst().message())
                .eq("error at 'A', definition clashes with earlier entry.");
        }

        // Verify that the database content is unchanged.
        check(db.toNeroScript()).eq("""
            define A/x;
            A(1);
            A(2);
            """);
    }

    // Verify that we can update given a NeroRuleSet, and the schema updates
    // properly.
    @Test public void testUpdate_NeroRuleSet() {
        test("testUpdate_nonEmpty");
        var r1 = Nero.compile("""
            define A/x;
            A(1);
            A(2);
            """);
        var r2 = Nero.compile("""
            define A/x;
            define B/x;
            A(3);
            B(1);
            """);
        db.update(r1);
        db.update(r2);
        check(db.schema()).eq(Schema.inferSchema(db.all()));

        check(db.toNeroScript()).eq("""
            define A/x;
            A(1);
            A(2);
            A(3);
            
            define B/x;
            B(1);
            """);
    }

    // Verify that we can update given a NeroRuleSet, and the schema updates
    // properly.
    @Test public void testUpdate_NeroRuleSet2() {
        test("testUpdate_nonEmpty");
        var r1 = Nero.compile("""
            define A/x;
            A(1);
            A(2);
            """);
        var r2 = Nero.compile("""
            define A/x,y;
            define B/x;
            A(3, 4);
            B(1);
            """);
        db.update(r1);

        try {
            db.update(r2);
            fail("Expected error.");
        } catch (JoeError ex) {
            check(ex.getMessage())
                .eq("Shape mismatch for fact: 'PairFact[relation=A, fieldNames=[x, y], fields=[3.0, 4.0]]'.");
        }
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
        check(db.schema().get("B")).eq(null);
    }

    // Verify that queries must be compatible with the schema.
    @Test public void testQuery_mismatch() {
        test("testQuery_mismatch");
        db.update("""
            define A/x;
            A(1);
            A(2);
            """);

        try {
            db.query("""
                define A/x,y;
                A(3, 4);
                """);
            fail("Expected error.");
        } catch (JoeError ex) {
            check(ex.getTraces().getFirst().message())
                .eq("error at 'A', definition clashes with earlier entry.");
        }

        // Verify that the database content is unchanged.
        check(db.toNeroScript()).eq("""
            define A/x;
            A(1);
            A(2);
            """);
    }

    //------------------------------------------------------------------------
    // addFacts()

    // Verify that we can add an arbitrary collection of facts, updating the
    // schema.
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
        check(db.schema().get("B")).eq(new Shape("B", List.of("x")));
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
                .eq("Schema mismatch for 'A', expected shape compatible with 'A/x', got: 'A/x,y'.");
        }

        check(db.toNeroScript()).eq("""
            define A/x;
            A(1);
            A(2);
            """);
    }

    // Verify that we can add the contents of another database, updating the
    // schema.
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
        check(db.schema().get("B")).eq(new Shape("B", List.of("x")));
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
                .eq("Schema mismatch for 'A', expected shape compatible with 'A/x', got: 'A/x,y'.");
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

        check(db.schema().get("A")).eq(new Shape("A", List.of("x")));
        check(db.schema().get("B")).eq(new Shape("B", List.of("x")));

        db.drop("B");
        check(db.schema().get("A")).eq(new Shape("A", List.of("x")));
        check(db.schema().get("B")).eq(null);
        check(db.toNeroScript()).eq("""
            define A/x;
            A(1);
            """);
    }
}
