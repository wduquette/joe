package com.wjduquette.joe.types;

import com.wjduquette.joe.*;
import com.wjduquette.joe.nero.Fact;
import com.wjduquette.joe.nero.FactSet;
import com.wjduquette.joe.nero.Nero;

import java.util.Collection;
import java.util.HashSet;
import java.util.stream.Collectors;

/**
 * A ProxyType for the FactBaseValue type.
 */
public class FactBaseType extends ProxyType<FactBase> {
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
        //
        // ## Input/Output
        //
        // Subject to particular constraints, the contents of a FactBase can
        // be output as a Nero script via the
        // [[static:FactBase.asNero]] and [[method:FactBase.toNero]]
        // methods, and later read back in via the
        // [[static:FactBase.fromNero]] method.
        //
        // The constraints are (currently) as follows:
        //
        // - All facts must be ordered
        // - All field values must be scalar values representable as
        //   Joe literals.

        proxies(FactBase.class);

        initializer(this::_init);
        iterableSupplier(this::_iterableSupplier);

        staticMethod("asNero",   this::_asNero);
        staticMethod("fromNero", this::_fromNero);

        method("add",            this::_add);
        method("addAll",         this::_addAll);
        method("all",            this::_all);
        method("drop",           this::_drop);
        method("clear",          this::_clear);
        method("filter",         this::_filter);
        method("isDebug",        this::_isDebug);
        method("isEmpty",        this::_isEmpty);
        method("map",            this::_map);
        method("query",          this::_query);
        method("relation",       this::_relation);
        method("relations",      this::_relations);
        method("remove",         this::_remove);
        method("removeAll",      this::_removeAll);
        method("removeIf",       this::_removeIf);
        method("rename",         this::_rename);
        method("setDebug",       this::_setDebug);
        method("size",           this::_size);
        method("toNero",         this::_toNero);
        method("toString",       this::_toString);
        method("update",         this::_update);
    }

    //-------------------------------------------------------------------------
    // Stringify

    @Override
    public String stringify(Joe joe, Object value) {
        assert value instanceof FactBase;
        var db = (FactBase)value;

        var buff = new StringBuilder();
        buff.append("FactBase[").append(db.all().size());
        for (var relation : db.getRelations().stream().sorted().toList()) {
            var items = db.relation(relation);
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
    // Static Method Implementations

    //**
    // @static asNero
    // %args facts
    // %result String
    // Given a set of *facts*, outputs the facts as a script of Nero
    // axioms subject to certain constraints.
    //
    // The *facts* value can be a FactBase or a collection of values
    // to be converted to facts. Throws an [[Error]] if any value cannot
    // be used as a `Fact`.
    private Object _asNero(Joe joe, Args args) {
        args.exactArity(1, "FactBase.asNero(facts)");
        var arg = args.next();

        if (arg instanceof FactBase db) {
            return Nero.toNeroScript(joe, db);
        } else {
            var db = new FactBase();
            for (var fact : joe.toCollection(arg)) {
                db.add(joe.toFact(fact));
            }
            return Nero.toNeroScript(joe, db);
        }
    }

    //**
    // @static fromNero
    // %args script
    // %result FactBase
    // Given a Nero script, executes the script and returns a FactBase
    // containing all known facts.
    private Object _fromNero(Joe joe, Args args) {
        args.exactArity(1, "FactBase.fromNero(script)");
        var script = joe.toString(args.next());
        var db = new FactBase();
        new Nero(joe).with(script).update(db);
        return db;
    }

    //-------------------------------------------------------------------------
    // Method Implementations

    //**
    // @init
    // %args [facts]
    // Creates an FactBase, optionally populating it with the given *facts*.
    // The *facts* value can be a FactBase or a collection of values
    // to be converted to facts. Throws an [[Error]] if any value cannot
    // be used as a `Fact`.
    private Object _init(Joe joe, Args args) {
        args.arityRange(0, 1, "FactBase([facts])");
        var db = new FactBase();
        if (!args.isEmpty()) {
            addAll(db, joe, args.next());
        }
        return db;
    }

    // Returns a read-only SetWrapper for the FactBase's set of facts.
    private Collection<?> _iterableSupplier(Joe joe, Object value) {
        assert value instanceof FactBase;
        var db = (FactBase)value;
        return joe.readonlySet(db.all());
    }

    //-------------------------------------------------------------------------
    // Instance Method Implementations

    //**
    // @method add
    // %args fact
    // %result this
    // Adds a single value to the database as a [[Fact]].  Throws an
    // [[Error]] if the value cannot be used as a `Fact`.
    private Object _add(FactBase db, Joe joe, Args args) {
        args.exactArity(1, "add(fact)");
        db.add(joe.toFact(args.next()));
        return db;
    }

    //**
    // @method addAll
    // %args facts
    // %result this
    // Adds a collection of values to the database as [[Fact]] values.
    // The *facts* value can be a FactBase or a collection of values
    // to be converted to facts. Throws an [[Error]] if any value cannot
    // be used as a `Fact`.
    //
    // **Note**: adding the contents of another FactBase is much faster
    // than adding an arbitrary collection.
    private Object _addAll(FactBase db, Joe joe, Args args) {
        args.exactArity(1, "add(facts)");
        addAll(db, joe, args.next());
        return db;
    }

    //**
    // @method all
    // %result Set
    // Returns a read-only [[Set]] of all facts in the database.
    private Object _all(FactBase db, Joe joe, Args args) {
        args.exactArity(0, "all()");
        return joe.readonlySet(db.all());
    }

    //**
    // @method clear
    // %result this
    // Clears the database of all content.
    private Object _clear(FactBase db, Joe joe, Args args) {
        args.exactArity(0, "clear()");
        db.clear();
        return db;
    }

    //**
    // @method drop
    // %args name
    // %result this
    // Drops a relation from the database, removing all its facts.
    private Object _drop(FactBase db, Joe joe, Args args) {
        args.exactArity(1, "drop(name)");
        db.drop(joe.toIdentifier(args.next()));
        return db;
    }

    //**
    // @method filter
    // %args predicate
    // %result Set
    // Returns a set containing the elements for which the filter
    // *predicate* is true.
    private Object _filter(FactBase db, Joe joe, Args args) {
        args.exactArity(1, "filter(predicate)");
        var callable = args.next();

        var result = new SetValue();
        for (var item : db.all()) {
            if (Joe.isTruthy(joe.call(callable, item))) {
                result.add(item);
            }
        }
        return result;
    }

    //**
    // @method isDebug
    // %result Boolean
    // Returns the database's debug flag.
    private Object _isDebug(FactBase db, Joe joe, Args args) {
        args.exactArity(0, "isDebug()");
        return db.isDebug();
    }


    //**
    // @method isEmpty
    // %result Boolean
    // Returns true if the database is empty, and false otherwise.
    private Object _isEmpty(FactBase db, Joe joe, Args args) {
        args.exactArity(0, "isEmpty()");
        return db.isEmpty();
    }

    //**
    // @method map
    // %args func
    // %result Set
    // Returns a set containing the items that result from applying
    // function *func* to each item in this set.
    private Object _map(FactBase db, Joe joe, Args args) {
        args.exactArity(1, "map(func)");
        var callable = args.next();

        var result = new SetValue();
        for (var item : db.all()) {
            result.add(joe.call(callable, item));
        }
        return result;
    }

    //**
    // @method query
    // %args rules
    // %result Set
    // Queries the database using the Nero *rules* and returns all inferred
    // facts. The database itself is not modified.
    private Object _query(FactBase db, Joe joe, Args args) {
        args.exactArity(1, "query(rules)");
        var rsv = joe.toType(RuleSetValue.class, args.next());

        return rsv.infer(joe, db);
    }


    //**
    // @method relation
    // %args relation
    // %result Set
    // Returns a read-only [[Set]] of all facts in the database that
    // have the given *relation*.
    private Object _relation(FactBase db, Joe joe, Args args) {
        args.exactArity(1, "relation(relation)");
        return joe.readonlySet(
            db.relation(joe.toIdentifier(args.next())));
    }

    //**
    // @method relations
    // %result Set
    // Returns a read-only [[Set]] of the names of the relations
    // of the facts in the database.
    private Object _relations(FactBase db, Joe joe, Args args) {
        args.exactArity(0, "relations()");
        var set = db.getRelations().stream()
            .filter(r -> !db.relation(r).isEmpty())
            .collect(Collectors.toSet());
        return joe.readonlySet(set);
    }

    //**
    // @method remove
    // %args fact
    // %result this
    // Deletes a single [[Fact]] from the database.
    private Object _remove(FactBase db, Joe joe, Args args) {
        args.exactArity(1, "remove(fact)");
        db.remove(joe.toFact(args.next()));
        return db;
    }

    //**
    // @method removeAll
    // %args facts
    // %result this
    // Deletes a collection of *facts* from the database.
    // The *facts* value can be a FactBase or a collection of values
    // to be converted to facts.
    private Object _removeAll(FactBase db, Joe joe, Args args) {
        args.exactArity(1, "removeAll(facts)");

        var arg = args.next();

        if (arg instanceof FactSet facts) {
            db.removeAll(facts);
        } else {
            var facts = joe.toCollection(arg);
            var factSet = new FactSet();
            for (var fact : facts) {
                factSet.add(joe.toFact(fact));
            }
            db.removeAll(factSet);
        }

        return db;
    }

    //**
    // @method removeIf
    // %args predicate
    // %result this
    // Deletes facts matching the predicate from the database.
    private Object _removeIf(FactBase db, Joe joe, Args args) {
        args.exactArity(1, "filter(predicate)");
        var callable = args.next();

        var items = new HashSet<Fact>();
        for (var item : db.all()) {
            if (Joe.isTruthy(joe.call(callable, item))) {
                items.add(item);
            }
        }
        db.removeAll(items);
        return db;
    }

    //**
    // @method rename
    // %args oldName, newName
    // %result this
    // Renames a relation, replacing any existing relation that has the new
    // name.
    private Object _rename(FactBase db, Joe joe, Args args) {
        args.exactArity(2, "rename(oldName)");
        db.rename(
            joe.toIdentifier(args.next()),
            joe.toIdentifier(args.next())
        );
        return db;
    }

    //**
    // @method setDebug
    // %args flag
    // %result this
    // Sets the database's debug flag.  If enabled,
    // [[method:FactBase.update]] and
    // [[method:FactBase.select]] will output a
    // detailed Nero execution trace.
    private Object _setDebug(FactBase db, Joe joe, Args args) {
        args.exactArity(1, "setDebug(flag)");
        db.setDebug(joe.toBoolean(args.next()));
        return db;
    }

    //**
    // @method size
    // %result Number
    // Returns the number of facts in the database.
    private Object _size(FactBase db, Joe joe, Args args) {
        args.exactArity(0, "size()");
        return (double)db.size();
    }

    //**
    // @method toNero
    // %result String
    // Returns a Nero script containing the database items as
    // Nero axioms.
    private Object _toNero(FactBase db, Joe joe, Args args) {
        args.exactArity(0, "toNero()");
        return Nero.toNeroScript(joe, db);
    }

    //**
    // @method toString
    // %result String
    // Returns the value's string representation.
    private Object _toString(FactBase db, Joe joe, Args args) {
        args.exactArity(0, "toString()");
        return stringify(joe, db);
    }

    //**
    // @method update
    // %args rules
    // %result this
    // Updates the database using the Nero *rules*.  Inferred facts
    // are added to the database and then returned to the caller.
    private Object _update(FactBase db, Joe joe, Args args) {
        args.exactArity(1, "update(ruleset)");
        var rsv = joe.toType(RuleSetValue.class, args.next());
        db.addAll(rsv.infer(joe, db));
        return db;
    }

    //-------------------------------------------------------------------------
    // Utilities

    // Adds the contents of the argument to the database as
    // efficiently as possible.  Throws a JoeError if the argument isn't
    // a collection or contains a non-fact.
    private void addAll(FactBase db, Joe joe, Object arg) {
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
