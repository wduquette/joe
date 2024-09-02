package com.wjduquette.joe;

import java.util.ArrayList;
import java.util.List;

public class Library {
    //-------------------------------------------------------------------------
    // Instance Variables

    private final List<NativeFunction> globalFunctions = new ArrayList<>();

    //-------------------------------------------------------------------------
    // Constructor

    public Library() {
        // nothing to do yet
    }

    //-------------------------------------------------------------------------
    // Library builders

    protected final void globalFunction(String name, JoeCallable callable) {
        globalFunctions.add(new NativeFunction(name, callable));
    }

    /**
     * Installs the library's native functions and types into the
     * engine.
     * @param joe The engine
     */
    public final void install(Joe joe) {
        globalFunctions.forEach(joe::installGlobalFunction);
    }
}
