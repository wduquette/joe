package com.wjduquette.joe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A "Tuple" is an immutable object containing some number of fields
 * whose names are specified at creation time.  This is valid object
 * for use in Joe scripts without a proxy; all methods are defined here.
 * StandardLibrary.java defines the type's constructor as a global
 * function `Tuple()`.
 */
public class Tuple implements JoeObject, JoeToString {
    //-------------------------------------------------------------------------
    // Static Variables
    public static final String ANY = "_";

    private static final Map<String, JoeValueCallable<Tuple>> METHODS =
        new HashMap<>();
    static {
        METHODS.put("matches",  Tuple::_matches);
        METHODS.put("toString", Tuple::_toString);
    }

    //-------------------------------------------------------------------------
    // Instance Variables

    private final Joe joe;
    private final Map<String,Object> fields = new LinkedHashMap<>();

    //-------------------------------------------------------------------------
    // Constructor

    //**
    // @package joe
    // @type Tuple
    // @init
    // @args name, value, [name, value]...
    // Given name and value pairs, returns a `Tuple` object with
    // the named fields assigned to the given values.
    //
    // For example,
    //
    // ```joe
    // var tuple = Tuple(#flag, true, #result, 123);
    //
    // println("flag   = " + tuple.flag);
    // println("result = " + tuple.result);
    // ```
    /**
     * The argument list is a flat list of field name/value pairs.
     * The names may be passed as Strings or Keywords.
     * @param joe The interpreter
     * @param args The name/value array
     */
    public Tuple(Joe joe, ArgQueue args) {
        this.joe = joe;
        if (args.size() % 2 != 0) {
            throw new JoeError("Expected an even number of arguments.");
        }

        for (int i = 0; i < args.size(); i += 2) {
            var name = toName(joe, args.get(i));

            if (!joe.isIdentifier(name)) {
                throw joe.expected("field name", args.get(i));
            }
            var value = args.get(i + 1);

            fields.put(name, value);
        }
    }

    //-------------------------------------------------------------------------
    // Value Methods

    //**
    // @method matches
    // @args name,...
    // @result Boolean
    // Returns `true` if the *names* passed in match the names of the
    // defined fields, and false otherwise.
    //
    // The *names* may be passed as strings or keywords; the convention
    // is to use keywords.  Pass the keyword `#_` as a wildcard to
    // match any field name.
    //
    // For example,
    //
    // ```joe
    // var tuple = Tuple(#ok, 123);
    // ...
    // if (tuple.matches(#ok, #value)) {
    //     println("ok: " + tuple.value);
    // }
    // ```
    private static Object _matches(Tuple tuple, Joe joe, ArgQueue args) {
        for (var key : tuple.fields.keySet()) {
            if (!args.hasRemaining()) {
                return false;
            }

            var arg = toName(joe, args.next());
            if (arg.equals(ANY)) {
                continue;
            }

            if (!key.equals(arg)) {
                return false;
            }
        }

        return !args.hasRemaining();
    }

    private static String toName(Joe joe, Object arg) {
        return switch (arg) {
            case String s -> s;
            case Keyword k -> k.name();
            default -> throw joe.expected("field name", arg);
        };
    }

    //**
    // @method toString
    // @result String
    // Returns the tuple's string representation.
    private static Object _toString(Tuple tuple, Joe joe, ArgQueue args) {
        Joe.exactArity(args, 0, "toString()");

        return tuple.toString(joe);
    }

    //-------------------------------------------------------------------------
    // JoeObject API


    @Override
    public Object get(String name) {
        var value = fields.get(name);
        if (value != null) {
            return value;
        }

        var method = METHODS.get(name);

        if (method != null) {
            return new NativeMethod<>(this, name, method);
        }

        throw new JoeError("Unknown property: '" + name + "'");
    }

    @Override
    public void set(String name, Object value) {
        throw new JoeError("Tuples have no settable properties.");
    }

    //-------------------------------------------------------------------------
    // JoeToString API

    @Override
    public String toString(Joe joe) {
        var list = new ArrayList<String>();
        for (var e : fields.entrySet()) {
            list.add("#" + e.getKey());
            list.add(joe.codify(e.getValue()));
        }

        return "Tuple(" + String.join(", ", list) + ")";
    }

}
