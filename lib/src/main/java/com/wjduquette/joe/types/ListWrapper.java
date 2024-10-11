package com.wjduquette.joe.types;

import com.wjduquette.joe.Joe;
import com.wjduquette.joe.JoeError;
import com.wjduquette.joe.JoeList;

import java.util.*;

/**
 * A wrapper for lists created by a Java application or library that must
 * be passed to Joe code.
 *
 * <p>Use ListWrapper.readOnly() to pass a list that should not be
 * modified at the script level.</p>
 *
 * <p>Use ListWrapper.wrap() to pass a list that can be
 * modified at the script level, controlling that additions are assignable
 * to the expected item type.</p>
 */
@SuppressWarnings("unused")
public class ListWrapper implements JoeList {
    //-------------------------------------------------------------------------
    // Static Methods

    /**
     * Wraps a general list as a JoeList.  Joe code can update the list with
     * any value that is assignable to the given itemType.
     * Attempts to add items that are not assignable to the itemType will
     * throw a JoeError.
     * @param joe The interpreter
     * @param list The list
     * @param itemType the item type
     * @return the JoeList
     */
    public static ListWrapper wrap(Joe joe, List<?> list, Class<?> itemType) {
        return new ListWrapper(joe, list, itemType, false);
    }

    /**
     * Wraps a general list as a read-only JoeList.
     * Attempts to modify the list will throw a JoeError.
     * @param joe The interpreter
     * @param list The list
     * @return the JoeList
     */
    public static ListWrapper readOnly(Joe joe, List<?> list) {
        return new ListWrapper(joe, list, null, true);
    }

    //-------------------------------------------------------------------------
    // Instance Variables

    private final Joe joe;
    private final List<Object> list;
    private final Class<?> itemType;
    private final boolean readOnly;

    //-------------------------------------------------------------------------
    // Constructor

    @SuppressWarnings("unchecked")
    private ListWrapper(
        Joe joe,
        List<?> list,
        Class<?> itemType,
        boolean readOnly
    ) {
        this.joe = joe;
        this.list = (List<Object>)list;
        this.itemType = itemType;
        this.readOnly = readOnly;
    }

    //-------------------------------------------------------------------------
    // Methods


    @Override
    public int size() {
        return list.size();
    }

    @Override
    public boolean isEmpty() {
        return list.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return list.contains(o);
    }

    @Override
    public Iterator<Object> iterator() {
        return list.iterator();
    }

    @Override
    public Object[] toArray() {
        return list.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return list.toArray(a);
    }

    @Override
    public boolean add(Object o) {
        System.out.println("add: " + o);
        requireReadWrite();
        System.out.println("type check: " + itemType);
        if (itemType != null) requireCanAdd(o);

        System.out.println("adding to: " + list);
        return list.add(o);
    }

    @Override
    public boolean remove(Object o) {
        requireReadWrite();
        return list.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return new HashSet<>(list).containsAll(c);
    }

    @Override
    public boolean addAll(Collection<?> c) {
        requireReadWrite();
        if (itemType != null) c.forEach(this::requireCanAdd);
        return list.addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection<?> c) {
        requireReadWrite();
        if (itemType != null) c.forEach(this::requireCanAdd);
        return list.addAll(index, c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        requireReadWrite();
        return list.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        requireReadWrite();
        return list.retainAll(c);
    }

    @Override
    public void clear() {
        requireReadWrite();
        list.clear();
    }

    @Override
    public Object get(int index) {
        return list.get(index);
    }

    @Override
    public Object set(int index, Object element) {
        requireReadWrite();
        if (itemType != null) requireCanAdd(element);
        return list.set(index, element);
    }

    @Override
    public void add(int index, Object element) {
        requireReadWrite();
        if (itemType != null) requireCanAdd(element);
        list.add(index, element);
    }

    @Override
    public Object remove(int index) {
        requireReadWrite();
        return list.remove(index);
    }

    @Override
    public int indexOf(Object o) {
        return list.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return list.lastIndexOf(o);
    }

    @Override
    public ListIterator<Object> listIterator() {
        return list.listIterator();
    }

    @Override
    public ListIterator<Object> listIterator(int index) {
        return list.listIterator(index);
    }

    @Override
    public List<Object> subList(int fromIndex, int toIndex) {
        return new ListValue(list.subList(fromIndex, toIndex));
    }

    private void requireReadWrite() {
        if (readOnly) {
            throw new JoeError("Cannot modify; list is read-only.");
        }
    }

    private void requireCanAdd(Object value) {
        if (itemType != null && !itemType.isAssignableFrom(value.getClass())) {
            throw joe.expected(joe.classTypeName(itemType), value);
        }
    }

    //-------------------------------------------------------------------------
    // Object API

    @Override
    public int hashCode() {
        return list.hashCode();
    }

    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    @Override
    public boolean equals(Object other) {
        return list.equals(other);
    }

    @Override
    public String toString() {
        return ListProxy.TYPE.stringify(joe, list);
    }
}
