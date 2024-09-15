package com.wjduquette.joe.tools.doc;

import java.util.List;

interface Callable {
    //-------------------------------------------------------------------------
    // Accessors

    default String prefix() { return null; }
    String filename();
    String id();
    String name();
    List<String> argSpecs();
    String result();
    List<String> content();
}
