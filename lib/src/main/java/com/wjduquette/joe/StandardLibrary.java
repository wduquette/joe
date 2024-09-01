package com.wjduquette.joe;

import java.util.List;

public class StandardLibrary extends Library {
    public static final StandardLibrary LIB = new StandardLibrary();
    public StandardLibrary() {
        super();

        globalFunction("catch",     this::_catch);
        globalFunction("codify",    this::_codify);
        globalFunction("stringify", this::_stringify);
        globalFunction("typeName",  this::_typeName);
    }

    //**
    // @global catch
    // @args callable
    // @returns error or null
    // Executes the callable, which must not require any arguments,
    // and returns `null` if the call succeeds and the error otherwise.
    //
    // This is a preliminary implementation of `catch`.  Ultimately
    // it needs to return a `Pair(ok,error)`.
    private Object _catch(Joe joe, List<Object> args) {
        Joe.exactArity(args, 1, "catch(callable)");
        var arg = args.get(0);

        if (arg instanceof JoeCallable callable) {
            try {
                callable.call(joe, List.of());
                return null;
            } catch (JoeError ex) {
                return ex;
            }
        } else {
            throw joe.expected("no-argument callable", arg);
        }
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
