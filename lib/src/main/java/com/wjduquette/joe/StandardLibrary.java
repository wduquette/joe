package com.wjduquette.joe;

import com.wjduquette.joe.types.*;

class StandardLibrary extends JoePackage {
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
        globalFunction("compare",   this::_compare);
        globalFunction("print",     this::_print);
        globalFunction("printf",    this::_printf);
        globalFunction("println",   this::_println);
        globalFunction("stringify", this::_stringify);
        globalFunction("typeName",  this::_typeName);

        // Documented in Tuple.java.
        globalFunction("Tuple", Tuple::new);

        type(AssertErrorProxy.TYPE);
        type(BooleanProxy.TYPE);
        type(ErrorProxy.TYPE);
        type(KeywordProxy.TYPE);
        type(ListProxy.TYPE);
        type(MapProxy.TYPE);
        type(NumberProxy.TYPE);
        type(SetProxy.TYPE);
        type(StringProxy.TYPE);
        type(TextBuilderProxy.TYPE);
    }

    //**
    // @packageTopic joe
    // @title Why "Joe"?
    // My friend Joe's a straight shooter, and never misses his mark.

    //**
    // @function catch
    // @args callable
    // @result Tuple
    // Executes the callable, which must not require any arguments.
    // Returns `Tuple(#ok, returnValue)` on success and
    // `Tuple(#error, Error)` on error.
    private Object _catch(Joe joe, Args args) {
        args.exactArity(1, "catch(callable)");

        try {
            var result = joe.call(args.next());
            return Tuple.of(joe, OK, result);
        } catch (JoeError ex) {
            ex.setPendingContext(null);
            ex.addInfo("Called from catch()");
            return Tuple.of(joe, ERROR, ex);
        }
    }

    //**
    // @function compare
    // @args a, b
    // @result Number
    //
    // Given two strings or two numbers *a* and *b*, returns -1, 0,
    // or 1 as *a* < *b*, *a* == *b*, or *a* > *b*.  This function
    // is useful when sorting collections.
    private Object _compare(Joe joe, Args args) {
        args.exactArity(2, "compare(a, b)");
        return (double)Joe.compare(args.next(), args.next());
    }

    //**
    // @function print
    // @args text
    // Prints its text to standard output (which might be
    // redirected by the application).
    private Object _print(Joe joe, Args args) {
        args.exactArity(1, "print(text)");

        joe.print(joe.stringify(args.next()));
        return null;
    }

    //**
    // @function printf
    // @args fmt, [values...]
    // Formats its arguments given the *fmt* string, and prints the result
    // to standard output (which might be redirected by the application).
    //
    // See [[String#topic.formatting]] for the format
    // string syntax.
    private Object _printf(Joe joe, Args args) {
        args.minArity(1, "printf(fmt, [values]...)");
        var fmt = joe.toString(args.next());

        joe.print(StringFormatter.format(joe, fmt, args.remainderAsList()));
        return null;
    }

    //**
    // @function println
    // @args [text]
    // Prints its text followed by a line separator to standard output
    // (which might be redirected by the application).
    private Object _println(Joe joe, Args args) {
        args.arityRange(0, 1, "println([text])");

        if (!args.hasNext()) {
            joe.println();
        } else {
            joe.println(joe.stringify(args.next(0)));
        }
        return null;
    }

    //**
    // @function stringify
    // @args value
    // @result String
    // Converts its value to a string for output.  This function
    // is functionally equivalent to [[String#init]].
    private Object _stringify(Joe joe, Args args) {
        args.exactArity(1, "stringify(value)");

        return joe.stringify(args.next(0));
    }

    //**
    // @function typeName
    // @args value
    // @result String
    // Returns the name of the value's type.
    private Object _typeName(Joe joe, Args args) {
        args.exactArity(1, "typeName(value)");

        return joe.typeName(args.next(0));
    }
}
