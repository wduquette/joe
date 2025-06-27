package com.wjduquette.joe.types;

import com.wjduquette.joe.Args;
import com.wjduquette.joe.Joe;
import com.wjduquette.joe.ProxyType;

import java.util.Collection;

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

//        method("add",          this::_add);
//        method("addAll",       this::_addAll);
//        method("clear",        this::_clear);
//        method("delete",       this::_delete);
//        method("deleteAll",    this::_deleteAll);
//        method("deleteIf",     this::_deleteIf);
        method("isDebug",      this::_isDebug);
//        method("query",        this::_query);
        method("setDebug",     this::_setDebug);
        method("toString",     this::_toString);
//        method("update",       this::_update);
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
                buff.append(", ").append(items.size()).append(" ")
                    .append(relation);
            }
        }
        buff.append("]");

        return buff.toString();
    }

    //-------------------------------------------------------------------------
    // Implementations

    //**
    // @init
    // Creates an empty FactBase.
    private Object _init(Joe joe, Args args) {
        args.exactArity(0, "FactBase()");
        return new FactBaseValue();
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
    // @method isDebug
    // @result Boolean
    // Returns the database's debug flag.
    private Object _isDebug(FactBaseValue db, Joe joe, Args args) {
        args.exactArity(0, "isDebug()");
        return db.isDebug();
    }

    //**
    // @method setDebug
    // @args flag
    // Sets the database's debug flag.  If enabled,
    // [[FactBase#method.update]] and
    // [[FactBase#method.query]] will output a
    // detailed Nero execution trace.
    private Object _setDebug(FactBaseValue db, Joe joe, Args args) {
        args.exactArity(1, "setDebug(flag)");
        db.setDebug(joe.toBoolean(args.next()));
        return null;
    }

    //**
    // @method toString
    // @result String
    // Returns the value's string representation.
    private Object _toString(FactBaseValue db, Joe joe, Args args) {
        args.exactArity(0, "toString()");
        return stringify(joe, db);
    }

    //-------------------------------------------------------------------------
    // Utilities
}
