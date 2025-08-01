package com.wjduquette.joe.parser;

import com.wjduquette.joe.scanner.Scanner;
import com.wjduquette.joe.scanner.Token;

class EmbeddedParser {
    //-------------------------------------------------------------------------
    // Instance Variables

    protected final Parser parent;
    protected final Scanner scanner;

    //-------------------------------------------------------------------------
    // Constructor

    public EmbeddedParser(Parser parent) {
        this.parent = parent;
        this.scanner = parent.scanner();
    }

    //-------------------------------------------------------------------------
    // Protected API

    // Reports the error without synchronization.  Use this for correct syntax
    // but bad semantics.
    protected void error(Token token, String message) {
        parent.error(token, message);
    }

    // Reports the error and throws an ErrorSync exception.  Use this for
    // bad syntax where synchronization is needed.
    protected Parser.ErrorSync errorSync(Token token, String message) {
        return parent.errorSync(token, message);
    }
}
