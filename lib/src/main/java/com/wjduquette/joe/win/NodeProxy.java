package com.wjduquette.joe.win;

import com.wjduquette.joe.ArgQueue;
import com.wjduquette.joe.Joe;
import com.wjduquette.joe.TypeProxy;
import javafx.scene.Node;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.stream.Collectors;

class NodeProxy extends FXProxy<Node> {
    public static final NodeProxy TYPE = new NodeProxy();

    //-------------------------------------------------------------------------
    // Constructor

    //**
    // @package joe.win
    // @type Node
    // The `Node` widget is the abstract base class for the JavaFX widget
    // hierarchy. This abstract type provides features available for
    // all widgets.
    public NodeProxy() {
        super("Node");
        proxies(Node.class);

        // No initializer

        //**
        // ## Properties
        //
        // | Property   | Type             | Description        |
        // | ---------- | ---------------- | ------------------ |
        // | `#id`      | [[joe.String]]   | JavaFX ID          |
        // | `#style`   | [[joe.String]]   | FXCSS style string |
        // | `#visible` | [[joe.Boolean]]  | Visibility flag    |
        fxProperty("id",      String.class,  Node::idProperty);
        fxProperty("style",   String.class,  Node::styleProperty);
        fxProperty("visible", Boolean.class, Node::visibleProperty);

        // Methods
        method("disable",         this::_disable);
        method("id",              this::_id);
        method("isDisabled",      this::_isDisabled);
        method("styleClasses",    this::_styleClasses);
        method("styles",          this::_styles);
        method("vgrow",           this::_vgrow);
    }


    //-------------------------------------------------------------------------
    // Methods

    //**
    // @method disable
    // @args [flag]
    // @result this
    // Sets the node's `#disable` property to *flag*; if omitted,
    // *flag* defaults to `true`.  While `true`, this node and its
    // descendants in the scene graph will be disabled.
    private Object _disable(Node node, Joe joe, ArgQueue args) {
        Joe.arityRange(args, 0, 1, "disable([flag])");
        var flag = args.isEmpty() ? true : Joe.isTruthy(args.next());
        node.setDisable(flag);
        return node;
    }

    //**
    // @method id
    // @args id
    // @result this
    // Sets the node's `#id` property to the given *id* string.
    private Object _id(Node node, Joe joe, ArgQueue args) {
        Joe.exactArity(args, 1, "id(id)");
        var id = joe.toString(args.next());
        node.setId(id);
        return node;
    }

    //**
    // @method isDisabled
    // @result joe.Boolean
    // Returns `true` if the node has been disabled, and `false` otherwise.
    private Object _isDisabled(Node node, Joe joe, ArgQueue args) {
        Joe.exactArity(args, 0, "isDisabled()");
        return node.isDisabled();
    }

    //**
    // @method styleClasses
    // @result joe.List
    // Gets the list of the node's FXCSS style class names.  Values must
    // be valid CSS style class strings.
    private Object _styleClasses(Node node, Joe joe, ArgQueue args) {
        Joe.exactArity(args, 0, "styleClasses()");
        return joe.wrapList(node.getStyleClass(), String.class);
    }

    //**
    // @method styles
    // @args style, ...
    // @result this
    // Sets the node's FXCSS `#style` property.  The caller can pass
    // multiple style strings, which will be joined with semicolons.
    private Object _styles(Node node, Joe joe, ArgQueue args) {
        Joe.minArity(args, 1, "styles(style, ...)");
        var styles = args.asList().stream()
            .map(joe::toString)
            .collect(Collectors.joining(";\n"));
        node.setStyle(styles);
        return node;
    }

    //**
    // @method vgrow
    // @args [priority]
    // @result this
    // Sets the [[VBox]] `vgrow` constraint for the node to the
    // given [[Priority]], or to `Priority.ALWAYS` if the priority
    // is omitted.
    //
    // This method is equivalent to the JavaFX `VBox.setVgrow()`
    // method.
    private Object _vgrow(Node node, Joe joe, ArgQueue args) {
        Joe.arityRange(args, 0, 1, "vgrow([priority]");
        if (args.isEmpty()) {
            VBox.setVgrow(node, Priority.ALWAYS);
        } else {
            VBox.setVgrow(node, joe.toEnum(args.next(), Priority.class));
        }
        return node;
    }
}
