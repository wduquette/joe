package com.wjduquette.joe.win;

import com.wjduquette.joe.Args;
import com.wjduquette.joe.Joe;
import com.wjduquette.joe.JoeClass;
import javafx.scene.Node;
import javafx.scene.control.Tab;

import java.util.stream.Collectors;

class TabClass extends WidgetType<Tab> {
    public static final TabClass TYPE = new TabClass();

    //-------------------------------------------------------------------------
    // Constructor

    //**
    // @package joe.win
    // @widget Tab
    // %extends Widget
    // The `Tab` widget contains a [[Node]] and is displayed in a [[TabPane]].
    // Joe classes can extend the `Tab` type.
    public TabClass() {
        super("Tab");
        proxies(Tab.class);

        initializer(this::_initializer);

        //**
        // @property content Node
        // The content node.
        fxProperty("content",  Tab::contentProperty, Win::toNode);

        //**
        // @property id joe.String
        // JavaFX widget ID
        fxProperty("id",       Tab::idProperty,      Joe::toString);

        //**
        // @property text joe.String
        // Text to display
        fxProperty("text",     Tab::textProperty,    Joe::toString);

        //**
        // @property style joe.String
        // FXCSS style string. See [[joe.win#topic.css]].
        fxProperty("style",    Tab::styleProperty,   Joe::toString);

        // Methods
        method("content",         this::_content);
        method("disable",         this::_disable);
        method("id",              this::_id);
        method("isDisabled",      this::_isDisabled);
        method("isSelected",      this::_isSelected);
        method("styleClasses",    this::_styleClasses);
        method("styles",          this::_styles);
        method("tabPane",         this::_tabPane);
        method("text",            this::_text);
    }

    //-------------------------------------------------------------------------
    // JoeClass API

    @Override
    public boolean canBeExtended() {
        return true;
    }

    @Override
    public Object make(Joe joe, JoeClass joeClass) {
        var tab = new TabInstance(joeClass);
        tab.setClosable(false);
        return tab;
    }

    //-------------------------------------------------------------------------
    // Initializer

    //**
    // @init
    // Returns a `Tab`.
    private Object _initializer(Joe joe, Args args) {
        args.exactArity(0, "Tab()");
        return make(joe, this);
    }

    //-------------------------------------------------------------------------
    // Methods

    //**
    // @method content
    // %args node
    // %result this
    // Sets the tab's `#content` property to *node*.
    private Object _content(Tab tab, Joe joe, Args args) {
        args.exactArity(1, "content(node)");
        var node = joe.toClass(args.next(), Node.class);
        tab.setContent(node);
        return tab;
    }

    //**
    // @method disable
    // %args [flag]
    // %result this
    // Sets the tab's `#disable` property to *flag*; if omitted,
    // *flag* defaults to `true`.  While `true`, this tab and its
    // descendants in the scene graph will be disabled.
    private Object _disable(Tab tab, Joe joe, Args args) {
        args.arityRange(0, 1, "disable([flag])");
        var flag = args.isEmpty() || Joe.isTruthy(args.next());
        tab.setDisable(flag);
        return tab;
    }

    //**
    // @method id
    // %args id
    // %result this
    // Sets the tab's `#id` property to the given *id* string.
    private Object _id(Tab tab, Joe joe, Args args) {
        args.exactArity(1, "id(id)");
        var id = joe.toString(args.next());
        tab.setId(id);
        return tab;
    }

    //**
    // @method isDisabled
    // %result joe.Boolean
    // Returns `true` if the tab has been disabled, and `false` otherwise.
    private Object _isDisabled(Tab tab, Joe joe, Args args) {
        args.exactArity(0, "isDisabled()");
        return tab.isDisabled();
    }

    //**
    // @method isSelected
    // %result joe.Boolean
    // Returns `true` if the tab is selected, and `false` otherwise.
    private Object _isSelected(Tab tab, Joe joe, Args args) {
        args.exactArity(0, "isSelected()");
        return tab.isSelected();
    }

    //**
    // @method styleClasses
    // %result joe.List
    // Gets the list of the tab's FXCSS style class names.  Values must
    // be valid CSS style class names.
    //
    // See [[joe.win#topic.css]] for more on using CSS.
    private Object _styleClasses(Tab tab, Joe joe, Args args) {
        args.exactArity(0, "styleClasses()");
        return joe.wrapList(tab.getStyleClass(), String.class);
    }

    //**
    // @method styles
    // %args style, ...
    // %result this
    // Sets the tab's FXCSS `#style` property.  The caller can pass
    // multiple style strings, which will be joined with semicolons.
    //
    // See [[joe.win#topic.css]] for more on using CSS.
    private Object _styles(Tab tab, Joe joe, Args args) {
        args.minArity(1, "styles(style, ...)");
        var styles = args.asList().stream()
            .map(joe::toString)
            .collect(Collectors.joining(";\n"));
        tab.setStyle(styles);
        return tab;
    }

    //**
    // @method tabPane
    // %result TabPane
    // Returns the [[TabPane]] to which this tab belongs, or null if
    // none.
    private Object _tabPane(Tab tab, Joe joe, Args args) {
        args.exactArity(0, "tabPane()");
        return tab.getTabPane();
    }

    //**
    // @method text
    // %args text
    // %result this
    // Sets the label's text.
    private Object _text(Tab tab, Joe joe, Args args) {
        args.exactArity(1, "text(text)");
        tab.setText(joe.stringify(args.next()));
        return tab;
    }
}
