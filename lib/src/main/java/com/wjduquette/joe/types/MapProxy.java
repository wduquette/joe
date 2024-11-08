package com.wjduquette.joe.types;

import com.wjduquette.joe.*;

import java.util.Map;
import java.util.stream.Collectors;

public class MapProxy extends TypeProxy<JoeMap> {
    /** The proxy's TYPE constant. */
    public static final MapProxy TYPE = new MapProxy();

    //-------------------------------------------------------------------------
    // Constructor

    //**
    // @package joe
    // @type Map
    // A Joe `Map` is a Java `Map`, roughly equivalent to a Java `HashMap`.
    // Maps created using the [[Map#init]] initializer can contain any kind
    // of Joe keys and values; the map need not be homogeneous.  Maps
    // received from Java code might be read-only or require a specific
    // key/value types.
    /** Creates the proxy. */
    public MapProxy() {
        super("Map");
        proxies(MapValue.class);    // Types that implement `JoeMap`
        proxies(MapWrapper.class);
        initializer(this::_init);

        method("clear",         this::_clear);
        method("containsKey",   this::_containsKey);
        method("containsValue", this::_containsValue);
        method("copy",          this::_copy);
        method("get",           this::_get);
        method("getOrDefault",  this::_getOrDefault);
        method("isEmpty",       this::_isEmpty);
        method("keySet",        this::_keySet);
        method("put",           this::_put);
        method("putAll",        this::_putAll);
        method("remove",        this::_remove);
        method("size",          this::_size);
        method("toString",      this::_toString);
        method("values",        this::_values);
    }

    //-------------------------------------------------------------------------
    // Initializer Implementation

    //**
    // @init
    // @args values...
    // Creates a `Map` of the argument values, which must be a flat list of
    // key/value pairs.
    private Object _init(Joe joe, Args args) {
        if (args.size() % 2 != 0) {
            throw new JoeError("Expected an even number of arguments, got: '" +
                joe.join(", ", args.asList()) + "'.");
        }

        var map = new MapValue();

        while (args.hasNext()) {
            map.put(args.next(), args.next());
        }

        return map;
    }

    //-------------------------------------------------------------------------
    // Stringify

    @Override
    public String stringify(Joe joe, Object object) {
        assert object instanceof JoeMap;
        var map = (JoeMap)object;

        return "Map("
            + map.entrySet().stream()
                .map(e -> joe.stringify(e.getKey()) + "=" + joe.stringify(e.getValue()))
                .collect(Collectors.joining(", "))
            + ")";
    }

    //-------------------------------------------------------------------------
    // Method Implementation

    //**
    // @method clear
    // @result this
    // Empties the map.
    private Object _clear(JoeMap map, Joe joe, Args args) {
        args.exactArity(0, "clear()");

        map.clear();
        return map;
    }

    //**
    // @method containsKey
    // @args key
    // @result Boolean
    // Returns `true` if the map contains the *key*, and `false` otherwise.
    private Object _containsKey(JoeMap map, Joe joe, Args args) {
        args.exactArity(1, "containsKey(key)");

        return map.containsKey(args.next());
    }

    //**
    // @method containsValue
    // @args value
    // @result Boolean
    // Returns `true` if the map has at least one key with the given
    // *value*, and `false` otherwise.
    private Object _containsValue(JoeMap map, Joe joe, Args args) {
        args.exactArity(1, "containsValue(value)");

        return map.containsValue(args.next());
    }

    //**
    // @method copy
    // @result Map
    // Returns a shallow copy of this map.
    private Object _copy(JoeMap map, Joe joe, Args args) {
        args.exactArity(0, "copy()");

        return new MapValue(map);
    }

    //**
    // @method get
    // @args key
    // @result value
    // Gets the *key*'s value, or null if the key is not found.
    private Object _get(JoeMap map, Joe joe, Args args) {
        args.exactArity(1, "get(key)");

        return map.get(args.next());
    }

    //**
    // @method getOrDefault
    // @args key, defaultValue
    // @result value
    // Gets the *key*'s value, or the *defaultValue* if the key is not found.
    private Object _getOrDefault(JoeMap map, Joe joe, Args args) {
        args.exactArity(2, "getOrDefault(key, defaultValue)");

        return map.getOrDefault(args.next(), args.next());
    }

    //**
    // @method isEmpty
    // @result Boolean
    // Returns `true` if the map is empty, and `false` otherwise.
    private Object _isEmpty(JoeMap map, Joe joe, Args args) {
        args.exactArity(0, "isEmpty()");

        return map.isEmpty();
    }

    //**
    // @method keySet
    // @result Set
    // Returns a set of the keys in the map.
    private Object _keySet(JoeMap map, Joe joe, Args args) {
        args.exactArity(0, "keySet()");

        return SetWrapper.readOnly(joe, map.keySet());
    }

    //**
    // @method put
    // @args key, value
    // @result value
    // Adds the *key/*value* pair to the map, returning the replaced value.
    private Object _put(JoeMap map, Joe joe, Args args) {
        args.exactArity(2, "put(key, value)");

        return map.put(args.next(), args.next());
    }

    //**
    // @method putAll
    // @args map
    // @result this
    // Adds the content of the map to this map.
    private Object _putAll(JoeMap map, Joe joe, Args args) {
        args.exactArity(1, "putAll(map)");
        var arg = args.next();

        if (arg instanceof Map<?,?> other) {
            map.putAll(other);
        } else {
            throw joe.expected("map", arg);
        }

        return map;
    }

    //**
    // @method remove
    // @args key
    // @result value
    // Removes and returns the *key*'s value, or null if the key wasn't found.
    private Object _remove(JoeMap map, Joe joe, Args args) {
        args.exactArity(1, "remove(key)");

        return map.remove(args.next());
    }

    //**
    // @method size
    // @result Number
    // Returns the number of key/value pairs in the map.
    private Object _size(JoeMap map, Joe joe, Args args) {
        args.exactArity(0, "size()");

        return (double)map.size();
    }

    //**
    // @method toString
    // @result String
    // Returns the map's string representation.
    private Object _toString(JoeMap map, Joe joe, Args args) {
        args.exactArity(0, "toString()");

        return stringify(joe, map);
    }

    //**
    // @method values
    // @result List
    // Returns a list of the values in the map.
    private Object _values(JoeMap map, Joe joe, Args args) {
        args.exactArity(0, "values()");

        return new ListValue(map.values());
    }
}
