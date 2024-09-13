package com.wjduquette.joe.tools.doc;

import java.io.*;

public class ContentWriter extends PrintWriter {
    //-------------------------------------------------------------------------
    // Constructor

    /**
     * Opens a ContentWriter on the given output stream
     * @param writer The output stream
     */
    public ContentWriter(BufferedWriter writer) {
        super(writer);
    }

    //-------------------------------------------------------------------------
    // Content Output Methods

    /**
     * Outputs a level 1 title.
     * @param title The title
     */
    public void h1(String title) {
        println("# " + title);
        println();
    }

    /**
     * Outputs a level 2 title.
     * @param title The title
     */
    public void h2(String title) {
        println("## " + title);
        println();
    }

    /**
     * Outputs a level 3 title.
     * @param title The title
     */
    public void h3(String title) {
        println("### " + title);
        println();
    }

    /**
     * Outputs an hline
     */
    public void hline() {
        println("---\n");
    }
}
