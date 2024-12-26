package com.wjduquette.joe.bert;

import java.util.HashMap;
import java.util.Map;

public class BertInstance {
    //-------------------------------------------------------------------------
    // Instance Variables

    final BertClass klass;
    final Map<String,Object> fields = new HashMap<>();

    //-------------------------------------------------------------------------
    // Constructor

    BertInstance(BertClass klass) {
        this.klass = klass;
    }

    //-------------------------------------------------------------------------
    // Object API

    @Override
    public String toString() {
        return "<" + klass.name() + "@" + String.format("%x",hashCode()) + ">";
    }
}
