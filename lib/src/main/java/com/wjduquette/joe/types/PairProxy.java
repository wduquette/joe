package com.wjduquette.joe.types;

import com.wjduquette.joe.ArgQueue;
import com.wjduquette.joe.Joe;
import com.wjduquette.joe.Pair;
import com.wjduquette.joe.TypeProxy;

public class PairProxy extends TypeProxy<Pair> {
    public static final PairProxy TYPE = new PairProxy();

    //-------------------------------------------------------------------------
    // Constructor

    public PairProxy() {
        super("Pair");

        //**
        // @package joe
        // @type Pair
        // The `Pair` type represents a pair of values.  It is used to return
        // two values from a function or method.
        proxies(Pair.class);

        initializer(this::_init);

        method("left",  this::_left);
        method("right", this::_right);
        method("toString", this::_toString);
    }

    //-------------------------------------------------------------------------
    // Overridable methods

    @Override
    public String stringify(Joe joe, Object value) {
        assert value instanceof Pair;
        var pair = (Pair)value;
        return "Pair("
            + joe.stringify(pair.left())
            + ", "
            + joe.stringify(pair.right())
            + ")";
    }

    @Override
    public String codify(Joe joe, Object value) {
        assert value instanceof Pair;
        var pair = (Pair)value;
        return "Pair("
            + joe.codify(pair.left())
            + ", "
            + joe.codify(pair.right())
            + ")";
    }

    //-------------------------------------------------------------------------
    // Initializer

    public Object _init(Joe joe, ArgQueue args) {
        Joe.exactArity(args, 2, "Pair(left, right)");
        return new Pair(args.get(0), args.get(1));
    }

    //-------------------------------------------------------------------------
    // Method Implementations

    //**
    // @method left
    // @result value
    // Gets the first value in the pair.
    private Object _left(Pair value, Joe joe, ArgQueue args) {
        Joe.exactArity(args, 0, "left()");
        return value.left();
    }

    //**
    // @method right
    // @result value
    // Gets the second value in the pair.
    private Object _right(Pair value, Joe joe, ArgQueue args) {
        Joe.exactArity(args, 0, "right()");
        return value.right();
    }

    private Object _toString(Pair value, Joe joe, ArgQueue args) {
        Joe.exactArity(args, 0, "toString");
        return joe.stringify(value);
    }
}
