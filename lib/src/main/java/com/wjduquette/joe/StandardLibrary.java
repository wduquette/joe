package com.wjduquette.joe;

import java.util.List;

public class StandardLibrary extends Library {
    public static final StandardLibrary LIB = new StandardLibrary();
    public StandardLibrary() {
        super();

        globalFunction("codify",    this::_codify);
        globalFunction("stringify", this::_stringify);
        globalFunction("typeName",  this::_typeName);
    }

    private Object _codify(Joe joe, List<Object> args) {
        Joe.exactArity(args, 1, "codify(value)");

        return joe.codify(args.get(0));
    }

    private Object _stringify(Joe joe, List<Object> args) {
        Joe.exactArity(args, 1, "stringify(value)");

        return joe.stringify(args.get(0));
    }

    private Object _typeName(Joe joe, List<Object> args) {
        Joe.exactArity(args, 1, "typeName(value)");

        return joe.typeName(args.get(0));
    }
}
