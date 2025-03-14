package com.wjduquette.joe.types;

import com.wjduquette.joe.Joe;
import com.wjduquette.joe.JoeError;
import com.wjduquette.joe.JoeMap;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * A wrapper for Java Maps that require special handling when used by
 * scripts.
 */
public class MapWrapper implements JoeMap {
    //-------------------------------------------------------------------------
    // Static Methods

    /**
     * Wraps a general map as a JoeMap.  Joe code can update the map with
     * any key/value pairs that are compatible with the keyType and valueType.
     * Attempts to add incompatible pairs will result in a JoeError.
     * @param joe The interpreter
     * @param map The map
     * @param keyType The key type
     * @param valueType The value type
     * @return the JoeMap
     */
    public static MapWrapper wrap(
        Joe joe,
        Map<?,?> map,
        Class<?> keyType,
        Class<?> valueType
    ) {
        return new MapWrapper(joe, map, keyType, valueType, false);
    }

    /**
     * Wraps a general map as a read-only JoeMap.
     * Attempts to modify the map will throw a JoeError.
     * @param joe The interpreter
     * @param map The general map
     * @return the JoeMap
     */
    public static MapWrapper readOnly(Joe joe, Map<?,?> map) {
        return new MapWrapper(joe, map, null, null, true);
    }

    //-------------------------------------------------------------------------
    // Instance Variables

    private final Joe joe;
    private final Map<Object,Object> map;
    private final Class<?> keyType;
    private final Class<?> valueType;
    private final boolean readOnly;

    //-------------------------------------------------------------------------
    // Constructor

    @SuppressWarnings("unchecked")
    private MapWrapper(
        Joe joe,
        Map<?,?> map,
        Class<?> keyType,
        Class<?> valueType,
        boolean readOnly
    ) {
        this.joe = joe;
        this.map = (Map<Object,Object>)map;
        this.keyType = keyType;
        this.valueType = valueType;
        this.readOnly = readOnly;
    }


    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    @Override
    public Object get(Object key) {
        return map.get(key);
    }

    @Override
    public Object put(Object key, Object value) {
        requireReadWrite();
        requireCanPut(key, value);
        return map.put(key, value);
    }

    @Override
    public Object remove(Object key) {
        requireReadWrite();
        return map.remove(key);
    }

    @Override
    public void putAll(Map<?, ?> other) {
        requireReadWrite();
        for (var e : other.entrySet()) {
            requireCanPut(e.getKey(), e.getValue());
        }
        map.putAll(other);
    }

    @Override
    public void clear() {
        requireReadWrite();
        map.clear();
    }

    @Override
    public Set<Object> keySet() {
        return map.keySet();
    }

    @Override
    public Collection<Object> values() {
        return map.values();
    }

    @Override
    public Set<Entry<Object, Object>> entrySet() {
        return map.entrySet();
    }

    private void requireReadWrite() {
        if (readOnly) {
            throw new JoeError("Cannot modify; the map is read-only.");
        }
    }

    private void requireCanPut(Object key, Object value) {
        if (keyType != null && !keyType.isAssignableFrom(key.getClass())) {
            throw joe.expected(
                "key of type " + joe.classTypeName(keyType), key);
        }
        if (valueType != null && !valueType.isAssignableFrom(value.getClass())) {
            throw joe.expected(
                "value of type " + joe.classTypeName(valueType), value);
        }
    }


    //-------------------------------------------------------------------------
    // Object API

    @Override
    public int hashCode() {
        return map.hashCode();
    }

    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    @Override
    public boolean equals(Object other) {
        return map.equals(other);
    }

    @Override
    public String toString() {
        return MapType.TYPE.stringify(joe, map);
    }
}
