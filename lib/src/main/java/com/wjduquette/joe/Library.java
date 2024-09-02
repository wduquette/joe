package com.wjduquette.joe;

import java.util.ArrayList;
import java.util.List;

public class Library {
    //-------------------------------------------------------------------------
    // Instance Variables

    private final List<NativeFunction> globalFunctions = new ArrayList<>();
    private final List<TypeProxy<?>> types = new ArrayList<>();

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

    protected final void type(TypeProxy<?> typeProxy) {
        types.add(typeProxy);
    }

    /**
     * Installs the library's native functions and types into the
     * engine.
     * @param joe The engine
     */
    public final void install(Joe joe) {
        globalFunctions.forEach(joe::installGlobalFunction);
        types.forEach(joe::installType);
    }
}
