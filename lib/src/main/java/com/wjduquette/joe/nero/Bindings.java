package com.wjduquette.joe.nero;

import java.util.HashMap;
import java.util.Map;

public class Bindings extends HashMap<Variable,Object> {
    public Bindings() {
        super();
    }

    public Bindings(Map<Variable,Object> other) {
        super(other);
    }
}
