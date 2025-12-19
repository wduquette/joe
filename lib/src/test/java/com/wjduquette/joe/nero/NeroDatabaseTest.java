package com.wjduquette.joe.nero;

import com.wjduquette.joe.*;
import org.junit.Before;
import org.junit.Test;

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
        db.update("A(1); A(2);");
        check(db.schema().get("A")).eq(new Shape.ListShape("A", 1));

        var content = """
            define A/1;
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
        db.update("A(1); A(2);");
        db.update("A(3); B(1);");
        check(db.schema().get("A")).eq(new Shape.ListShape("A", 1));
        check(db.schema().get("B")).eq(new Shape.ListShape("B", 1));

        check(db.toNeroScript()).eq("""
            define A/1;
            A(1);
            A(2);
            A(3);
            
            define B/1;
            B(1);
            """);
    }

    // Verify that update() catches schema mismatches
    @Test public void testUpdate_mismatch() {
        test("testUpdate_mismatch");
        db.update("A(1); A(2);");

        try {
            db.update("A(3, 4);");
            fail("Expected error.");
        } catch (JoeError ex) {
            check(ex.getTraces().getFirst().message())
                .eq("error at 'A', schema mismatch, expected shape compatible with 'A/1', got: 'A/2'.");
        }

        // Verify that the database content is unchanged.
        check(db.toNeroScript()).eq("""
            define A/1;
            A(1);
            A(2);
            """);
    }

    //-------------------------------------------------------------------------
    // query()

    // Verify that we can query without updating the database.
    @Test public void testQuery_ok() {
        test("testQuery_ok");
        db.update("A(1); A(2);");

        var inferred = db.query("B(x) :- A(x);");

        check(db.toNeroScript(inferred)).eq("""
            define B/1;
            B(1);
            B(2);
            """);
        check(db.toNeroScript()).eq("""
            define A/1;
            A(1);
            A(2);
            """);
        check(db.schema().get("B")).eq(null);
    }

    // Verify that queries must be compatible with the schema.
    @Test public void testQuery_mismatch() {
        test("testQuery_mismatch");
        db.update("A(1); A(2);");

        try {
            db.query("A(3, 4);");
            fail("Expected error.");
        } catch (JoeError ex) {
            check(ex.getTraces().getFirst().message())
                .eq("error at 'A', schema mismatch, expected shape compatible with 'A/1', got: 'A/2'.");
        }

        // Verify that the database content is unchanged.
        check(db.toNeroScript()).eq("""
            define A/1;
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
        db.update("A(1); A(2);");
        var other = new NeroDatabase().update("B(1);");

        // Also tests addFacts(Collection<Fact>)
        db.addFacts(new FactSet(other.all()));

        check(db.toNeroScript()).eq("""
            define A/1;
            A(1);
            A(2);
            
            define B/1;
            B(1);
            """);
        check(db.schema().get("B")).eq(new Shape.ListShape("B", 1));
    }

    // Verify that we check the schema while adding an arbitrary collection of
    // facts.
    @Test public void testAddFacts_factSet_mismatch() {
        test("testAddFacts_factSet_mismatch");
        db.update("A(1); A(2);");
        var other = new NeroDatabase().update("A(3, 4);");

        try {
            // Also tests addFacts(Collection<Fact>)
            db.addFacts(new FactSet(other.all()));
            fail("Expected error.");
        } catch (JoeError ex) {
            check(ex.getMessage())
                .eq("Schema mismatch for 'A', expected shape compatible with 'A/1', got: 'A/2'.");
        }

        check(db.toNeroScript()).eq("""
            define A/1;
            A(1);
            A(2);
            """);
    }

    // Verify that we can add the contents of another database, updating the
    // schema.
    @Test public void testAddFacts_db_ok() {
        test("testAddFacts_db_ok");
        db.update("A(1); A(2);");
        var other = new NeroDatabase().update("B(1);");

        // Also tests addFacts(Collection<Fact>)
        db.addFacts(other);

        check(db.toNeroScript()).eq("""
            define A/1;
            A(1);
            A(2);
            
            define B/1;
            B(1);
            """);
        check(db.schema().get("B")).eq(new Shape.ListShape("B", 1));
    }

    // Verify that we check the schema while adding the contents of another
    // database.
    @Test public void testAddFacts_db_mismatch() {
        test("testAddFacts_db_mismatch");
        db.update("A(1); A(2);");
        var other = new NeroDatabase().update("A(3, 4);");

        try {
            // Also tests addFacts(Collection<Fact>)
            db.addFacts(other);
            fail("Expected error.");
        } catch (JoeError ex) {
            check(ex.getMessage())
                .eq("Schema mismatch for 'A', expected shape compatible with 'A/1', got: 'A/2'.");
        }

        check(db.toNeroScript()).eq("""
            define A/1;
            A(1);
            A(2);
            """);
    }

    //------------------------------------------------------------------------
    // drop()

    @Test public void testDrop() {
        test("testAddFacts_db_mismatch");
        db.update("A(1); B(2);");

        check(db.schema().get("A")).eq(new Shape.ListShape("A", 1));
        check(db.schema().get("B")).eq(new Shape.ListShape("B", 1));

        db.drop("B");
        check(db.schema().get("A")).eq(new Shape.ListShape("A", 1));
        check(db.schema().get("B")).eq(null);
        check(db.toNeroScript()).eq("""
            define A/1;
            A(1);
            """);
    }


}
