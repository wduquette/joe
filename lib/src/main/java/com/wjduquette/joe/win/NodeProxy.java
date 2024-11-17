package com.wjduquette.joe.win;

import com.wjduquette.joe.Args;
import com.wjduquette.joe.Joe;
import javafx.scene.Node;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
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
    //
    // @includeMixin FXProxy
    public NodeProxy() {
        super("Node");
        proxies(Node.class);

        // No initializer

        //**
        // ## Properties
        //
        // All `Node` widgets have the following properties.
        //
        // | Property   | Type             | Description        |
        // | ---------- | ---------------- | ------------------ |
        // | `#id`      | [[joe.String]]   | JavaFX ID          |
        // | `#style`   | [[joe.String]]   | FXCSS style string |
        // | `#visible` | [[joe.Boolean]]  | Visibility flag    |
        //
        // See [[joe.win#topic.css]] for more on using CSS.
        fxProperty("id",      Node::idProperty,      Joe::toString);
        fxProperty("style",   Node::styleProperty,   Joe::toString);
        fxProperty("visible", Node::visibleProperty, Joe::toBoolean);

        // Methods
        method("disable",               this::_disable);
        method("gridColumn",            this::_gridColumn);
        method("gridColumnSpan",        this::_gridColumnSpan);
        method("gridHalignment",        this::_gridHalignment);
        method("gridHgrow",             this::_gridHgrow);
        method("gridMargin",            this::_gridMargin);
        method("gridRow",               this::_gridRow);
        method("gridRowSpan",           this::_gridRowSpan);
        method("gridValignment",        this::_gridValignment);
        method("gridVgrow",             this::_gridVgrow);
        method("hgrow",                 this::_hgrow);
        method("id",                    this::_id);
        method("isDisabled",            this::_isDisabled);
        method("splitResizeWithParent", this::_splitResizeWithParent);
        method("styleClasses",          this::_styleClasses);
        method("styles",                this::_styles);
        method("vgrow",                 this::_vgrow);
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
    private Object _disable(Node node, Joe joe, Args args) {
        args.arityRange(0, 1, "disable([flag])");
        var flag = args.isEmpty() || Joe.isTruthy(args.next());
        node.setDisable(flag);
        return node;
    }

    //**
    // @method gridColumn
    // @args index
    // @result this
    // Sets the [[GridPane]] `column` constraint for the node to the
    // given column *index*.
    //
    // This method is equivalent to the JavaFX `GridPane.setColumnIndex()`
    // method.
    private Object _gridColumn(Node node, Joe joe, Args args) {
        args.exactArity(1, "gridColumn(index)");
        GridPane.setColumnIndex(node, joe.toInteger(args.next()));
        return node;
    }

    //**
    // @method gridColumnSpan
    // @args span
    // @result this
    // Sets the [[GridPane]] `columnSpan` constraint for the node to the
    // given  *span*, which must be a positive number.
    //
    // This method is equivalent to the JavaFX `GridPane.setColumnSpan()`
    // method.
    private Object _gridColumnSpan(Node node, Joe joe, Args args) {
        args.exactArity(1, "gridColumnSpan(span)");
        GridPane.setColumnSpan(node, WinPackage.toSpan(joe, args.next()));
        return node;
    }

    //**
    // @method gridHalignment
    // @args hpos
    // @result this
    // Sets the [[GridPane]] `halignment` constraint for the node to the
    // given [[HPos]].
    //
    // This method is equivalent to the JavaFX `GridPane.setHalignment()`
    // method.
    private Object _gridHalignment(Node node, Joe joe, Args args) {
        args.exactArity(1, "gridHalignment(hpos)");
        GridPane.setHalignment(node, WinPackage.toHPos(joe, args.next()));
        return node;
    }

    //**
    // @method gridHgrow
    // @args [priority]
    // @result this
    // Sets the [[GridPane]] `hgrow` constraint for the node to the
    // given [[Priority]], or to `Priority.ALWAYS` if the priority
    // is omitted.
    //
    // This method is equivalent to the JavaFX `GridPane.setHgrow()`
    // method.
    private Object _gridHgrow(Node node, Joe joe, Args args) {
        args.arityRange(0, 1, "gridHgrow([priority])");
        if (args.isEmpty()) {
            GridPane.setHgrow(node, Priority.ALWAYS);
        } else {
            GridPane.setHgrow(node, WinPackage.toPriority(joe, args.next()));
        }
        return node;
    }

    //**
    // @method gridMargin
    // @args insets
    // @result this
    // Sets the [[GridPane]] `margin` constraint for the node to the
    // given [[Insets]].
    //
    // This method is equivalent to the JavaFX `GridPane.setMargin()`
    // method.
    private Object _gridMargin(Node node, Joe joe, Args args) {
        args.exactArity(1, "gridMargin(insets)");
        GridPane.setMargin(node, WinPackage.toInsets(joe, args.next()));
        return node;
    }

    //**
    // @method gridRow
    // @args index
    // @result this
    // Sets the [[GridPane]] `row` constraint for the node to the
    // given row *index*.
    //
    // This method is equivalent to the JavaFX `GridPane.setRowIndex()`
    // method.
    private Object _gridRow(Node node, Joe joe, Args args) {
        args.exactArity(1, "gridRow(index)");
        GridPane.setRowIndex(node, joe.toInteger(args.next()));
        return node;
    }

    //**
    // @method gridRowSpan
    // @args span
    // @result this
    // Sets the [[GridPane]] `rowSpan` constraint for the node to the
    // given  *span*, which must be a positive number.
    //
    // This method is equivalent to the JavaFX `GridPane.setRowSpan()`
    // method.
    private Object _gridRowSpan(Node node, Joe joe, Args args) {
        args.exactArity(1, "gridRowSpan(span)");
        GridPane.setRowSpan(node, WinPackage.toSpan(joe, args.next()));
        return node;
    }


    //**
    // @method gridValignment
    // @args vpos
    // @result this
    // Sets the [[GridPane]] `valignment` constraint for the node to the
    // given [[VPos]].
    //
    // This method is equivalent to the JavaFX `GridPane.setValignment()`
    // method.
    private Object _gridValignment(Node node, Joe joe, Args args) {
        args.exactArity(1, "gridValignment(vpos)");
        GridPane.setValignment(node, WinPackage.toVPos(joe, args.next()));
        return node;
    }

    //**
    // @method gridVgrow
    // @args [priority]
    // @result this
    // Sets the [[GridPane]] `vgrow` constraint for the node to the
    // given [[Priority]], or to `Priority.ALWAYS` if the priority
    // is omitted.
    //
    // This method is equivalent to the JavaFX `GridPane.setVgrow()`
    // method.
    private Object _gridVgrow(Node node, Joe joe, Args args) {
        args.arityRange(0, 1, "gridVgrow([priority])");
        if (args.isEmpty()) {
            GridPane.setVgrow(node, Priority.ALWAYS);
        } else {
            GridPane.setVgrow(node, WinPackage.toPriority(joe, args.next()));
        }
        return node;
    }

    //**
    // @method hgrow
    // @args [priority]
    // @result this
    // Sets the [[HBox]] `hgrow` constraint for the node to the
    // given [[Priority]], or to `Priority.ALWAYS` if the priority
    // is omitted.
    //
    // This method is equivalent to the JavaFX `HBox.setHgrow()`
    // method.
    private Object _hgrow(Node node, Joe joe, Args args) {
        args.arityRange(0, 1, "hgrow([priority])");
        if (args.isEmpty()) {
            HBox.setHgrow(node, Priority.ALWAYS);
        } else {
            HBox.setHgrow(node, WinPackage.toPriority(joe, args.next()));
        }
        return node;
    }

    //**
    // @method id
    // @args id
    // @result this
    // Sets the node's `#id` property to the given *id* string.
    private Object _id(Node node, Joe joe, Args args) {
        args.exactArity(1, "id(id)");
        var id = joe.toString(args.next());
        node.setId(id);
        return node;
    }

    //**
    // @method isDisabled
    // @result joe.Boolean
    // Returns `true` if the node has been disabled, and `false` otherwise.
    private Object _isDisabled(Node node, Joe joe, Args args) {
        args.exactArity(0, "isDisabled()");
        return node.isDisabled();
    }

    //**
    // @method splitResizeWithParent
    // @args flag
    // @result this
    // If *flag* is `true` (the default value) the node's "split" will resize
    // when its parent [[SplitPane]] is resized, preserving its
    // divider fraction.  If `false`, the divider fraction will change to
    // keep the node's width or height constant. Use this to prevent sidebars
    // from resizing when the window is resized.
    //
    // This is equivalent to the
    // JavaFX `SplitPane.setResizeWithParent()` method.
    private Object _splitResizeWithParent(Node node, Joe joe, Args args) {
        args.exactArity(1, "splitResizeWithParent(flag)");
        var flag = args.isEmpty() || Joe.isTruthy(args.next());
        SplitPane.setResizableWithParent(node, flag);
        return node;
    }

    //**
    // @method styleClasses
    // @result joe.List
    // Gets the list of the node's FXCSS style class names.  Values must
    // be valid CSS style class names.
    //
    // See [[joe.win#topic.css]] for more on using CSS.
    private Object _styleClasses(Node node, Joe joe, Args args) {
        args.exactArity(0, "styleClasses()");
        return joe.wrapList(node.getStyleClass(), String.class);
    }

    //**
    // @method styles
    // @args style, ...
    // @result this
    // Sets the node's FXCSS `#style` property.  The caller can pass
    // multiple style strings, which will be joined with semicolons.
    //
    // See [[joe.win#topic.css]] for more on using CSS.
    private Object _styles(Node node, Joe joe, Args args) {
        args.minArity(1, "styles(style, ...)");
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
    private Object _vgrow(Node node, Joe joe, Args args) {
        args.arityRange(0, 1, "vgrow([priority])");
        if (args.isEmpty()) {
            VBox.setVgrow(node, Priority.ALWAYS);
        } else {
            VBox.setVgrow(node, WinPackage.toPriority(joe, args.next()));
        }
        return node;
    }
}
