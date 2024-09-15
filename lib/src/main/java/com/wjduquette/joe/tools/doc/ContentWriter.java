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
        hline();
    }

    /**
     * Outputs a level 2 title.
     * @param title The title
     */
    public void h2(String title) {
        println("## " + title);
        hline();
    }

    /**
     * Outputs a level 2 title.
     * @param title The title
     */
    public void h2(String id, String title) {
        println("<h2 id=\"" + id + "\">" + title + "</h2>\n");
        hline();
    }

    /**
     * Outputs a level 3 title.
     * @param title The title
     */
    public void h3(String title) {
        println("### " + title);
        hline();
    }

    public void hb(String title) {
        println("**" + title + "**");
    }

    public void hb(String id, String title) {
        println("**[" + title + "](#" + id + ")**");
    }

    /**
     * Outputs a level 3 title with link ID
     * @param id The link ID
     * @param title The title
     */
    public void h3(String id, String title) {
        println("<span id=\"" + id + "\"> </span>\n");
        println("### " + title);
        println();
        hline();
    }

    /**
     * Outputs an hline
     */
    public void hline() {
        println("---\n");
    }


}
