package com.wjduquette.joe.types;

import com.wjduquette.joe.*;

import java.util.Collection;

/**
 * A type proxy for Joe's Set types.
 */
public class SetProxy extends TypeProxy<JoeSet> {
    /** The proxy's TYPE constant. */
    public static final SetProxy TYPE = new SetProxy();

    //-------------------------------------------------------------------------
    // Constructor

    //**
    // @package joe
    // @type Set
    // A Joe `Set` is a Java `Set`, roughly equivalent to a Java `HashSet`.
    // Sets created using the [[Set#init]] initializer can contain any kind
    // of Joe value; the set need not be homogeneous.  Sets
    // received from Java code might be read-only or require a specific
    // value type.
    /** Creates the proxy. */
    public SetProxy() {
        super("Set");
        proxies(SetValue.class);    // Types that implement `JoeSet`
        proxies(SetWrapper.class);
        initializer(this::_init);

        method("add",           this::_add);
        method("addAll",        this::_addAll);
        method("clear",         this::_clear);
        method("contains",      this::_contains);
        method("copy",          this::_copy);
        method("isEmpty",       this::_isEmpty);
        method("remove",        this::_remove);
        method("removeAll",     this::_removeAll);
        method("size",          this::_size);
        method("toString",      this::_toString);
    }

    //-------------------------------------------------------------------------
    // Initializer Implementation

    //**
    // @init
    // @args values...
    // Creates a `Set` of the argument values, which must be a flat list of
    // key/value pairs.
    private Object _init(Joe joe, Args args) {
        return new SetValue(args.asList());
    }

    //-------------------------------------------------------------------------
    // Stringify

    @Override
    public String stringify(Joe joe, Object object) {
        assert object instanceof JoeSet;
        var set = (JoeSet)object;

        return "Set(" + joe.join(", ", set) + ")";
    }

    //-------------------------------------------------------------------------
    // Method Implementation

    //**
    // @method add
    // @args value
    // @result Boolean
    // Adds the *value* to the set, returning true if it wasn't already present.
    private Object _add(JoeSet set, Joe joe, Args args) {
        args.exactArity(1, "add(value)");

        return set.add(args.next());
    }

    //**
    // @method addAll
    // @args collection
    // @result Boolean
    // Adds the content of the *collection* to this set.
    private Object _addAll(JoeSet set, Joe joe, Args args) {
        args.exactArity(1, "addAll(collection)");
        var arg = args.next();

        if (arg instanceof Collection<?> other) {
            return set.addAll(other);
        } else {
            throw joe.expected("collection", arg);
        }
    }

    //**
    // @method clear
    // @result this
    // Empties the set.
    private Object _clear(JoeSet set, Joe joe, Args args) {
        args.exactArity(0, "clear()");

        set.clear();
        return set;
    }

    //**
    // @method contains
    // @args value
    // @result Boolean
    // Returns `true` if the set contains the *value*, and `false` otherwise.
    private Object _contains(JoeSet set, Joe joe, Args args) {
        args.exactArity(1, "contains(value)");

        return set.contains(args.next());
    }

    //**
    // @method copy
    // @result Set
    // Returns a shallow copy of this set.
    private Object _copy(JoeSet set, Joe joe, Args args) {
        args.exactArity(0, "copy()");

        return new SetValue(set);
    }


    //**
    // @method isEmpty
    // @result Boolean
    // Returns `true` if the set is empty, and `false` otherwise.
    private Object _isEmpty(JoeSet set, Joe joe, Args args) {
        args.exactArity(0, "isEmpty()");

        return set.isEmpty();
    }

    //**
    // @method remove
    // @args value
    // @result Boolean
    // Removes the value, return `true` if it was present and `false`
    // otherwise.
    private Object _remove(JoeSet set, Joe joe, Args args) {
        args.exactArity(1, "remove(value)");

        return set.remove(args.next());
    }

    //**
    // @method removeAll
    // @args collection
    // @result Boolean
    // Removes all values in the *collection* from the set, returning `true`
    // if the set changed and `false` otherwise.
    private Object _removeAll(JoeSet set, Joe joe, Args args) {
        args.exactArity(1, "removeAll(collection)");

        var arg = args.next();
        if (arg instanceof Collection<?> other) {
            return set.removeAll(other);
        } else {
            throw joe.expected("collection", arg);
        }
    }

    //**
    // @method size
    // @result Number
    // Returns the number of values in the set.
    private Object _size(JoeSet set, Joe joe, Args args) {
        args.exactArity(0, "size()");

        return (double)set.size();
    }

    //**
    // @method toString
    // @result String
    // Returns the set's string representation.
    private Object _toString(JoeSet set, Joe joe, Args args) {
        args.exactArity(0, "toString()");

        return stringify(joe, set);
    }
}
