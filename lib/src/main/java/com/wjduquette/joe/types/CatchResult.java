package com.wjduquette.joe.types;

import com.wjduquette.joe.JoeError;

public class CatchResult extends RecordValue {
    //-------------------------------------------------------------------------
    // Instance Variables

    private final Object result;
    private final JoeError error;

    //-------------------------------------------------------------------------
    // Constructor

    private CatchResult(Object result, JoeError error) {
        super(CatchResultType.TYPE, result, error);
        this.result = result;
        this.error = error;
    }

    //-------------------------------------------------------------------------
    // Java API

    public static CatchResult ok(Object result) {
        return new CatchResult(result, null);
    }

    public static CatchResult error(JoeError error) {
        return new CatchResult(null, error);
    }

    public boolean isOK() {
        return error == null;
    }

    public boolean isError() {
        return error != null;
    }

    public Object result() {
        return result;
    }

    public JoeError error() {
        return error;
    }
}
