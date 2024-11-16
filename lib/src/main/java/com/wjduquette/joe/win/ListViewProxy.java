package com.wjduquette.joe.win;

import com.wjduquette.joe.*;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

/**
 * Proxy for a JavaFX JoeListView.
 */
class ListViewProxy extends FXProxy<JoeListView> {
    public static final ListViewProxy TYPE = new ListViewProxy();

    //-------------------------------------------------------------------------
    // Constructor

    //**
    // @package joe.win
    // @type JoeListView
    // @extends Control
    // The `ListView` type is a JavaFX scrolling list widget.
    // Joe classes can extend the `ListView` type.
    public ListViewProxy() {
        super("ListView");
        extendsProxy(ControlProxy.TYPE);
        proxies(JoeListView.class);

        // Initializer
        initializer(this::_initializer);

        //**
        // ## Properties
        //
        // `JoeListView` widgets have the following properties, in addition to
        // those inherited from superclasses.
        //
        // | Property         | Type            | Description            |
        // | ---------------- | --------------- | ---------------------- |
        // | `#placeholder`   | [[Node]]        | Empty list message     |
        // | `#selectedIndex` | [[joe.Number]]  | Index of select item   |
        // | `#selectedItem`  | any             | Value of selected item |

        fxProperty("placeholder", ListView::placeholderProperty, WinPackage::toNode);

        // Methods
        method("getSelectedIndex", this::_getSelectedIndex);
        method("getSelectedItem",  this::_getSelectedItem);
        method("item",             this::_item);
        method("items",            this::_items);
        method("onSelect",         this::_onSelect);
        method("placeholder",      this::_placeholder);
        method("placeholderText",  this::_placeholderText);
        method("selectIndex",      this::_selectIndex);
        method("selectItem",       this::_selectItem);
    }

    //-------------------------------------------------------------------------
    // JoeClass API

    @Override
    public boolean canBeExtended() {
        return true;
    }

    @Override
    public JoeObject make(Joe joe, JoeClass joeClass) {
        return new JoeListView(joe, joeClass);
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
    // @method getSelectedIndex
    // @result joe.Number
    // Gets the index of the selected item, or -1 if there is no selection.
    private Object _getSelectedIndex(JoeListView node, Joe joe, Args args) {
        args.exactArity(0, "getSelectedIndex()");
        return (double)node.getSelectionModel().getSelectedIndex();
    }

    //**
    // @method getSelectedItem
    // @result item
    // Gets the selected item, or `null` if there is no selection.
    private Object _getSelectedItem(JoeListView node, Joe joe, Args args) {
        args.exactArity(0, "getSelectedItem()");
        return node.getSelectionModel().getSelectedItem();
    }

    //**
    // @method item
    // @args item
    // @result this
    // Adds a value to the widget's list of items.
    private Object _item(JoeListView node, Joe joe, Args args) {
        args.exactArity(1, "item(item)");
        node.getItems().add(args.next());
        return node;
    }

    //**
    // @method items
    // @result joe.List
    // Gets the list of the widget's items, which can be updated freely.
    private Object _items(JoeListView node, Joe joe, Args args) {
        args.exactArity(0, "items()");
        return joe.wrapList(node.getItems(), Object.class);
    }

    private Object _onSelect(JoeListView node, Joe joe, Args args) {
        args.exactArity(1, "onSelect(callable)");
        var handler = args.next();

        if (handler instanceof JoeCallable callable) {
            node.setOnSelect(n -> joe.call(callable, n));
        } else {
            throw joe.expected("callable", handler);
        }
        return node;
    }

    private Object _placeholder(JoeListView node, Joe joe, Args args) {
        args.exactArity(1, "placeholder(node)");
        node.setPlaceholder(WinPackage.toNode(joe, args.next()));
        return node;
    }
    private Object _placeholderText(JoeListView node, Joe joe, Args args) {
        args.exactArity(1, "placeholderText(text)");
        node.setPlaceholder(new Label(joe.stringify(args.next())));
        return node;
    }

    private Object _selectIndex(JoeListView node, Joe joe, Args args) {
        args.exactArity(1, "selectIndex(index)");
        var index = joe.toIndex(args.next(), node.getItems().size());
        node.selectIndex(index);
        return node;
    }

    private Object _selectItem(JoeListView node, Joe joe, Args args) {
        args.exactArity(1, "selectItem(item)");
        node.selectItem(args.next());
        return node;
    }

}
