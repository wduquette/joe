package com.wjduquette.joe;

import com.wjduquette.joe.types.*;

import java.util.List;

public class StandardLibrary extends Library {
    public static final StandardLibrary LIB = new StandardLibrary();
    public static final Keyword OK = new Keyword("ok");
    public static final Keyword ERROR = new Keyword("error");

    public StandardLibrary() {
        super();

        globalFunction("catch",     this::_catch);
        globalFunction("codify",    this::_codify);
        globalFunction("print",     this::_print);
        globalFunction("println",   this::_println);
        globalFunction("stringify", this::_stringify);
        globalFunction("typeName",  this::_typeName);

        type(DoubleProxy.TYPE);
        type(ErrorProxy.TYPE);
        type(KeywordProxy.TYPE);
        type(PairProxy.TYPE);
        type(StringProxy.TYPE);
    }

    //**
    // @global catch
    // @args callable
    // @returns Pair
    // Executes the callable, which must not require any arguments.
    // Returns `Pair(#ok, returnValue)` on success and
    // `Pair(#error, Error)` on error.
    private Object _catch(Joe joe, List<Object> args) {
        Joe.exactArity(args, 1, "catch(callable)");
        var arg = args.get(0);

        if (arg instanceof JoeCallable callable) {
            try {
                var result = callable.call(joe, List.of());
                return new Pair(OK, result);
            } catch (JoeError ex) {
                return new Pair(ERROR, ex);
            }
        } else {
            throw joe.expected("no-argument callable", arg);
        }
    }

    private Object _codify(Joe joe, List<Object> args) {
        Joe.exactArity(args, 1, "codify(value)");

        return joe.codify(args.get(0));
    }

    private Object _print(Joe joe, List<Object> args) {
        Joe.exactArity(args, 1, "print(text)");

        System.out.print(joe.stringify(args.get(0)));
        return null;
    }

    private Object _println(Joe joe, List<Object> args) {
        Joe.arityRange(args, 0, 1, "println([text])");

        if (args.isEmpty()) {
            System.out.println();
        } else {
            System.out.println(joe.stringify(args.get(0)));
        }
        return null;
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
