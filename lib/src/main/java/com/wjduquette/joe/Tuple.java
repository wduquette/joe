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
    public static final Keyword ANY = new Keyword("_");

    private static final Map<String, JoeValueCallable<Tuple>> METHODS =
        new HashMap<>();

    static {
        METHODS.put("is",       Tuple::_is);
        METHODS.put("has",      Tuple::_has);
        METHODS.put("toString", Tuple::_toString);
    }

    /**
     * For use by Java code.  Creates a Tuple given name/value pairs
     * instead of keyword/value pairs.
     *
     * @param joe  The interpreter
     * @param args The pairs
     * @return The tuple
     */
    public static Tuple of(Joe joe, Object... args) {
        return new Tuple(joe, new Args(args));
    }

    //-------------------------------------------------------------------------
    // Instance Variables

    private final Map<Keyword, Object> fields = new LinkedHashMap<>();

    //-------------------------------------------------------------------------
    // Constructor

    //**
    // @package joe
    // @type Tuple
    // @init
    // @args keyword, value, [keyword, value]...
    // Given keyword and value pairs, returns an immutable `Tuple` object with
    // the fields named by the keywords assigned to the given values.
    //
    // For example,
    //
    // ```joe
    // var tuple = Tuple(#flag, true, #result, 123);
    //
    // println("flag   = " + tuple.flag);
    // println("result = " + tuple.result);
    // ```
    //
    // Tuples are intended as a result type for functions that need to return
    // multiple values, e.g., a status and a value or two values.
    //
    // A function may return tuples with different fields depending on the
    // circumstances.  The [[Tuple#method.is]] method is used to match
    // against the tuple's fields, and the [[Tuple#method.has]] method is
    // used to match against both fields and their values.
    //
    // ```joe
    // var tuple = Tuple(#flag, true, #result, 123);
    //
    // if (tuple.has(#flag, true, #result, #_)) {
    //     println("OK, got: " + tuple.result);
    // } else if (tuple.has(#flag, false, #error, #_)) {
    //     println("Error, got: " + tuple.error);
    // }
    // ```
    //
    // In case like this, the field name itself can convey the status.  The
    // function could return either `Tuple(#ok, result)` or
    // `Tuple(#error, error)`, resulting in code like this:
    //
    // ```joe
    // var tuple = Tuple(#ok, true);
    //
    // if (tuple.is(#ok)) {
    //     println("OK, got: " + tuple.ok);
    // } else if (tuple.is(#error)) {
    //     println("Error, got: " + tuple.error);
    // }
    // ```
    //
    // See the [[Tuple#method.is]] and [[Tuple#method.has]] methods for more.
    /**
     * The argument list is a flat list of keyword/value pairs.
     * @param joe The interpreter
     * @param args The name/value array
     */
    public Tuple(Joe joe, Args args) {
        if (args.size() % 2 != 0) {
            throw new JoeError("Tuple() expects an even number of arguments.");
        }

        for (int i = 0; i < args.size(); i += 2) {
            var keyword = joe.toKeyword(args.get(i));
            validateField(keyword.name());
            var value = args.get(i + 1);

            fields.put(keyword, value);
        }
    }

    private void validateField(String name) {
        if (METHODS.containsKey(name)) {
            throw new JoeError(
                "Invalid field #" + name +
                ": conflicts with the tuple's '" + name + "()' method.");
        }

        if (name.equals("_")) {
            throw new JoeError(
                "Invalid field #" + name +
                ": Tuple reserves #_ as a wildcard.");
        }
    }

    //-------------------------------------------------------------------------
    // Value Methods

    //**
    // @method is
    // @args keyword,...
    // @result Boolean
    //
    // Returns `true` if the number of *keywords* passed in match the number
    // of fields in the tuple, and each keyword has the same name as the
    // matching field, and `false` otherwise.
    //
    // The keyword `#_` is a wildcard; it will match any field.
    //
    // For example, the following creates a tuple with the field `#ok`
    // which has the value `123`, and then matches against it.
    //
    // ```joe
    // var tuple = Tuple(#ok, 123);
    // ...
    // if (tuple.is(#ok)) {
    //     println("ok: " + tuple.ok);
    // } else {
    //     println("No match.");
    // }
    // ```
    private static Object _is(Tuple tuple, Joe joe, Args args) {
        if (args.size() == tuple.fields.size()) {
            for (var key : tuple.fields.keySet()) {
                var arg = args.next();
                if (!arg.equals(ANY) && !key.equals(arg)) return false;
            }
            return true;
        } else {
            return false;
        }
    }

    //**
    // @method has
    // @args keyword, value, [keyword, value]...
    // @result Boolean
    //
    // Returns `true` if the number of *keyword*/*value* pairs matches the
    // number of fields in the tuple, and each field has the same name
    // and value as the matching *keyword/*value* pair.
    //
    // The keyword `#_` is a wildcard; it will match any field or value.
    //
    // For example, the following creates a tuple with the field `#ok`
    // which has the value `123`, and then matches against the field name
    // and its value.
    //
    // ```joe
    // var tuple = Tuple(#ok, 123);
    // ...
    // if (tuple.has(#ok, 123)) {
    //     println("ok: " + tuple.ok);
    // } else {
    //     println("No match.");
    // }
    // ```
    private static Object _has(Tuple tuple, Joe joe, Args args) {
        if (args.size() == 2*tuple.fields.size()) {
            for (var e : tuple.fields.entrySet()) {
                var keyArg = args.next();
                var valueArg = args.next();
                if (!keyArg.equals(ANY) && !e.getKey().equals(keyArg)) return false;
                if (!valueArg.equals(ANY) && !e.getValue().equals(valueArg)) return false;
            }
            return true;
        } else {
            return false;
        }
    }

    //**
    // @method toString
    // @result String
    // Returns the tuple's string representation.
    private static Object _toString(Tuple tuple, Joe joe, Args args) {
        Joe.exactArity(args, 0, "toString()");

        return tuple.toString(joe);
    }

    //-------------------------------------------------------------------------
    // JoeObject API


    @Override
    public String typeName() {
        return "Tuple";
    }

    @Override
    public Object get(String name) {
        var keyword = new Keyword(name);
        var value = fields.get(keyword);
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
            list.add(e.getKey().toString());
            list.add(joe.codify(e.getValue()));
        }

        return "Tuple(" + String.join(", ", list) + ")";
    }
}
