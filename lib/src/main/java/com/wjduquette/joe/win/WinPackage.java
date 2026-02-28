package com.wjduquette.joe.win;

import com.wjduquette.joe.*;
import com.wjduquette.joe.types.EnumType;
import javafx.geometry.*;
import javafx.scene.control.ContentDisplay;
import javafx.scene.layout.Priority;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.StageStyle;

/**
 * An experimental Joe package for building JavaFX GUIs
 */
public class WinPackage extends NativePackage {
    /** The package, for registration and installation. */
    public static final WinPackage PACKAGE = new WinPackage();

    //-------------------------------------------------------------------------
    // Constructor

    //**
    // @package joe.win
    // %title GUI API
    // This package contains an experimental API for building JavaFX GUIs.
    // It assumes a familiarity with JavaFX.
    //
    // @packageTopic hierarchy
    // %title Widget Hierarchy
    //
    // The `joe.win` widget type hierarchy is a subset of the JavaFX Java
    // class hierarchy.
    //
    // - [[Widget]] (base type)
    //   - [[MenuItem]]
    //     - [[Menu]]
    //   - [[Node]]: (base type)
    //     - [[Parent]]: (base type)
    //         - [[Region]]: (base type)
    //           - [[Control]]: (base type)
    //             - [[Labeled]]: (base type)
    //               - [[Button]]
    //               - [[Label]]
    //             - [[ListView]]
    //             - [[MenuBar]]
    //             - [[Separator]]
    //             - [[SplitPane]]
    //             - [[TabPane]]
    //           - [[Pane]]
    //             - [[GridPane]]
    //             - [[HBox]]
    //             - [[StackPane]]
    //             - [[VBox]]
    //   - [[Scene]]: A JavaFX scene graph
    //   - [[Tab]]: A tab in a [[TabPane]]
    //   - [[Window]]: (base type)
    //     - [[Stage]]: Application window type
    //
    // @packageTopic css
    // %title Styling with CSS
    //
    // Scripts can style widgets using CSS in several different ways.
    //
    // - The [[static:Win.css]] method associates a CSS stylesheet, passed
    //   as a text string, with the application as a whole.
    // - Each [[Node]] subtype has a [[method:Node.styleClasses]] method that returns
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
     */
    public WinPackage() {
        super("joe.win");

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
        type(SceneType.TYPE);
        type(TabClass.TYPE);
        type(WindowType.TYPE);
           type(StageType.TYPE);

        // Miscellaneous Types
        type(InsetsType.TYPE);
        type(ListenerType.TYPE);
        type(WinSingleton.TYPE);

        // Enums

        //**
        // @enum HPos
        // %javaType javafx.geometry.HPos
        // %enumConstants
        type(new EnumType<>("HPos", HPos.class));

        //**
        // @enum ContentDisplay
        // %javaType javafx.scene.control.ContentDisplay
        // %enumConstants
        type(new EnumType<>("ContentDisplay", ContentDisplay.class));

        //**
        // @enum Modality
        // %javaType javafx.stage.Modality
        // %enumConstants
        type(new EnumType<>("Modality", Modality.class));

        //**
        // @enum Orientation
        // %javaType javafx.geometry.Orientation
        // %enumConstants
        type(new EnumType<>("Orientation", Orientation.class));

        //**
        // @enum Pos
        // %javaType javafx.geometry.Pos
        // %enumConstants
        // The `Pos` enum lists ways a widget can be aligned
        // relative to the boundaries of a rectangular space.
        type(new EnumType<>("Pos", Pos.class));

        //**
        // @enum Priority
        // %javaType javafx.scene.layout.Priority
        // %enumConstants
        // The `Priority` enum's values indicate when a widget
        // should resize itself to fit its parent widget.  The
        // default is generally `NEVER`.
        type(new EnumType<>("Priority", Priority.class));

        //**
        // @enum Side
        // %javaType javafx.geometry.Side
        // %enumConstants
        // A `Side` of a rectangular region.
        type(new EnumType<>("Side", Side.class));

        //**
        // @enum StageStyle
        // %javaType javafx.stage.StageStyle
        // %enumConstants
        type(new EnumType<>("StageStyle", StageStyle.class));

        //**
        // @enum TextAlignment
        // %javaType javafx.scene.text.TextAlignment
        // %enumConstants
        type(new EnumType<>("TextAlignment", TextAlignment.class));

        //**
        // @enum VPos
        // %javaType javafx.geometry.VPos
        // %enumConstants
        type(new EnumType<>("VPos", VPos.class));
    }
}
