package com.wjduquette.joe.types;

import com.wjduquette.joe.*;

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
        method("get",         this::_get);
        method("getFirst",    this::_getFirst);
        method("getLast",     this::_getLast);
        method("indexOf",     this::_indexOf);
        method("isEmpty",     this::_isEmpty);
        method("lastIndexOf", this::_lastIndexOf);
        // map
        method("peekFirst",   this::_peekFirst);
        method("peekLast",    this::_peekLast);
        method("remove",      this::_remove);
        method("removeAll",   this::_removeAll);
        method("removeAt",    this::_removeAt);
        method("removeFirst", this::_removeFirst);
        method("removeLast",  this::_removeLast);
        method("reversed",    this::_reversed);
        method("set",         this::_set);
        // sort -- need comparison infrastructure
        method("sublist",     this::_sublist);
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
    // @method get
    // @args index
    // @result value
    // Returns the value at the given *index*.
    private Object _get(JoeList list, Joe joe, ArgQueue args) {
        Joe.exactArity(args, 1, "get(index)");
        return list.get(joe.toIndex(args.next(), list.size()));
    }

    //**
    // @method getFirst
    // @result value
    // Returns the first value in the list.  It's an error if
    // the list is empty.
    private Object _getFirst(JoeList list, Joe joe, ArgQueue args) {
        Joe.exactArity(args, 0, "getFirst()");
        if (!list.isEmpty()) {
            return list.getFirst();
        } else {
            throw new JoeError("List is empty.");
        }
    }

    //**
    // @method getLast
    // @result value
    // Returns the last value in the list.  It's an error if
    // the list is empty.
    private Object _getLast(JoeList list, Joe joe, ArgQueue args) {
        Joe.exactArity(args, 0, "getLast()");
        if (!list.isEmpty()) {
            return list.getLast();
        } else {
            throw new JoeError("List is empty.");
        }
    }

    //**
    // @method indexOf
    // @args value
    // @result Number
    // Returns the index of the first occurrence of the *value* in
    // the list, or -1 if not found.
    private Object _indexOf(JoeList list, Joe joe, ArgQueue args) {
        Joe.exactArity(args, 1, "indexOf(value)");
        return (double)list.indexOf(args.next());
    }

    //**
    // @method isEmpty
    // @result Boolean
    // Returns `true` if the list is empty, and `false` otherwise.
    private Object _isEmpty(JoeList list, Joe joe, ArgQueue args) {
        Joe.exactArity(args, 0, "isEmpty()");
        return list.isEmpty();
    }

    //**
    // @method lastIndexOf
    // @args value
    // @result Number
    // Returns the index of the last occurrence of the *value* in
    // the list, or -1 if not found.
    private Object _lastIndexOf(JoeList list, Joe joe, ArgQueue args) {
        Joe.exactArity(args, 1, "lastIndexOf(value)");
        return (double)list.lastIndexOf(args.next());
    }

    //**
    // @method peekFirst
    // @result value
    // Returns the first value in the list, or null if
    // the list is empty.
    private Object _peekFirst(JoeList list, Joe joe, ArgQueue args) {
        Joe.exactArity(args, 0, "peekFirst()");
        if (!list.isEmpty()) {
            return list.getFirst();
        } else {
            return null;
        }
    }

    //**
    // @method peekLast
    // @result value
    // Returns the last value in the list, or null if
    // the list is empty.
    private Object _peekLast(JoeList list, Joe joe, ArgQueue args) {
        Joe.exactArity(args, 0, "peekLast()");
        if (!list.isEmpty()) {
            return list.getLast();
        } else {
            return null;
        }
    }


    //**
    // @method remove
    // @args value
    // @result Boolean
    // Removes the *value* from the list if it's present.  Returns
    // `true` if the item was removed, and `false` if it was not
    // present
    private Object _remove(JoeList list, Joe joe, ArgQueue args) {
        Joe.exactArity(args, 1, "remove(value)");
        return list.remove(args.next());
    }

    //**
    // @method removeAll
    // @args collection
    // @result Boolean
    // Removes all items in the list that are found in the *collection*.
    // Returns `true` if any items were removed, `false` otherwise.
    private Object _removeAll(JoeList list, Joe joe, ArgQueue args) {
        Joe.exactArity(args, 1, "removeAll(collection)");
        return list.removeAll(
            joe.toType(Collection.class, args.next()));
    }

    //**
    // @method removeAt
    // @args index
    // @result value
    // Removes and returns the value at the given *index*.
    private Object _removeAt(JoeList list, Joe joe, ArgQueue args) {
        Joe.exactArity(args, 1, "remove(index)");
        return list.remove(joe.toIndex(args.next(), list.size()));
    }

    //**
    // @method removeFirst
    // @result value
    // Removes and returns the first value in the list.  It's an error if
    // the list is empty.
    private Object _removeFirst(JoeList list, Joe joe, ArgQueue args) {
        Joe.exactArity(args, 0, "removeFirst()");
        if (!list.isEmpty()) {
            return list.removeFirst();
        } else {
            throw new JoeError("List is empty.");
        }
    }

    //**
    // @method removeLast
    // @result value
    // Removes and returns the last value in the list.  It's an error if
    // the list is empty.
    private Object _removeLast(JoeList list, Joe joe, ArgQueue args) {
        Joe.exactArity(args, 0, "removeLast()");
        if (!list.isEmpty()) {
            return list.getLast();
        } else {
            throw new JoeError("List is empty.");
        }
    }

    //**
    // @method reverse
    // @result List
    // Returns a reversed copy of the list.
    private Object _reversed(JoeList list, Joe joe, ArgQueue args) {
        Joe.exactArity(args, 0, "reversed()");
        return new ListValue(list.reversed());
    }

    //**
    // @method set
    // @args index, newValue
    // @result oldValue
    // Puts the *value* at the given *index*, returning the
    // *oldValue* at that index.
    private Object _set(JoeList list, Joe joe, ArgQueue args) {
        Joe.exactArity(args, 2, "set(index, newValue)");
        return list.set(
            joe.toIndex(args.next(), list.size()),
            args.next()
        );
    }

    //**
    // @method size
    // @result Number
    // Returns the number of items in the list.
    private Object _size(JoeList list, Joe joe, ArgQueue args) {
        Joe.exactArity(args, 0, "size()");
        return (double)list.size();
    }

    //**
    // @method sublist
    // @args start, [end]
    // @result List
    // Returns the sublist of this list that starts at *start*
    // and ends before *end*, which defaults to the end of the list.
    private Object _sublist(JoeList list, Joe joe, ArgQueue args) {
        Joe.arityRange(args, 1, 2, "sublist(start, [end])");
        var start = joe.toIndex(args.next(), list.size());

        if (args.isEmpty()) {
            return new ListValue(list.subList(start, list.size()));
        } else {
            var end = joe.toIndex(args.next(), list.size() + 1);
            return new ListValue(list.subList(start, end));
        }
    }
}
