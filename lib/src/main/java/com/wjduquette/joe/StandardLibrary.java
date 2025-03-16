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
        globalFunction("print",     this::_print);
        globalFunction("printf",    this::_printf);
        globalFunction("println",   this::_println);

        // Documented in Tuple.java.
        globalFunction("Tuple", Tuple::new);

        type(AssertErrorType.TYPE);
        type(BooleanType.TYPE);
        type(CatchResultType.TYPE);
        type(ErrorType.TYPE);
        type(JoeSingleton.TYPE);
        type(KeywordType.TYPE);
        type(ListType.TYPE);
        type(MapType.TYPE);
        type(NumberType.TYPE);
        type(SetType.TYPE);
        type(StringType.TYPE);
        type(TextBuilderClass.TYPE);
        type(TypeType.TYPE);
    }

    //**
    // @packageTopic joe
    // @title Why "Joe"?
    // My friend Joe's a straight shooter, and never misses his mark.

    //**
    // @function catch
    // @args callable
    // @result CatchResult
    // Executes the callable, which must not require any arguments.
    // Returns a [[CatchResult]] indicating success or failure and providing
    // the returned result or the error message respectively.
    private Object _catch(Joe joe, Args args) {
        args.exactArity(1, "catch(callable)");

        try {
            var result = joe.call(args.next());
            return CatchResult.ok(result);
        } catch (JoeError ex) {
            ex.setPendingContext(null);
            ex.addInfo("Called from catch()");
            return CatchResult.error(ex);
        }
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
}
