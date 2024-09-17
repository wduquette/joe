package com.wjduquette.joe.types;

import com.wjduquette.joe.ArgQueue;
import com.wjduquette.joe.Joe;
import com.wjduquette.joe.JoeList;
import com.wjduquette.joe.TypeProxy;

import java.util.Collection;
import java.util.HashSet;

public class ListProxy extends TypeProxy<JoeList> {
    public static final ListProxy TYPE = new ListProxy();

    //-------------------------------------------------------------------------
    // Constructor

    //**
    // @package joe
    // @type List
    // A Joe `List` is a Java `List`, roughly equivalent to a Java `ArrayList`.
    // Lists created using the [[List#init]] initializer can contain any kind
    // of Joe value; the list need not be homogeneous.  Lists received from
    // Java code might be read-only or require a specific item type.
    public ListProxy() {
        super("List");
        proxies(ListValue.class); // Types that implement `JoeList`
        initializer(this::_init);

        method("add",         this::_add);
        method("addAll",      this::_addAll);
        method("clear",       this::_clear);
        method("contains",    this::_contains);
        method("containsAll", this::_containsAll);
        method("copy",        this::_copy);
        // filter
        // get
        // getFirst
        // getLast
        // indexOf
        // isEmpty
        // lastIndexOf
        // map
        // remove
        // removeAt
        // removeAll
        // removeFirst
        // removeLast
        // reversed
        // set
        // sort -- need comparison infrastructure
        // subList
        method("size",        this::_size);
    }

    //-------------------------------------------------------------------------
    // Initializer Implementation

    //**
    // @init
    // @args values...
    // Creates a `List` of the argument values.
    private Object _init(Joe joe, ArgQueue args) {
        return new ListValue(args.asList());
    }

    //-------------------------------------------------------------------------
    // Method Implementation

    //**
    // @method add
    // @args [index], item
    // @result this
    // Adds the item to the list at the given *index*, which defaults
    // to the end of the list.
    private Object _add(JoeList list, Joe joe, ArgQueue args) {
        Joe.arityRange(args, 1, 2, "add([index], item)");

        if (args.size() == 1) {
            list.add(args.next());
        } else {
            list.add(
                joe.toIndex(args.next(), list.size() + 1),
                args.next()
            );
        }

        return list;
    }

    //**
    // @method addAll
    // @args [index], collection
    // @result this
    // Adds all items in the *collection* to the list at the
    // given *index*, which defaults to the end of the list.
    private Object _addAll(JoeList list, Joe joe, ArgQueue args) {
        Joe.arityRange(args, 1, 2, "addAll([index], collection)");

        if (args.size() == 1) {
            list.addAll(joe.toList(args.next()));
        } else {
            list.addAll(
                joe.toIndex(args.next(), list.size() + 1),
                joe.toType(Collection.class, args.next())
            );
        }

        return list;
    }

    //**
    // @method clear
    // @result this
    // Removes all items from the list.
    private Object _clear(JoeList list, Joe joe, ArgQueue args) {
        Joe.exactArity(args, 0, "clear()");
        list.clear();
        return list;
    }

    //**
    // @method contains
    // @args value
    // @result Boolean
    // Returns `true` if the list contains the *value*, and `false`
    // otherwise.
    private Object _contains(JoeList list, Joe joe, ArgQueue args) {
        Joe.exactArity(args, 1, "contains(value)");
        return list.contains(args.next());
    }

    //**
    // @method containsAll
    // @args collection
    // @result Boolean
    // Returns `true` if the list contains all the values in
    // the *collection*, and `false` otherwise.
    private Object _containsAll(JoeList list, Joe joe, ArgQueue args) {
        Joe.exactArity(args, 1, "containsAll(collection)");
        // According to IntelliJ, converting the list to a HashSet gives
        // better performance.
        return new HashSet<>(list).containsAll(
            joe.toType(Collection.class, args.next()));
    }

    //**
    // @method copy
    // @result List
    // Returns a shallow copy of the list.
    private Object _copy(JoeList list, Joe joe, ArgQueue args) {
        Joe.exactArity(args, 0, "copy()");
        return new ListValue(list);
    }

    //**
    // @method size
    // @result Number
    // Returns the number of items in the list.
    private Object _size(JoeList list, Joe joe, ArgQueue args) {
        Joe.exactArity(args, 0, "size()");
        return (double)list.size();
    }

}
