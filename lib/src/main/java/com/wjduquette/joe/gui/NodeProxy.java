package com.wjduquette.joe.gui;

import com.wjduquette.joe.ArgQueue;
import com.wjduquette.joe.Joe;
import com.wjduquette.joe.TypeProxy;
import javafx.scene.Node;

import java.util.stream.Collectors;

class NodeProxy extends TypeProxy<Node> {
    public static final NodeProxy TYPE = new NodeProxy();

    //-------------------------------------------------------------------------
    // Constructor

    //**
    // @package joe.gui
    // @type Node
    // The `Node` widget is the abstract base class for the JavaFX widget
    // hierarchy. This abstract type provides features available for
    // all widgets.
    public NodeProxy() {
        super("Node");
        proxies(Node.class);

        // No initializer

        // Methods
        method("getId",           this::_getId);
        method("getStyle",        this::_getStyle);
        method("getStyleClasses", this::_getStyleClasses);
        method("isDisabled",      this::_isDisabled);
        method("isVisible",       this::_isVisible);
        method("setDisable",      this::_setDisable);
        method("setId",           this::_setId);
        method("setStyle",        this::_setStyle);
        method("setVisible",      this::_setVisible);
    }


    //-------------------------------------------------------------------------
    // Methods

    //**
    // @method getId
    // @result joe.String
    // Gets the node's ID string, or null if not defined.
    private Object _getId(Node node, Joe joe, ArgQueue args) {
        Joe.exactArity(args, 0, "getId()");
        return node.getId();
    }

    //**
    // @method getStyle
    // @result joe.String
    // Gets the node's FXCSS style, or null if none.  The style is a single
    // CSS string which can contain multiple style settings, separated by
    // semicolons.
    private Object _getStyle(Node node, Joe joe, ArgQueue args) {
        Joe.exactArity(args, 0, "getStyle()");
        return node.getStyle();
    }

    //**
    // @method getStyleClasses
    // @result joe.List
    // Gets the list of the node's FXCSS style class names.  Values must
    // be valid CSS style class strings.
    private Object _getStyleClasses(Node node, Joe joe, ArgQueue args) {
        Joe.exactArity(args, 0, "getStyleClasses()");
        return joe.wrapList(node.getStyleClass(), String.class);
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
    // @method isVisible
    // @result joe.Boolean
    // Returns `true` if the node is marked "visible", and `false` otherwise.
    //
    // Note: if `false`, the node will occupy its usual space in the window,
    // but will be transparent.
    private Object _isVisible(Node node, Joe joe, ArgQueue args) {
        Joe.exactArity(args, 0, "isVisible()");
        return node.isVisible();
    }

    //**
    // @method setDisable
    // @args flag
    // @result this
    // Sets the node's `disable` flag.  If `true`, this node and its
    // descendants in the scene graph will be disabled.
    private Object _setDisable(Node node, Joe joe, ArgQueue args) {
        Joe.exactArity(args, 1, "setDisable(flag)");
        node.setDisable(Joe.isTruthy(args.next()));
        return node;
    }

    //**
    // @method setId
    // @args id
    // @result this
    // Sets the node's ID string.
    private Object _setId(Node node, Joe joe, ArgQueue args) {
        Joe.exactArity(args, 1, "setId(id)");
        var id = joe.toString(args.next());
        node.setId(id);
        return node;
    }

    //**
    // @method setVisible
    // @args flag
    // @result this
    // Sets the node's `visible` flag.
    //
    // Note: if `false`, the node will occupy its usual space in the window,
    // but will be transparent.
    private Object _setVisible(Node node, Joe joe, ArgQueue args) {
        Joe.exactArity(args, 1, "setVisible(flag)");
        node.setVisible(Joe.isTruthy(args.next()));
        return node;
    }

    //**
    // @method setStyle
    // @args style, ...
    // @result this
    // Sets the node's FXCSS style.  The caller can pass multiple style
    // strings, which will be joined with semicolons.
    private Object _setStyle(Node node, Joe joe, ArgQueue args) {
        Joe.minArity(args, 1, "setStyle(style, ...)");
        var styles = args.asList().stream()
            .map(joe::toString)
            .collect(Collectors.joining(";\n"));
        node.setStyle(styles);
        return node;
    }
}
