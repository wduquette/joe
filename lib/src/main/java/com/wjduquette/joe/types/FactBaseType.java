package com.wjduquette.joe.types;

import com.wjduquette.joe.Args;
import com.wjduquette.joe.Joe;
import com.wjduquette.joe.JoeError;
import com.wjduquette.joe.ProxyType;
import com.wjduquette.joe.nero.FactSet;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * A ProxyType for the FactBaseValue type.
 */
public class FactBaseType extends ProxyType<FactBaseValue> {
    /** The type, ready for installation. */
    public static final FactBaseType TYPE = new FactBaseType();

    //-------------------------------------------------------------------------
    // Constructor

    /**
     * Creates the proxy.
     */
    FactBaseType() {
        super("FactBase");

        //**
        // @package joe
        // @type FactBase
        //
        // A FactBase is an in-memory database of Nero [[Fact]] values.
        // The database can be updated and queried using Nero
        // [[FactBase]] values.
        proxies(FactBaseValue.class);

        initializer(this::_init);
        iterableSupplier(this::_iterableSupplier);

        method("add",          this::_add);
        method("addAll",       this::_addAll);
        method("all",          this::_all);
        method("byRelation",   this::_byRelation);
        method("clear",        this::_clear);
        method("delete",       this::_delete);
        method("deleteAll",    this::_deleteAll);
//        method("deleteIf",     this::_deleteIf);
//        method("filter",       this::_filter);
        method("isDebug",      this::_isDebug);
        method("isEmpty",      this::_isEmpty);
//        method("map",          this::_map);
        method("relations",    this::_relations);
        method("select",       this::_select);
        method("setDebug",     this::_setDebug);
        method("size",         this::_size);
        method("toString",     this::_toString);
        method("update",       this::_update);
    }

    //-------------------------------------------------------------------------
    // Stringify

    @Override
    public String stringify(Joe joe, Object value) {
        assert value instanceof FactBaseValue;
        var db = (FactBaseValue)value;

        var buff = new StringBuilder();
        buff.append("FactBase[").append(db.getAll().size());
        for (var relation : db.getRelations().stream().sorted().toList()) {
            var items = db.getRelation(relation);
            if (!items.isEmpty()) {
                buff.append(", ")
                    .append(relation)
                    .append("[")
                    .append(items.size())
                    .append("]");
            }
        }
        buff.append("]");

        return buff.toString();
    }

    //-------------------------------------------------------------------------
    // Implementations

    //**
    // @init
    // @args [facts]
    // Creates an FactBase, optionally populating it with the given *facts*.
    // The *facts* value can be a FactBase or a collection of values
    // to be converted to facts. Throws an [[Error]] if any value cannot
    // be used as a `Fact`.
    private Object _init(Joe joe, Args args) {
        args.arityRange(0, 1, "FactBase([facts])");
        var db = new FactBaseValue();
        if (!args.isEmpty()) {
            addAll(db, joe, args.next());
        }
        return db;
    }

    // Returns a read-only SetWrapper for the FactBase's set of facts.
    private Collection<?> _iterableSupplier(Joe joe, Object value) {
        assert value instanceof FactBaseValue;
        var db = (FactBaseValue)value;
        return joe.readonlySet(db.getAll());
    }

    //-------------------------------------------------------------------------
    // Instance Method Implementations

    //**
    // @method add
    // @args fact
    // @result this
    // Adds a single value to the database as a [[Fact]].  Throws an
    // [[Error]] if the value cannot be used as a `Fact`.
    private Object _add(FactBaseValue db, Joe joe, Args args) {
        args.exactArity(1, "add(fact)");
        db.add(joe.toFact(args.next()));
        return db;
    }

    //**
    // @method addAll
    // @args facts
    // @result this
    // Adds a collection of values to the database as [[Fact]] values.
    // The *facts* value can be a FactBase or a collection of values
    // to be converted to facts. Throws an [[Error]] if any value cannot
    // be used as a `Fact`.
    //
    // **Note**: adding the contents of another FactBase is much faster
    // than adding an arbitrary collection.
    private Object _addAll(FactBaseValue db, Joe joe, Args args) {
        args.exactArity(1, "add(facts)");
        addAll(db, joe, args.next());
        return db;
    }

    //**
    // @method all
    // @result Set
    // Returns a read-only [[Set]] of all facts in the database.
    private Object _all(FactBaseValue db, Joe joe, Args args) {
        args.exactArity(0, "all()");
        return joe.readonlySet(db.getAll());
    }

    //**
    // @method byRelation
    // @arg relation
    // @result Set
    // Returns a read-only [[Set]] of all facts in the database that
    // have the given *relation*.
    private Object _byRelation(FactBaseValue db, Joe joe, Args args) {
        args.exactArity(1, "byRelation(relation)");
        return joe.readonlySet(
            db.getRelation(joe.toIdentifier(args.next())));
    }

    //**
    // @method clear
    // @result this
    // Clears the database of all content.
    private Object _clear(FactBaseValue db, Joe joe, Args args) {
        args.exactArity(0, "clear()");
        db.clear();
        return db;
    }

    //**
    // @method delete
    // @args fact
    // @result this
    // Deletes a single [[Fact]] from the database.
    private Object _delete(FactBaseValue db, Joe joe, Args args) {
        args.exactArity(1, "delete(fact)");
        db.delete(joe.toFact(args.next()));
        return db;
    }

    //**
    // @method deleteAll
    // @args facts
    // @result this
    // Deletes a collection of *facts* from the database.
    // The *facts* value can be a FactBase or a collection of values
    // to be converted to facts.
    private Object _deleteAll(FactBaseValue db, Joe joe, Args args) {
        args.exactArity(1, "deleteAll(facts)");

        var arg = args.next();

        if (arg instanceof FactSet facts) {
            db.deleteAll(facts);
        } else {
            var facts = joe.toCollection(arg);
            var factSet = new FactSet();
            for (var fact : facts) {
                factSet.add(joe.toFact(fact));
            }
            db.deleteAll(factSet);
        }

        return db;
    }

    //**
    // @method isDebug
    // @result Boolean
    // Returns the database's debug flag.
    private Object _isDebug(FactBaseValue db, Joe joe, Args args) {
        args.exactArity(0, "isDebug()");
        return db.isDebug();
    }

    //**
    // @method isEmpty
    // @result Boolean
    // Returns true if the database is empty, and false otherwise.
    private Object _isEmpty(FactBaseValue db, Joe joe, Args args) {
        args.exactArity(0, "isEmpty()");
        return db.isEmpty();
    }

    //**
    // @method relations
    // @result Set
    // Returns a read-only [[Set]] of the names of the relations
    // of the facts in the database.
    private Object _relations(FactBaseValue db, Joe joe, Args args) {
        args.exactArity(0, "relations()");
        var set = db.getRelations().stream()
            .filter(r -> !db.getRelation(r).isEmpty())
            .collect(Collectors.toSet());
        return joe.readonlySet(set);
    }

    //**
    // @method select
    // @args ruleset
    // @result Set
    // Queries the database using the *ruleset* and returns all inferred
    // facts.  If the *ruleset* contains `export` directives, the relevant
    // facts will be exported as domain values.  The database itself
    // is not modified.
    private Object _select(FactBaseValue db, Joe joe, Args args) {
        args.exactArity(1, "select(ruleset)");
        var ruleset = joe.toType(RuleSetValue.class, args.next());

        // TODO Revise execution to make best use of FactSet indexing.
        // TODO use debug flag
        return ruleset.infer(joe, db.getAll());
    }

    //**
    // @method setDebug
    // @args flag
    // @result this
    // Sets the database's debug flag.  If enabled,
    // [[FactBase#method.update]] and
    // [[FactBase#method.query]] will output a
    // detailed Nero execution trace.
    private Object _setDebug(FactBaseValue db, Joe joe, Args args) {
        args.exactArity(1, "setDebug(flag)");
        db.setDebug(joe.toBoolean(args.next()));
        return db;
    }

    //**
    // @method size
    // @result Number
    // Returns the number of facts in the database.
    private Object _size(FactBaseValue db, Joe joe, Args args) {
        args.exactArity(0, "size()");
        return (double)db.size();
    }

    //**
    // @method toString
    // @result String
    // Returns the value's string representation.
    private Object _toString(FactBaseValue db, Joe joe, Args args) {
        args.exactArity(0, "toString()");
        return stringify(joe, db);
    }

    //**
    // @method update
    // @args ruleset
    // @result Set
    // Updates the database using the *ruleset*.  Inferred facts
    // are added to the database and then returned to the caller.
    // It is an error if the *ruleset* contains `export` directives.
    private Object _update(FactBaseValue db, Joe joe, Args args) {
        args.exactArity(1, "update(ruleset)");
        var ruleset = joe.toType(RuleSetValue.class, args.next());
        if (!ruleset.exports().isEmpty()) {
            throw new JoeError(
                "Cannot `export` facts in update().");
        }

        // TODO Revise execution to make best use of FactSet indexing.
        // TODO use debug flag

        var newFacts = ruleset.infer(joe, db.getAll());
        newFacts.forEach(f -> db.add(joe.toFact(f)));
        return newFacts;
    }

    //-------------------------------------------------------------------------
    // Utilities

    // Adds the contents of the argument to the database as
    // efficiently as possible.  Throws a JoeError if the argument isn't
    // a collection or contains a non-fact.
    private void addAll(FactBaseValue db, Joe joe, Object arg) {
        if (arg instanceof FactSet facts) {
            db.addAll(facts);
        } else {
            var facts = joe.toCollection(arg);
            var factSet = new FactSet();
            for (var fact : facts) {
                factSet.add(joe.toFact(fact));
            }
            db.addAll(factSet);
        }
    }
}
