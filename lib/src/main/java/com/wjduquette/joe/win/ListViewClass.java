package com.wjduquette.joe.win;

import com.wjduquette.joe.*;
import javafx.scene.control.Label;

/**
 * Proxy for a JavaFX ListViewInstance.
 */
class ListViewClass extends WidgetType<ListViewInstance> {
    public static final ListViewClass TYPE = new ListViewClass();

    //-------------------------------------------------------------------------
    // Constructor

    //**
    // @package joe.win
    // @widget ListView
    // @extends Control
    // The `ListView` type is a JavaFX scrolling list widget.
    // Joe classes can extend the `ListView` type.
    public ListViewClass() {
        super("ListView");
        extendsProxy(ControlType.TYPE);
        proxies(ListViewInstance.class);

        // Initializer
        initializer(this::_initializer);

        //**
        // @property placeholder Node
        // A node to display when there are no items.
        fxProperty("placeholder", ListViewInstance::placeholderProperty, Win::toNode);

        // Methods
        method("getItems",         this::_getItems);
        method("getSelectedIndex", this::_getSelectedIndex);
        method("getSelectedItem",  this::_getSelectedItem);
        method("item",             this::_item);
        method("items",            this::_items);
        method("onSelect",         this::_onSelect);
        method("placeholder",      this::_placeholder);
        method("placeholderText",  this::_placeholderText);
        method("selectIndex",      this::_selectIndex);
        method("selectItem",       this::_selectItem);
        method("stringifier",      this::_stringifier);
    }

    //-------------------------------------------------------------------------
    // JoeClass API

    @Override
    public boolean canBeExtended() {
        return true;
    }

    @Override
    public Object make(Joe joe, JoeClass joeClass) {
        return new ListViewInstance(joe, joeClass);
    }


    //-------------------------------------------------------------------------
    // Initializer

    //**
    // @init
    // Returns a `ListView`.
    private Object _initializer(Joe joe, Args args) {
        args.exactArity(0, "ListView()");
        return make(joe, this);
    }

    //-------------------------------------------------------------------------
    // Methods

    //**
    // @method getItems
    // @result joe.List
    // Gets the list of the widget's items, which can be updated freely.
    private Object _getItems(ListViewInstance node, Joe joe, Args args) {
        args.exactArity(0, "getItems()");
        return joe.wrapList(node.getItems(), Object.class);
    }

    //**
    // @method getSelectedIndex
    // @result joe.Number
    // Gets the index of the selected item, or -1 if there is no selection.
    private Object _getSelectedIndex(ListViewInstance node, Joe joe, Args args) {
        args.exactArity(0, "getSelectedIndex()");
        return (double)node.getSelectionModel().getSelectedIndex();
    }

    //**
    // @method getSelectedItem
    // @result item
    // Gets the selected item, or `null` if there is no selection.
    private Object _getSelectedItem(ListViewInstance node, Joe joe, Args args) {
        args.exactArity(0, "getSelectedItem()");
        return node.getSelectionModel().getSelectedItem();
    }

    //**
    // @method item
    // @args item
    // @result this
    // Adds a value to the widget's list of items.
    private Object _item(ListViewInstance node, Joe joe, Args args) {
        args.exactArity(1, "item(item)");
        node.getItems().add(args.next());
        return node;
    }

    //**
    // @method items
    // @args list
    // @result this
    // Adds the contents of the *list* to the widgets list of items.
    private Object _items(ListViewInstance node, Joe joe, Args args) {
        args.exactArity(1, "items(list)");
        var list = joe.toList(args.next());
        node.getItems().addAll(list);
        return node;
    }

    //**
    // @method onSelect
    // @args callable
    // @result this
    // Specifies a callable to be called when the user selects an
    // item in the list.  The callable must take one argument,
    // the `ListView` itself.
    private Object _onSelect(ListViewInstance node, Joe joe, Args args) {
        args.exactArity(1, "onSelect(callable)");
        var handler = joe.toCallable(args.next());

        node.setOnSelect(new JoeConsumer<>(joe, handler));
        return node;
    }

    //**
    // @method placeholder
    // @args node
    // @result this
    // Sets the widget's placeholder graphic, a [[Node]] to display when
    // the widget's [[ListView#method.items]] list is empty.
    private Object _placeholder(ListViewInstance node, Joe joe, Args args) {
        args.exactArity(1, "placeholder(node)");
        node.setPlaceholder(Win.toNode(joe, args.next()));
        return node;
    }

    //**
    // @method placeholderText
    // @args text
    // @result this
    // Sets the widget's placeholder graphic to a label displaying
    // the given text. The placeholder is shown when
    // the widget's [[ListView#method.items]] list is empty.
    private Object _placeholderText(ListViewInstance node, Joe joe, Args args) {
        args.exactArity(1, "placeholderText(text)");
        node.setPlaceholder(new Label(joe.stringify(args.next())));
        return node;
    }

    //**
    // @method selectIndex
    // @args index
    // @result this
    // Selects the item at the given *index*.  Throws an error if the
    // index is not in range.
    private Object _selectIndex(ListViewInstance node, Joe joe, Args args) {
        args.exactArity(1, "selectIndex(index)");
        var index = joe.toIndex(args.next(), node.getItems().size());
        node.selectIndex(index);
        return node;
    }

    //**
    // @method selectItem
    // @args item
    // @result this
    // Selects the given *item*.  The call is a no-op if the *item*
    // isn't contained in the widget's list of items.
    private Object _selectItem(ListViewInstance node, Joe joe, Args args) {
        args.exactArity(1, "selectItem(item)");
        node.selectItem(args.next());
        return node;
    }

    //**
    // @method stringifier
    // @args callable
    // @result this
    // Sets the widget's stringifier to the given *callable*, which
    // must take one argument, a list item, and return a string
    // representation for that item.
    private Object _stringifier(ListViewInstance node, Joe joe, Args args) {
        args.exactArity(1, "stringifier(callable)");

        var handler = args.next();

        if (handler instanceof NativeCallable callable) {
            node.setStringifier(v -> joe.call(callable, v).toString());
        } else {
            throw joe.expected("callable", handler);
        }
        return node;
    }
}
