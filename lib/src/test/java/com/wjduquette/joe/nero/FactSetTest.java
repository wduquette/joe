package com.wjduquette.joe.nero;

import com.wjduquette.joe.Ted;
import org.junit.Test;

import java.util.List;
import java.util.Set;

import static com.wjduquette.joe.checker.Checker.check;

public class FactSetTest extends Ted {
    private FactSet db;

    //-------------------------------------------------------------------------
    // Constructor

    @Test
    public void testConstructor_noArgs() {
        test("testConstructor_noArgs()");

        db = new FactSet();
        check(db.isEmpty()).eq(true);
        check(db.size()).eq(0);
        check(db.all()).eq(Set.of());
        check(db.getRelations()).eq(Set.of());
    }

    @Test
    public void testConstructor_facts() {
        test("testConstructor_facts()");

        db = new FactSet(List.of(
            fact("A", 1),
            fact("B", 1)
        ));
        check(db.isEmpty()).eq(false);
        check(db.size()).eq(2);
        check(db.all()).eq(Set.of(
            fact("A", 1),
            fact("B", 1)
        ));
        check(db.getRelations()).eq(Set.of("A", "B"));
    }

    @Test
    public void testConstructor_other() {
        test("testConstructor_facts()");

        var other = new FactSet(List.of(
            fact("A", 1),
            fact("B", 1)
        ));
        db = new FactSet(other);
        check(db.isEmpty()).eq(false);
        check(db.size()).eq(2);
        check(db.all()).eq(Set.of(
            fact("A", 1),
            fact("B", 1)
        ));
        check(db.getRelations()).eq(Set.of("A", "B"));
    }

    //-------------------------------------------------------------------------
    // add

    @Test
    public void testAdd() {
        test("testAdd");

        db = new FactSet();
        check(db.relation("A").size()).eq(0);

        db.add(fact("A", 1));
        check(db.size()).eq(1);
        check(db.getRelations()).eq(Set.of("A"));
        check(db.relation("A")).eq(Set.of(fact("A", 1)));

        db.add(fact("B", 2));
        check(db.size()).eq(2);
        check(db.getRelations()).eq(Set.of("A", "B"));
        check(db.relation("B")).eq(Set.of(fact("B", 2)));

        db.add(fact("A", 2));
        check(db.size()).eq(3);
        check(db.getRelations()).eq(Set.of("A", "B"));
        check(db.relation("A")).eq(Set.of(fact("A", 1), fact("A", 2)));
        check(db.all()).eq(Set.of(
            fact("A", 1),
            fact("A", 2),
            fact("B", 2)
        ));
    }

    //-------------------------------------------------------------------------
    // addAll

    @Test
    public void testAddAll_facts() {
        test("testAddAll_facts");

        db = new FactSet();
        check(db.size()).eq(0);

        db.addAll(Set.of(
            fact("A", 1),
            fact("A", 2),
            fact("B", 2)
        ));

        check(db.size()).eq(3);
        check(db.getRelations()).eq(Set.of("A", "B"));
        check(db.relation("A")).eq(Set.of(fact("A", 1), fact("A", 2)));
        check(db.relation("B")).eq(Set.of(fact("B", 2)));
        check(db.all()).eq(Set.of(
            fact("A", 1),
            fact("A", 2),
            fact("B", 2)
        ));
    }

    @Test
    public void testAddAll_other() {
        test("testAddAll_facts");

        db = new FactSet();
        check(db.size()).eq(0);

        var other = new FactSet(Set.of(
            fact("A", 1),
            fact("A", 2),
            fact("B", 2)
        ));
        db.addAll(other);

        check(db.size()).eq(3);
        check(db.getRelations()).eq(Set.of("A", "B"));
        check(db.relation("A")).eq(Set.of(fact("A", 1), fact("A", 2)));
        check(db.relation("B")).eq(Set.of(fact("B", 2)));
        check(db.all()).eq(Set.of(
            fact("A", 1),
            fact("A", 2),
            fact("B", 2)
        ));
    }

    //-------------------------------------------------------------------------
    // clear()

    @Test public void testClear() {
        test("testClear");

        var db = new FactSet(Set.of(
            fact("A", 1),
            fact("A", 2),
            fact("B", 2)
        ));
        db.clear();

        check(db.size()).eq(0);
        check(db.isEmpty()).eq(true);
        check(db.getRelations()).eq(Set.of());
    }

    //-------------------------------------------------------------------------
    // contains()

    @Test public void testContains() {
        test("testContains");

        var a1 = fact("A", 1);
        var a2 = fact("A", 2);
        var b1 = fact("B", 1);
        var b2 = fact("B", 2);

        var db = new FactSet(Set.of(a1, a2, b1));

        check(db.contains(a1)).eq(true);
        check(db.contains(a2)).eq(true);
        check(db.contains(b1)).eq(true);
        check(db.contains(b2)).eq(false);
    }
    //-------------------------------------------------------------------------
    // drop()

    @Test public void testDrop() {
        test("testDrop");

        var db = new FactSet(Set.of(
            fact("A", 1),
            fact("A", 2),
            fact("B", 2)
        ));
        db.drop("A");

        check(db.size()).eq(1);
        check(db.getRelations()).eq(Set.of("B"));
        check(db.all()).eq(Set.of(fact("B", 2)));
        check(db.relation("A")).eq(Set.of());
    }

    //-------------------------------------------------------------------------
    // remove()

    @Test public void testRemove() {
        test("testRemove");

        var db = new FactSet(Set.of(
            fact("A", 1),
            fact("A", 2),
            fact("B", 2)
        ));
        db.remove(fact("A", 2));

        check(db.size()).eq(2);
        check(db.getRelations()).eq(Set.of("A", "B"));
        check(db.all()).eq(Set.of(fact("A", 1), fact("B", 2)));
    }

    //-------------------------------------------------------------------------
    // remove()

    @Test public void testRemoveAll_facts() {
        test("testRemoveAll_facts");

        var db = new FactSet(Set.of(
            fact("A", 1),
            fact("A", 2),
            fact("B", 3),
            fact("B", 4)
        ));
        db.removeAll(Set.of(fact("A", 2), fact("B", 4)));

        check(db.size()).eq(2);
        check(db.getRelations()).eq(Set.of("A", "B"));
        check(db.all()).eq(Set.of(fact("A", 1), fact("B", 3)));
    }

    @Test public void testRemoveAll_other() {
        test("testRemoveAll_other");

        var db = new FactSet(Set.of(
            fact("A", 1),
            fact("A", 2),
            fact("B", 3),
            fact("B", 4)
        ));

        var other = new FactSet(Set.of(fact("A", 2), fact("B", 4)));
        db.removeAll(other);

        check(db.size()).eq(2);
        check(db.getRelations()).eq(Set.of("A", "B"));
        check(db.all()).eq(Set.of(fact("A", 1), fact("B", 3)));
    }

    //-------------------------------------------------------------------------
    // rename()

    @Test public void testRename() {
        test("testRename");

        var db = new FactSet(Set.of(
            fact("A", 1),
            fact("A", 2),
            fact("B", 3),
            fact("B", 4)
        ));
        db.rename("B", "A");

        // Old A's are gone, Old B's are A's.
        check(db.size()).eq(2);
        check(db.getRelations()).eq(Set.of("A"));
        check(db.all()).eq(Set.of(fact("A", 3), fact("A", 4)));
    }

    //-------------------------------------------------------------------------
    // eq/hashCode

    @Test public void testEqHashCode() {
        test("testEqHashCode");

        db = new FactSet(Set.of(
            fact("A", 1),
            fact("A", 2),
            fact("B", 3),
            fact("B", 4)
        ));
        var other = new FactSet(Set.of(
            fact("A", 1),
            fact("A", 2),
            fact("B", 3),
            fact("B", 4)
        ));

        check(db.equals(other)).eq(true);
        check(db.hashCode() == other.hashCode()).eq(true);

        other.drop("B");
        check(db.equals(other)).eq(false);
        check(db.hashCode() == other.hashCode()).eq(false);
    }

    //-------------------------------------------------------------------------
    // getRelation()/getRelations()

    // Verify that using getRelation for an unknown relation returns an
    // empty set but does not add the relation to getRelations.
    // (This was a bug.)
    @Test public void testRelations() {
        test("testRelations");

        var db = new FactSet();

        check(db.relation("Unknown")).eq(Set.of());
        check(db.getRelations()).eq(Set.of());
    }

    //-------------------------------------------------------------------------
    // Helpers

    private Fact fact(String relation, double value) {
        return new ListFact(relation, List.of(value));
    }
}
