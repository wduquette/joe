package com.wjduquette.joe.win;

import com.wjduquette.joe.Args;
import com.wjduquette.joe.Joe;
import com.wjduquette.joe.ProxyType;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

class WinSingleton extends ProxyType<Void> {
    /** Proxy type for installation into an interpreter. */
    public static final WinSingleton TYPE = new WinSingleton();

    //---------------------------------------------------------------------
    // Constructor

    //**
    // @package joe.win
    // @singleton Win
    // This static type provides access to the application window.
    WinSingleton() {
        super("Win");
        staticType();
        staticMethod("css2sheet", this::_css2sheet);
    }

    //---------------------------------------------------------------------
    // Static Methods

    //**
    // @static css2sheet
    // %args css
    // %result url
    // Converts the CSS text to a `data:` URL that can be added to
    // a [[Scene]]'s or [[Node]]'s list of stylesheets.
    //
    // ```joe
    // var sheet = Win.css2sheet("""
    //     .label { -fx-text-fill: pink; }
    //     """);
    // scene.getStylesheets().add(sheet);
    // ```
    //
    // See [[topic:joe.win.css]] for more on using CSS in `joe win` scripts.
    private Object _css2sheet(Joe joe, Args args) {
        args.arity(1, "css2sheet(css)");
        var css = joe.toString(args.next());

        // %-encode for inclusion in a URL
        var encoded = Base64.getEncoder()
            .encodeToString(css.getBytes(StandardCharsets.UTF_8));
        return "data:text/css;base64," + encoded;
    }
}
