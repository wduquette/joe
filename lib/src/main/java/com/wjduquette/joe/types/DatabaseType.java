package com.wjduquette.joe.types;

import com.wjduquette.joe.*;
import com.wjduquette.joe.nero.*;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * A ProxyType for the NeroDatabase type.
 */
public class DatabaseType extends ProxyType<NeroDatabase> {
    /** The type, ready for installation. */
    public static final DatabaseType TYPE = new DatabaseType();

    //-------------------------------------------------------------------------
    // Constructor

    /**
     * Creates the proxy.
     */
    DatabaseType() {
        super("Database");

        //**
        // @package joe
        // @type Database
        // %javaType com.wjduquette.joe.nero.NeroDatabase
        // %proxyType com.wjduquette.joe.types.DatabaseType
        // The `Database` object allows Joe code to work with a collection
        // of Nero [[Fact|Facts]] over a series of transactions.
        proxies(NeroDatabase.class);

        initializer(this::_init);

        method("addFacts",        this::_addFacts);
        method("all",             this::_all);
        method("clear",           this::_clear);
        method("debug",           this::_debug);
        method("drop",            this::_drop);
        method("equivalence",     this::_equivalence);
        method("equivalences",    this::_equivalences);
        method("filter",          this::_filter);
        method("getEquivalences", this::_getEquivalences);
        method("isDebug",         this::_isDebug);
        method("isEmpty",         this::_isEmpty);
        method("load",            this::_load);
        method("map",             this::_map);
        method("query",           this::_query);
        method("relation",        this::_relation);
        method("relations",       this::_relations);
        method("remove",          this::_remove);
        method("removeAll",       this::_removeAll);
        method("removeIf",        this::_removeIf);
        method("rename",          this::_rename);
        method("size",            this::_size);
        method("toNeroScript",    this::_toNeroScript);
        method("toNeroAxiom",     this::_toNeroAxiom);
        method("toString",        this::_toString);
        method("update",          this::_update);
    }

    //-------------------------------------------------------------------------
    // Stringify

    public String stringify(Joe joe, Object value) {
        return name() + "@" + value.hashCode();
    }

    //-------------------------------------------------------------------------
    // Initializer

    //**
    // @init
    // Creates a new instance of `Database`.
    private Object _init(Joe joe, Args args) {
        args.exactArity(0, "Database()");
        return new NeroDatabase(joe);
    }

    //-------------------------------------------------------------------------
    // Instance Method Implementations

    //**
    // @method addFacts
    // %args facts
    // %result this
    // Adds a collection of values to the database as [[Fact]] values.
    // The values can be [[Fact|Facts]] or values
    // to be converted to facts. Throws an [[Error]] if any value cannot
    // be used as a `Fact`.
    private Object _addFacts(NeroDatabase db, Joe joe, Args args) {
        args.exactArity(1, "add(facts)");
        db.addFacts(joe.toFacts(args.next()));
        return db;
    }

    //**
    // @method all
    // %result Set
    // Returns a read-only [[Set]] of all facts in the database.
    private Object _all(NeroDatabase db, Joe joe, Args args) {
        args.exactArity(0, "all()");
        return joe.readonlySet(db.all());
    }

    //**
    // @method clear
    // %result this
    // Clears the database of all content.
    private Object _clear(NeroDatabase db, Joe joe, Args args) {
        args.exactArity(0, "clear()");
        db.clear();
        return db;
    }

    //**
    // @method debug
    // %args [flag]
    // %result this
    // Sets the database's debug *flag*.  If omitted, the flag defaults to
    // true.
    private Object _debug(NeroDatabase db, Joe joe, Args args) {
        args.arityRange(0, 1, "debug([flag])");
        db.setDebug(!args.hasNext() || joe.toBoolean(args.next()));
        return db;
    }

    //**
    // @method drop
    // %args name
    // %result this
    // Drops a relation from the database, removing all its facts.
    private Object _drop(NeroDatabase db, Joe joe, Args args) {
        args.exactArity(1, "drop(name)");
        db.drop(joe.toIdentifier(args.next()));
        return db;
    }


    //**
    // @method equivalence
    // %args equivalence
    // %result this
    // Adds an equivalence relation for use with the
    // `equivalent/equivalence,a,b` built-in predicate.
    private Object _equivalence(NeroDatabase db, Joe joe, Args args) {
        args.exactArity(1, "equivalence(equivalence)");
        db.addEquivalence(joe.toType(Equivalence.class, args.next()));
        return db;
    }

    //**
    // @method equivalences
    // %args equivalence, ...
    // %args list
    // %result this
    // Adds equivalences relation for use with the
    // `equivalent/equivalence,a,b` built-in predicate.  The equivalences
    // can be passed as individual arguments or as a single list.
    private Object _equivalences(NeroDatabase db, Joe joe, Args args) {
        args = args.expandOrRemaining();
        var list = new ArrayList<Equivalence>();
        while (args.hasNext()) {
            list.add(joe.toType(Equivalence.class, args.next()));
        }
        db.addEquivalences(list);
        return db;
    }

    //**
    // @method filter
    // %args predicate
    // %result Set
    // Returns a set containing the [[Fact|Facts]] for which the filter
    // *predicate* is true.
    private Object _filter(NeroDatabase db, Joe joe, Args args) {
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
    // @method getEquivalences
    // %result Set
    // Returns a set of all client-defined equivalences.
    private Object _getEquivalences(NeroDatabase db, Joe joe, Args args) {
        args.exactArity(0, "getEquivalences()");
        return new SetValue(db.getEquivalences().values());
    }

    //**
    // @method isDebug
    // %result Boolean
    // Returns the debug flag.
    private Object _isDebug(NeroDatabase db, Joe joe, Args args) {
        args.exactArity(0, "isDebug()");
        return db.isDebug();
    }

    //**
    // @method isEmpty
    // %result Boolean
    // Returns true if the database is empty, and false otherwise.
    private Object _isEmpty(NeroDatabase db, Joe joe, Args args) {
        args.exactArity(0, "isEmpty()");
        return db.isEmpty();
    }

    //**
    // @method load
    // %args path
    // %result this
    // Loads the Nero script at the given *path* and uses it to update the
    // database.  The *path* may be passed as a
    // [[Path]] or string.
    private Object _load(NeroDatabase db, Joe joe, Args args) {
        args.exactArity(1, "load(path)");
        throw new JoeError("Not implemented yet!");
//        return db.load(joe.toPath(args.next()));
    }

    //**
    // @method map
    // %args func
    // %result Set
    // Returns a set containing the items that result from applying
    // function *func* to each item in this set.
    private Object _map(NeroDatabase db, Joe joe, Args args) {
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
    // Queries the database given the *rules*, returning a set of
    // [[Fact|Facts]].  The *rules* may be passed
    // as a [[RuleSet]] or as a Nero script for compilation.  Does not
    // modify the database.
    private Object _query(NeroDatabase db, Joe joe, Args args) {
        args.exactArity(1, "query(rules)");
        return new SetValue(db.query(toRules(db, joe, args.next())).all());
    }

    //**
    // @method relation
    // %args relation
    // %result Set
    // Returns a read-only [[Set]] of all facts in the database that
    // have the given *relation*.
    private Object _relation(NeroDatabase db, Joe joe, Args args) {
        args.exactArity(1, "relation(relation)");
        return joe.readonlySet(
            db.relation(joe.toIdentifier(args.next())));
    }

    //**
    // @method relations
    // %result Set
    // Returns a read-only [[Set]] of the names of the relations
    // of the facts in the database.
    private Object _relations(NeroDatabase db, Joe joe, Args args) {
        args.exactArity(0, "relations()");
        return joe.readonlySet(db.getRelations());
    }

    //**
    // @method remove
    // %args fact
    // %result this
    // Deletes a single [[Fact]] from the database.
    private Object _remove(NeroDatabase db, Joe joe, Args args) {
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
    private Object _removeAll(NeroDatabase db, Joe joe, Args args) {
        args.exactArity(1, "removeAll(facts)");
        var facts = joe.toFacts(args.next());
        db.removeAll(facts);
        return db;
    }

    //**
    // @method removeIf
    // %args predicate
    // %result this
    // Deletes facts matching the predicate from the database.
    private Object _removeIf(NeroDatabase db, Joe joe, Args args) {
        args.exactArity(1, "removeIf(predicate)");
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
    private Object _rename(NeroDatabase db, Joe joe, Args args) {
        args.exactArity(2, "rename(oldName, newName)");
        db.rename(
            joe.toIdentifier(args.next()),
            joe.toIdentifier(args.next())
        );
        return db;
    }

    //**
    // @method size
    // %result Number
    // Returns the number of facts in the database.
    private Object _size(NeroDatabase db, Joe joe, Args args) {
        args.exactArity(0, "size()");
        return (double)db.size();
    }

    //**
    // @method toNeroScript
    // %args [facts]
    // %result String
    // Returns a Nero script for a collection of [[Fact|Facts]].  Every value
    // in *facts* must either be a [[Fact]] or a value that can be converted
    // into a [[Fact]]. If *facts* is omitted, returns a Nero script for the
    // content of the database.
    //
    // Throws an error if any fact contains a term that cannot be represented
    // in Nero syntax.
    private Object _toNeroScript(NeroDatabase db, Joe joe, Args args) {
        args.exactArity(1, "toNeroScript(facts)");
        return db.toNeroScript(joe.toFacts(args.next()));
    }

    //**
    // @method toNeroAxiom
    // %args fact
    // %result String
    // Returns the *fact* as a Nero axiom string.  The *fact* must either
    // be a [[Fact]] or an object that can be converted into a [[Fact]].
    //
    // Throws an error if the fact contains a term that cannot be represented
    // in Nero syntax.
    private Object _toNeroAxiom(NeroDatabase db, Joe joe, Args args) {
        args.exactArity(1, "toNeroAxiom(fact)");
        return db.toNeroAxiom(joe.toFact(args.next()));
    }

    //**
    // @method toString
    // %result String
    // Returns the value's string representation.
    private Object _toString(NeroDatabase db, Joe joe, Args args) {
        args.exactArity(0, "toString()");
        return stringify(joe, db);
    }

    //**
    // @method update
    // %args rules
    // %result this
    // Updates the database given the *rules*.  The *rules* may be passed
    // as a [[RuleSet]] or as a Nero script for compilation.
    private Object _update(NeroDatabase db, Joe joe, Args args) {
        args.exactArity(1, "update(rules)");
        return db.update(toRules(db, joe, args.next()));
    }

    private NeroRuleSet toRules(NeroDatabase db, Joe joe, Object arg) {
        if (arg instanceof NeroRuleSet rs) return rs;
        if (arg instanceof String s) {
            return Nero.compile(db.schema(), new SourceBuffer("*joe*", s));
        }
        throw joe.expected("ruleset or script", arg);
    }
}
