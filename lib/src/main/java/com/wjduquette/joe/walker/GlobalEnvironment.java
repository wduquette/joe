package com.wjduquette.joe.walker;

import com.wjduquette.joe.Joe;

import java.util.Collections;
import java.util.Set;

/**
 * A {@link Joe} interpreter's global environment, containing all
 * global variables, functions, types, etc.
 */
class GlobalEnvironment extends Environment {
    public GlobalEnvironment() {
        super();
    }

    /**
     * Gets the value of the named global variable.
     * @param name The name
     * @return The value
     */
    public Object getVar(String name) {
        return values.get(name);
    }


    /**
     * Gets the names of the variables declared in the global environment.
     * @return The set.
     */
    public Set<String> getVarNames() {
        return Collections.unmodifiableSet(values.keySet());
    }
}
