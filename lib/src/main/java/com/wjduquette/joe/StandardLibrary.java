package com.wjduquette.joe;

import com.wjduquette.joe.types.ErrorProxy;
import com.wjduquette.joe.types.KeywordProxy;
import com.wjduquette.joe.types.PairProxy;
import com.wjduquette.joe.types.StringProxy;

import java.util.List;

public class StandardLibrary extends Library {
    public static final StandardLibrary LIB = new StandardLibrary();
    public StandardLibrary() {
        super();

        globalFunction("catch",     this::_catch);
        globalFunction("codify",    this::_codify);
        globalFunction("print",     this::_print);
        globalFunction("println",   this::_println);
        globalFunction("stringify", this::_stringify);
        globalFunction("typeName",  this::_typeName);

        type(ErrorProxy.TYPE);
        type(KeywordProxy.TYPE);
        type(PairProxy.TYPE);
        type(StringProxy.TYPE);
    }

    //**
    // @global catch
    // @args callable
    // @returns Pair(result, error)
    // Executes the callable, which must not require any arguments.
    // and returns `null` if the call succeeds and the error otherwise.
    //
    // Returns `Pair(result, null)` on success and
    // `Pair(null, Error)` on error.
    private Object _catch(Joe joe, List<Object> args) {
        Joe.exactArity(args, 1, "catch(callable)");
        var arg = args.get(0);

        if (arg instanceof JoeCallable callable) {
            try {
                var result = callable.call(joe, List.of());
                return new Pair(result, null);
            } catch (JoeError ex) {
                return new Pair(null, ex);
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
