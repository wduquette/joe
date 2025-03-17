package com.wjduquette.joe.types;

import com.wjduquette.joe.Args;
import com.wjduquette.joe.Joe;
import com.wjduquette.joe.JoeError;

public class CatchResultType extends RecordType<CatchResult> {
    public static final CatchResultType TYPE = new CatchResultType();

    //-------------------------------------------------------------------------
    // Constructor

    public CatchResultType() {
        super("CatchResult");

        //**
        // @package joe
        // @type CatchResult
        // The result type of the standard [[function.catch]] function.
        // The record has two fields, `result` and `error`.
        proxies(CatchResult.class);

        recordField("result", CatchResult::result);
        recordField("error",  CatchResult::error);

        staticMethod("ok",    this::_ok);
        staticMethod("error", this::_error);

        method("isOK",        this::_isOK);
        method("isError",     this::_isError);
    }

    //-------------------------------------------------------------------------
    // Static Method implementations

    //**
    // @static ok
    // @args result
    // @result CatchResult
    // Creates a new `CatchResult` indicating a successful result.
    // The *result* is the `result` value; the `error` field will
    // be null.
    private Object _ok(Joe joe, Args args) {
        args.exactArity(1, "CatchResult.ok(result)");
        return CatchResult.ok(args.next());
    }

    //**
    // @static error
    // @args error
    // @result CatchResult
    // Creates a new `CatchResult` indicating a failure.  The
    // *error* is the specific error, either a [[String]] or
    // an [[Error]].  The `CatchResult`'s `error` field will be
    // set to the *error*, and its `result` field will be null.
    private Object _error(Joe joe, Args args) {
        args.exactArity(1, "CatchResult.error(error)");
        var arg = args.next();
        if (arg instanceof String s) {
            return CatchResult.error(new JoeError(s));
        } else if (arg instanceof JoeError e) {
            return CatchResult.error(e);
        } else {
            throw joe.expected("Error or String", arg);
        }
    }

    //-------------------------------------------------------------------------
    // Instance Method Implementations

    //**
    // @method isOK
    // @result Boolean
    // Returns true if the `CatchResult` represents a success
    // and false if it represents an error.
    private Object _isOK(CatchResult value, Joe joe, Args args) {
        return value.isOK();
    }

    //**
    // @method isError
    // @result Boolean
    // Returns true if the `CatchResult` represents an error
    // and false if it represents a success.
    private Object _isError(CatchResult value, Joe joe, Args args) {
        return value.isError();
    }
}
