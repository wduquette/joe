package com.wjduquette.joe.types;

import com.wjduquette.joe.Joe;
import com.wjduquette.joe.Pair;
import com.wjduquette.joe.TypeProxy;

import java.util.List;

public class PairProxy extends TypeProxy<Pair> {
    public static final PairProxy TYPE = new PairProxy();

    //-------------------------------------------------------------------------
    // Constructor

    public PairProxy() {
        super("Pair");

        proxies(Pair.class);

        initializer(this::_init);

        method("left",  this::_left);
        method("right", this::_right);
    }

    //-------------------------------------------------------------------------
    // Overridable methods

    @Override
    public String stringify(Joe joe, Pair value) {
        return "Pair("
            + joe.stringify(value.left())
            + ", "
            + joe.stringify(value.right())
            + ")";
    }

    @Override
    public String codify(Joe joe, Pair value) {
        return "Pair("
            + joe.codify(value.left())
            + ", "
            + joe.codify(value.right())
            + ")";
    }

    //-------------------------------------------------------------------------
    // Initializer

    public Object _init(Joe joe, List<Object> args) {
        Joe.exactArity(args, 2, "Pair(left, right)");
        return new Pair(args.get(0), args.get(1));
    }

    //-------------------------------------------------------------------------
    // Method Implementations

    //**
    // @method left
    // @returns The left-hand value
    // Gets the first value in the pair.
    private Object _left(Pair value, Joe joe, List<Object> args) {
        Joe.exactArity(args, 0, "left()");
        return value.left();
    }

    //**
    // @method right
    // @returns The right-hand value
    // Gets the second value in the pair.
    private Object _right(Pair value, Joe joe, List<Object> args) {
        Joe.exactArity(args, 0, "right()");
        return value.right();
    }
}
