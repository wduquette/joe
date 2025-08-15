package com.wjduquette.joe.nero;

public enum Aggregator {
    SUM("sum");

    //-------------------------------------------------------------------------
    // Metadata

    private final String function;

    Aggregator(String function) {
        this.function = function;
    }

    public String function() {
        return function;
    }
}
