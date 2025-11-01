package com.wjduquette.joe.types;

import com.wjduquette.joe.*;

import java.util.Collection;

/**
 * A type proxy for Joe's Set types.
 */
public class SetType extends ProxyType<JoeSet> {
    /** The proxy's TYPE constant. */
    public static final SetType TYPE = new SetType();

    //-------------------------------------------------------------------------
    // Constructor

    //**
    // @package joe
    // @type Set
    // A Joe `Set` is a Java `Set`, roughly equivalent to a Java `HashSet`.
    // Sets created using the [[init:Set]] initializer can contain any kind
    // of Joe value; the set need not be homogeneous.  Sets
    // received from Java code might be read-only or require a specific
    // value type.
    /** Creates the proxy. */
    public SetType() {
        super("Set");
        proxies(SetValue.class);    // Types that implement `JoeSet`
        proxies(SetWrapper.class);

        staticMethod("of",     this::_of);

        initializer(this::_init);

        method("add",           this::_add);
        method("addAll",        this::_addAll);
        method("clear",         this::_clear);
        method("contains",      this::_contains);
        method("containsAll",   this::_containsAll);
        method("copy",          this::_copy);
        method("filter",        this::_filter);
        method("isEmpty",       this::_isEmpty);
        method("map",           this::_map);
        method("remove",        this::_remove);
        method("removeAll",     this::_removeAll);
        method("size",          this::_size);
        method("sorted",        this::_sorted);
        method("toString",      this::_toString);
    }

    //-------------------------------------------------------------------------
    // Static Method Implementations

    //**
    // @static of
    // %args values...
    // Creates a `Set` of the argument values.
    private Object _of(Joe joe, Args args) {
        return new SetValue(args.asList());
    }

    //-------------------------------------------------------------------------
    // Initializer Implementation

    //**
    // @init
    // %args [other]
    // Creates a `Set`, optionally populating it with the items from the
    // *other* collection.
    private Object _init(Joe joe, Args args) {
        args.arityRange(0, 1, "Set([other])");

        if (args.isEmpty()) {
            return new SetValue();
        } else {
            return new SetValue(joe.toCollection(args.next()));
        }
    }

    //-------------------------------------------------------------------------
    // Stringify

    @Override
    public String stringify(Joe joe, Object object) {
        assert object instanceof JoeSet;
        var set = (JoeSet)object;

        return "{" + joe.join(", ", set) + "}";
    }

    //-------------------------------------------------------------------------
    // Method Implementation

    //**
    // @method add
    // %args value
    // %result Boolean
    // Adds the *value* to the set, returning true if it wasn't already present.
    private Object _add(JoeSet set, Joe joe, Args args) {
        args.exactArity(1, "add(value)");

        return set.add(args.next());
    }

    //**
    // @method addAll
    // %args collection
    // %result Boolean
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
    // %result this
    // Empties the set.
    private Object _clear(JoeSet set, Joe joe, Args args) {
        args.exactArity(0, "clear()");

        set.clear();
        return set;
    }

    //**
    // @method contains
    // %args value
    // %result Boolean
    // Returns `true` if the set contains the *value*, and `false` otherwise.
    private Object _contains(JoeSet set, Joe joe, Args args) {
        args.exactArity(1, "contains(value)");

        return set.contains(args.next());
    }

    //**
    // @method containsAll
    // %args collection
    // %result Boolean
    // Returns `true` if the set contains the values in the
    // *collection*, and `false` otherwise.
    private Object _containsAll(JoeSet set, Joe joe, Args args) {
        args.exactArity(1, "contains(collection)");
        var collection = joe.toCollection(args.next());

        return set.containsAll(collection);
    }

    //**
    // @method copy
    // %result Set
    // Returns a shallow copy of this set.
    private Object _copy(JoeSet set, Joe joe, Args args) {
        args.exactArity(0, "copy()");

        return new SetValue(set);
    }

    //**
    // @method filter
    // %args predicate
    // %result Set
    // Returns a list containing the elements for which the filter
    // *predicate* is true.
    private Object _filter(JoeSet set, Joe joe, Args args) {
        args.exactArity(1, "filter(predicate)");
        var callable = args.next();

        var result = new SetValue();
        for (var item : set) {
            if (Joe.isTruthy(joe.call(callable, item))) {
                result.add(item);
            }
        }
        return result;
    }

    //**
    // @method isEmpty
    // %result Boolean
    // Returns `true` if the set is empty, and `false` otherwise.
    private Object _isEmpty(JoeSet set, Joe joe, Args args) {
        args.exactArity(0, "isEmpty()");

        return set.isEmpty();
    }

    //**
    // @method map
    // %args func
    // %result Set
    // Returns a set containing the items that result from applying
    // function *func* to each item in this set.
    private Object _map(JoeSet set, Joe joe, Args args) {
        args.exactArity(1, "map(func)");
        var callable = args.next();

        var result = new SetValue();
        for (var item : set) {
            result.add(joe.call(callable, item));
        }
        return result;
    }


    //**
    // @method remove
    // %args value
    // %result Boolean
    // Removes the value, return `true` if it was present and `false`
    // otherwise.
    private Object _remove(JoeSet set, Joe joe, Args args) {
        args.exactArity(1, "remove(value)");

        return set.remove(args.next());
    }

    //**
    // @method removeAll
    // %args collection
    // %result Boolean
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
    // %result Number
    // Returns the number of values in the set.
    private Object _size(JoeSet set, Joe joe, Args args) {
        args.exactArity(0, "size()");

        return (double)set.size();
    }

    //**
    // @method sorted
    // %args [comparator]
    // %result List
    // Returns a list of the set's items, sorted in ascending order.  If
    // no *comparator* is provided, then this set must be a set of
    // strings or numbers.  If a *comparator* is given, it must be a function
    // that takes two arguments and returns -1, 0, 1, like
    // the standard [[static:Joe.compare]] function.
    //
    // To sort in descending order, provide a *comparator* that reverses
    // the comparison.
    private Object _sorted(JoeSet set, Joe joe, Args args) {
        args.arityRange(0, 1, "sorted([comparator])");
        if (!args.hasNext()) {
            var result = set.stream()
                .sorted(Joe::compare)
                .toList();
            return new ListValue(result);
        } else {
            var comparator = joe.toComparator(args.next());
            var result = set.stream()
                .sorted(comparator)
                .toList();
            return new ListValue(result);
        }
    }

    //**
    // @method toString
    // %result String
    // Returns the set's string representation.
    private Object _toString(JoeSet set, Joe joe, Args args) {
        args.exactArity(0, "toString()");

        return stringify(joe, set);
    }
}
