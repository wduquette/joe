package com.wjduquette.joe.win;

import com.wjduquette.joe.*;
import com.wjduquette.joe.types.EnumType;
import javafx.geometry.*;
import javafx.scene.control.ContentDisplay;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * An experimental Joe package for building JavaFX GUIs
 */
public class WinPackage extends NativePackage {
    //-------------------------------------------------------------------------
    // Instance Variables

    private final Stage stage;
    private final VBox root;

    //-------------------------------------------------------------------------
    // Constructor

    //**
    // @package joe.win
    // This package contains an experimental API for building JavaFX GUIs.
    // It assumes a familiarity with JavaFX.
    //
    // @packageTopic hierarchy
    // @title Widget Hierarchy
    //
    // The `joe.win` widget type hierarchy is a subset of the JavaFX Java
    // class hierarchy.
    //
    // - [[Widget]] (base type)
    //   - [[MenuItem]]
    //     - [[Menu]]
    //   - [[Node]]: (base type)
    //     - [[Region]]: (base type)
    //       - [[Control]]: (base type)
    //         - [[Labeled]]: (base type)
    //           - [[Button]]
    //           - [[Label]]
    //         - [[ListView]]
    //         - [[MenuBar]]
    //         - [[Separator]]
    //         - [[SplitPane]]
    //         - [[TabPane]]
    //       - [[Pane]]
    //         - [[GridPane]]
    //         - [[HBox]]
    //         - [[StackPane]]
    //         - [[VBox]]
    //   - [[Tab]]: A tab in a [[TabPane]]
    //
    // @packageTopic css
    // @title Styling with CSS
    //
    // Scripts can style widgets using CSS in several different ways.
    //
    // - The [[Win#static.css]] method associates a CSS stylesheet, passed
    //   as a text string, with the application as a whole.
    // - Each widget has a [[Node#method.styleClasses]] method which returns
    //   a list of the names of the CSS style classes that apply to the widget;
    //   add any desired style class name to this list.
    // - Each widget has a `#style` property which can be set to a string
    //   defining one or more CSS style settings, similar to the HTML
    //   `style` attribute.
    //
    // JavaFX styles widgets using its own peculiar form of CSS, called
    // "FXCSS".  See the
    // [*JavaFX CSS Reference Guide*](https://openjfx.io/javadoc/21/javafx.graphics/javafx/scene/doc-files/cssref.html)
    // for details.

    /**
     * Creates an instance of the package.
     * @param stage The root stage window
     * @param root The root widget in the scene graph
     */
    public WinPackage(Stage stage, VBox root) {
        super("joe.win");
        this.stage = stage;
        this.root = root;

        // Main Singleton
        type(new WinProxy());

        // Widget classes, indented according to hierarchy
        type(MenuItemType.TYPE);
            type(MenuType.TYPE);
        type(NodeType.TYPE);
            type(RegionType.TYPE);
                type(ControlType.TYPE);
                    type(LabeledType.TYPE);
                        type(ButtonType.TYPE);
                        type(LabelType.TYPE);
                    type(ListViewClass.TYPE);
                    type(MenuBarType.TYPE);
                    type(SeparatorType.TYPE);
                    type(SplitPaneType.TYPE);
                    type(TabPaneType.TYPE);
                type(PaneType.TYPE);
                    type(GridPaneClass.TYPE);
                    type(StackPaneClass.TYPE);
                    type(VBoxClass.TYPE);
                    type(HBoxClass.TYPE);
        type(TabClass.TYPE);

        // Miscellaneous Types
        type(InsetsType.TYPE);
        type(ListenerType.TYPE);

        // Enums

        //**
        // @enum HPos
        // @constant CENTER
        // @constant LEFT
        // @constant RIGHT
        type(new EnumType<>("HPos", HPos.class));

        //**
        // @enum ContentDisplay
        // @constant BOTTOM
        // @constant CENTER
        // @constant GRAPHIC_ONLY
        // @constant LEFT
        // @constant RIGHT
        // @constant TEXT_ONLY
        // @constant TOP
        type(new EnumType<>("ContentDisplay", ContentDisplay.class));

        //**
        // @enum Orientation
        // @constant HORIZONTAL
        // @constant VERTICAL
        type(new EnumType<>("Orientation", Orientation.class));

        //**
        // @enum Pos
        // The `Pos` enum lists ways a widget can be aligned
        // relative to the boundaries of a rectangular space.
        //
        // @constant BASELINE_CENTER
        // @constant BASELINE_LEFT
        // @constant BASELINE_RIGHT
        // @constant BOTTOM_CENTER
        // @constant BOTTOM_LEFT
        // @constant BOTTOM_RIGHT
        // @constant CENTER
        // @constant CENTER_LEFT
        // @constant CENTER_RIGHT
        // @constant TOP_CENTER
        // @constant TOP_LEFT
        // @constant TOP_RIGHT
        type(new EnumType<>("Pos", Pos.class));

        //**
        // @enum Priority
        // The `Priority` enum's values indicate when a widget
        // should resize itself to fit its parent widget.  The
        // default is generally `NEVER`.
        //
        // @constant ALWAYS
        // @constant SOMETIMES
        // @constant NEVER
        type(new EnumType<>("Priority", Priority.class));

        //**
        // @enum Side
        // A `Side` of a rectangular region.
        //
        // @constant BOTTOM
        // @constant LEFT
        // @constant RIGHT
        // @constant TOP
        type(new EnumType<>("Side", Side.class));

        //**
        // @enum TextAlignment
        // @constant CENTER
        // @constant JUSTIFY
        // @constant LEFT
        // @constant RIGHT
        type(new EnumType<>("TextAlignment", TextAlignment.class));

        //**
        // @enum VPos
        // @constant BASELINE
        // @constant BOTTOM
        // @constant CENTER
        // @constant TOP
        type(new EnumType<>("VPos", VPos.class));
    }


    //-------------------------------------------------------------------------
    // Configuration

    // None Yet


    //-------------------------------------------------------------------------
    // The Gui Type

    private class WinProxy extends ProxyType<Void> {
        //---------------------------------------------------------------------
        // Constructor

        //**
        // @singleton Win
        // This static type provides access to the application window.
        WinProxy() {
            super("Win");
            staticType();
            staticMethod("css",      this::_css);
            staticMethod("cssFile",  this::_cssFile);
            staticMethod("root",     this::_root);
            staticMethod("setSize",  this::_setSize);
            staticMethod("setTitle", this::_setTitle);
        }

        //---------------------------------------------------------------------
        // Static Methods

        //**
        // @static css
        // @args css
        // @result this
        // Sets the text of the CSS style sheet for the application as a
        // whole to *css*.  For example,
        //
        // ```joe
        // Win.css("""
        //     .label { -fx-text-fill: pink; }
        //     """);
        // ```
        //
        // See [[joe.win#topic.css]] for more on using CSS in `joe win` scripts.
        //
        // **JavaFX:** In particular, this adds the given CSS to the
        // `Scene`'s `stylesheets` property as a `data:` URL containing
        // the given *css* text. The styles are therefore accessible to
        // the entire scene.
        private Object _css(Joe joe, Args args) {
            args.exactArity(1, "css(css)");
            var css = joe.toString(args.next());

            // %-encode for inclusion in a URL
            var encoded = Base64.getEncoder()
                .encodeToString(css.getBytes(StandardCharsets.UTF_8));
            stage.getScene().getStylesheets()
                .add("data:text/css;base64," + encoded);
            return this;
        }

        //**
        // @static cssFile
        // @args filename
        // @result this
        // Sets the CSS style sheet for the application as a whole given
        // a path to a `.css` file.
        //
        // ```joe
        // Win.cssFill("my.css");
        // ```
        //
        // See [[joe.win#topic.css]] for more on using CSS in `joe win` scripts.
        //
        // **JavaFX:** In particular, this adds the given CSS file to the
        // `Scene`'s `stylesheets` property as a `file:` URL. The styles are
        // therefore accessible to // the entire scene.
        private Object _cssFile(Joe joe, Args args) {
            args.exactArity(1, "cssFile(filename)");
            var filename = joe.toString(args.next());

            stage.getScene().getStylesheets()
                .add("file:" + filename);
            return this;
        }

        //**
        // @static root
        // @result VBox
        // Returns the root window, a [[VBox]].
        private Object _root(Joe joe, Args args) {
            args.exactArity(0, "Win.root()");
            return root;
        }

        //**
        // @static setSize
        // @args width, height
        // @result this
        // Sets the preferred size of the root window.  The width and height
        // must be positive.
        private Object _setSize(Joe joe, Args args) {
            args.exactArity(2, "Win.setSize(width, title)");
            var width = joe.toDouble(args.next());
            var height = joe.toDouble(args.next());
            if (width <= 0 || height <= 0) {
                throw new JoeError("Expected positive width and height.");
            }
            stage.setWidth(width);
            stage.setHeight(height);
            return this;
        }

        //**
        // @static setTitle
        // @args title
        // @result this
        // Sets the title of the root window.
        private Object _setTitle(Joe joe, Args args) {
            args.exactArity(1, "Win.setTitle(title)");
            var title = joe.toString(args.next());
            stage.setTitle(title);
            return this;
        }
    }

    //-------------------------------------------------------------------------
    // Static converters for use with properties

    @SuppressWarnings("unused")
    static HPos toHPos(Joe joe, Object arg) {
        return joe.toEnum(arg, HPos.class);
    }

    static Insets toInsets(Joe joe, Object arg) {
        return joe.toClass(arg, Insets.class);
    }

    static Orientation toOrientation(Joe joe, Object arg) {
        return joe.toEnum(arg, Orientation.class);
    }

    static int toSpan(Joe joe, Object arg) {
        var span = joe.toInteger(arg);
        if (span <= 0) {
            throw joe.expected("positive span", arg);
        }
        return span;
    }

    @SuppressWarnings("unused")
    static VPos toVPos(Joe joe, Object arg) {
        return joe.toEnum(arg, VPos.class);
    }
}
