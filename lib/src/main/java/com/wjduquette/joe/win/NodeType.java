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

class NodeType extends WidgetType<Node> {
    public static final NodeType TYPE = new NodeType();

    //-------------------------------------------------------------------------
    // Constructor

    //**
    // @package joe.win
    // @type Node
    // @extends Widget
    // The `Node` widget is the abstract base class for JavaFX widgets
    // that can be added to [[Pane|Panes]] in the scene graph, and provides
    // services for all such widgets.
    public NodeType() {
        super("Node");
        proxies(Node.class);

        // No initializer

        //**
        // @property disable joe.Boolean
        // If true, disables the node and its descendants.
        fxProperty("disable", Node::disableProperty, Joe::toBoolean);

        //**
        // @property id joe.String
        // JavaFX widget ID
        fxProperty("id",      Node::idProperty,      Joe::toString);

        //**
        // @property style joe.String
        // FXCSS style string. See [[joe.win#topic.css]].
        fxProperty("style",   Node::styleProperty,   Joe::toString);

        //**
        // @property visible joe.Boolean
        // Whether the widget is visible or not.
        fxProperty("visible", Node::visibleProperty, Joe::toBoolean);

        // Methods
        method("getId",            this::_getId);
        method("hgrow",            this::_hgrow);
        method("isDisable",        this::_isDisable);
        method("isDisabled",       this::_isDisabled);
        method("resizeWithParent", this::_resizeWithSplit);
        method("setDisable",       this::_setDisable);
        method("setId",            this::_setId);
        method("styleClasses",     this::_styleClasses);
        method("styles",           this::_styles);
        method("vgrow",            this::_vgrow);
    }


    //-------------------------------------------------------------------------
    // Methods

    //**
    // @method getId
    // @result joe.String
    // Gets the value of the node's `#id` property.
    private Object _getId(Node node, Joe joe, Args args) {
        args.exactArity(0, "getId()");
        return node.getId();
    }

    //**
    // @method hgrow
    // @args [priority]
    // @result this
    // Sets the [[HBox]] and [[GridPane]] `hgrow` constraint for the node to
    // the given [[Priority]], or to `Priority.ALWAYS` if the priority
    // is omitted.
    //
    // This method is equivalent to calling both
    // [[HBox#static.setHgrow]] and
    // [[GridPane#static.setHgrow]] for this node.
    private Object _hgrow(Node node, Joe joe, Args args) {
        args.arityRange(0, 1, "hgrow([priority])");
        var priority = args.isEmpty()
            ? Priority.ALWAYS
            : Win.toPriority(joe, args.next());
        HBox.setHgrow(node, priority);
        GridPane.setHgrow(node, priority);
        return node;
    }

    //**
    // @method isDisable
    // @result joe.Boolean
    // Returns `true` if this node's `disable` property has
    // been set, and `false` otherwise.
    private Object _isDisable(Node node, Joe joe, Args args) {
        args.exactArity(0, "isDisable()");
        return node.isDisable();
    }

    //**
    // @method isDisabled
    // @result joe.Boolean
    // Returns `true` if this node is disabled because it or a parent
    // node's `disable` property has been set, and `false` otherwise.
    private Object _isDisabled(Node node, Joe joe, Args args) {
        args.exactArity(0, "isDisabled()");
        return node.isDisabled();
    }

    //**
    // @method resizeWithSplit
    // @args flag
    // @result this
    // If *flag* is `true` (the default value) the node's "split" will resize
    // when its parent [[SplitPane]] is resized, preserving its
    // divider fraction.  If `false`, the divider fraction will change to
    // keep the node's width or height constant. Use this to prevent sidebars
    // from resizing when the window is resized.
    //
    // This is equivalent to the
    // [[SplitPane#static.setResizableWithParent]] method.
    private Object _resizeWithSplit(Node node, Joe joe, Args args) {
        args.exactArity(1, "resizeWithSplit(flag)");
        var flag = args.isEmpty() || Joe.isTruthy(args.next());
        SplitPane.setResizableWithParent(node, flag);
        return node;
    }

    //**
    // @method setDisable
    // @args [flag]
    // @result this
    // Sets the node's `#disable` property to *flag*; if omitted,
    // *flag* defaults to `true`.  While `true`, this node and its
    // descendants in the scene graph will be disabled.
    private Object _setDisable(Node node, Joe joe, Args args) {
        args.arityRange(0, 1, "setDisable([flag])");
        var flag = args.isEmpty() || Joe.isTruthy(args.next());
        node.setDisable(flag);
        return node;
    }

    //**
    // @method setId
    // @args id
    // @result this
    // Sets the node's `#id` property to the given *id* string.
    private Object _setId(Node node, Joe joe, Args args) {
        args.exactArity(1, "setId(id)");
        var id = joe.toString(args.next());
        node.setId(id);
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
    // Sets the [[VBox]] and [[GridPane]] `vgrow` constraint for the node to
    // the given [[Priority]], or to `Priority.ALWAYS` if the priority
    // is omitted.
    //
    // This method is equivalent to calling both
    // [[VBox#static.setVgrow]] and
    // [[GridPane#static.setVgrow]] for this node.
    private Object _vgrow(Node node, Joe joe, Args args) {
        args.arityRange(0, 1, "vgrow([priority])");
        var priority = args.isEmpty()
            ? Priority.ALWAYS
            : Win.toPriority(joe, args.next());
        VBox.setVgrow(node, priority);
        GridPane.setVgrow(node, priority);
        return node;
    }
}
