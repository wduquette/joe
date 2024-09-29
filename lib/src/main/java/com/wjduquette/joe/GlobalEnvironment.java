package com.wjduquette.joe;

import java.util.ArrayList;
import java.util.List;

/**
 * A {@link Joe} interpreter's global environment, containing all
 * global variables, functions, types, etc.
 */
public class GlobalEnvironment extends Environment {
    GlobalEnvironment() {
        super();
    }

    /**
     * Gets the names of the variables declared in the global environment.
     * @return The list.
     */
    public List<String> getVarNames() {
        return new ArrayList<>(values.keySet());
    }
}
