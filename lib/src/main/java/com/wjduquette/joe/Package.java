package com.wjduquette.joe;

import java.util.ArrayList;
import java.util.List;

/**
 * A package containing Joe functions and or types, for installation into
 * a Joe interpreter.
 */
public class Package {
    //-------------------------------------------------------------------------
    // Instance Variables

    private final String name;

    private final List<NativeFunction> globalFunctions = new ArrayList<>();
    private final List<TypeProxy<?>> types = new ArrayList<>();

    //-------------------------------------------------------------------------
    // Constructor

    public Package(String name) {
        this.name = name;
    }

    //-------------------------------------------------------------------------
    // Package builders

    public final void globalFunction(String name, JoeCallable callable) {
        globalFunctions.add(new NativeFunction(name, callable));
    }

    public final void type(TypeProxy<?> typeProxy) {
        types.add(typeProxy);
    }

    /**
     * Installs the package's native functions and types into the
     * engine.
     * @param joe The engine
     */
    public final void install(Joe joe) {
        globalFunctions.forEach(joe::installGlobalFunction);
        types.forEach(joe::installType);
    }

    //-------------------------------------------------------------------------
    // Accessors

    @SuppressWarnings("unused")
    public String name() {
        return name;
    }
}
