package com.wjduquette.joe.types;

import com.wjduquette.joe.JoeMap;

import java.util.HashMap;
import java.util.Map;

/**
 * The standard Map class for maps created in Joe code.
 */
public class MapValue extends HashMap<Object,Object> implements JoeMap {
    /**
     * Creates an empty map.
     */
    public MapValue() {
        // Nothing to do.
    }

    /**
     * Creates a new map with the content of the other map.
     * @param other The other map
     */
    public MapValue(Map<?,?> other) {
        putAll(other);
    }
}
