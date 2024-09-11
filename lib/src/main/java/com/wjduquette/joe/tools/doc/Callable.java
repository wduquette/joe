package com.wjduquette.joe.tools.doc;

import java.util.List;

interface Callable {
    //-------------------------------------------------------------------------
    // Accessors

    List<String> argSpecs();

    String returnSpec();
}
