package com.wjduquette.joe;

import com.wjduquette.joe.types.*;

public class StandardLibrary extends Package {
    public static final StandardLibrary PACKAGE = new StandardLibrary();
    public static final Keyword OK = new Keyword("ok");
    public static final Keyword ERROR = new Keyword("error");

    public StandardLibrary() {
        super("joe");

        //**
        // @package joe
        // @title Joe Standard Library
        // The `joe` package contains Joe's standard library.
        globalFunction("catch",     this::_catch);
        globalFunction("codify",    this::_codify);
        globalFunction("compare",   this::_compare);
        globalFunction("print",     this::_print);
        globalFunction("println",   this::_println);
        globalFunction("stringify", this::_stringify);
        globalFunction("typeName",  this::_typeName);

        type(BooleanProxy.TYPE);
        type(ErrorProxy.TYPE);
        type(KeywordProxy.TYPE);
        type(ListProxy.TYPE);
        type(NumberProxy.TYPE);
        type(PairProxy.TYPE);
        type(StringProxy.TYPE);
    }

    //**
    // @function catch
    // @args callable
    // @result Pair
    // Executes the callable, which must not require any arguments.
    // Returns `Pair(#ok, returnValue)` on success and
    // `Pair(#error, Error)` on error.
    private Object _catch(Joe joe, ArgQueue args) {
        Joe.exactArity(args, 1, "catch(callable)");
        var callable = args.getRemaining(0);

        try {
            var result = joe.call(callable);
            return new Pair(OK, result);
        } catch (JoeError ex) {
            return new Pair(ERROR, ex);
        }
    }

    private Object _codify(Joe joe, ArgQueue args) {
        Joe.exactArity(args, 1, "codify(value)");

        return joe.codify(args.getRemaining(0));
    }

    //**
    // @function compare
    // @args a, b
    // @result Number
    //
    // Given two strings or two numbers *a* and *b*, returns -1, 0,
    // or 1 as *a* < *b*, *a* == *b*, or *a* > *b*.  This function
    // is useful when sorting collections.
    private Object _compare(Joe joe, ArgQueue args) {
        Joe.exactArity(args, 2, "compare(a, b)");
        return (double)Joe.compare(args.next(), args.next());
    }

    //**
    // @function print
    // @args text
    // Prints its text to standard output (which might be
    // redirected by the application).
    private Object _print(Joe joe, ArgQueue args) {
        Joe.exactArity(args, 1, "print(text)");

        joe.print(joe.stringify(args.getRemaining(0)));
        return null;
    }

    //**
    // @function println
    // @args [text]
    // Prints its text followed by a line separator to standard output
    // (which might be redirected by the application).
    private Object _println(Joe joe, ArgQueue args) {
        Joe.arityRange(args, 0, 1, "println([text])");

        if (args.hasRemaining()) {
            joe.println();
        } else {
            joe.println(joe.stringify(args.getRemaining(0)));
        }
        return null;
    }

    //**
    // @function stringify
    // @args value
    // @result String
    // Converts its value to a string for output.  This function
    // is functionally equivalent to [[String#init]].
    private Object _stringify(Joe joe, ArgQueue args) {
        Joe.exactArity(args, 1, "stringify(value)");

        return joe.stringify(args.getRemaining(0));
    }

    //**
    // @function typeName
    // @args value
    // @result String
    // Returns the name of the value's type.
    private Object _typeName(Joe joe, ArgQueue args) {
        Joe.exactArity(args, 1, "typeName(value)");

        return joe.typeName(args.getRemaining(0));
    }
}
