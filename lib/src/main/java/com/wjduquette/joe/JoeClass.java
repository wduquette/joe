package com.wjduquette.joe;

import java.util.List;
import java.util.Map;

public class JoeClass implements JoeCallable {
    public static final String INIT = "init";
    private final String name;
    private final JoeClass superclass;
    private final Map<String, JoeFunction> methods;

    /**
     * Creates a new class.
     * @param name The class's variable name.
     * @param superclass The superclass, or null
     * @param methods The map of methods by name
     */
    JoeClass(
        String name,
        JoeClass superclass,
        Map<String, JoeFunction> methods
    ) {
        this.name = name;
        this.superclass = superclass;
        this.methods = methods;
    }

    public String name() {
        return name;
    }

    JoeFunction findMethod(String name) {
        if (methods.containsKey(name)) {
            return methods.get(name);
        }

        if (superclass != null) {
            return superclass.findMethod(name);
        }

        return null;
    }

    @Override
    public Object call(Interpreter interp, List<Object> args) {
        JoeInstance instance = new JoeInstance(this);
        JoeFunction initializer = findMethod(INIT);
        if (initializer != null) {
            initializer.bind(instance).call(interp, args);
        }
        return instance;
    }

    @Override
    public String toString() {
        return "<class " + name + ">";
    }
}
