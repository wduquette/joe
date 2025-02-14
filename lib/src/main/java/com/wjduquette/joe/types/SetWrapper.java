package com.wjduquette.joe.types;

import com.wjduquette.joe.Joe;
import com.wjduquette.joe.JoeError;
import com.wjduquette.joe.JoeSet;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

/**
 * A wrapper for Java Sets that require special handling when used at
 * the script level.
 */
public class SetWrapper implements JoeSet {
    //-------------------------------------------------------------------------
    // Static Methods

    /**
     * Wraps a general set as a JoeSet.  Joe code can update the set with
     * any key/value pairs that are compatible with the keyType and valueType.
     * Attempts to add incompatible pairs will result in a JoeError.
     * @param joe The interpreter
     * @param set The set
     * @param valueType The value type
     * @return the JoeSet
     */
    public static SetWrapper wrap(
        Joe joe,
        Set<?> set,
        Class<?> valueType
    ) {
        return new SetWrapper(joe, set, valueType, false);
    }

    /**
     * Wraps a general set as a read-only JoeSet.
     * Attempts to modify the set will throw a JoeError.
     * @param joe The interpreter
     * @param set The general set
     * @return the JoeSet
     */
    public static SetWrapper readOnly(Joe joe, Set<?> set) {
        return new SetWrapper(joe, set, null, true);
    }

    //-------------------------------------------------------------------------
    // Instance Variables

    private final Joe joe;
    private final Set<Object> set;
    private final Class<?> valueType;
    private final boolean readOnly;

    //-------------------------------------------------------------------------
    // Constructor

    @SuppressWarnings("unchecked")
    private SetWrapper(
        Joe joe,
        Set<?> set,
        Class<?> valueType,
        boolean readOnly
    ) {
        this.joe = joe;
        this.set = (Set<Object>)set;
        this.valueType = valueType;
        this.readOnly = readOnly;
    }


    @Override
    public boolean add(Object value) {
        requireReadWrite();
        requireCanAdd(value);
        return set.add(value);
    }

    @Override
    public boolean addAll(Collection<?> other) {
        requireReadWrite();
        for (var e : other) {
            requireCanAdd(e);
        }
        return set.addAll(other);
    }


    @Override
    public int size() {
        return set.size();
    }

    @Override
    public boolean isEmpty() {
        return set.isEmpty();
    }

    @Override
    public Iterator<Object> iterator() {
        return set.iterator();
    }

    @Override
    public boolean contains(Object key) {
        return set.contains(key);
    }

    @Override
    public boolean remove(Object value) {
        requireReadWrite();
        return set.remove(value);
    }

    @Override
    public void clear() {
        requireReadWrite();
        set.clear();
    }

    @Override
    public Object[] toArray() {
        return set.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return set.toArray(a);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return set.containsAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        requireReadWrite();
        return set.retainAll(c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        requireReadWrite();
        return set.removeAll(c);
    }

    private void requireReadWrite() {
        if (readOnly) {
            throw new JoeError("Cannot modify; the set is read-only.");
        }
    }

    private void requireCanAdd(Object value) {
        if (valueType != null && !valueType.isAssignableFrom(value.getClass())) {
            throw joe.expected(
                "value of type " + joe.classTypeName(valueType), value);
        }
    }


    //-------------------------------------------------------------------------
    // Object API

    @Override
    public int hashCode() {
        return set.hashCode();
    }

    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    @Override
    public boolean equals(Object other) {
        return set.equals(other);
    }

    @Override
    public String toString() {
        return SetProxy.TYPE.stringify(joe, set);
    }
}
