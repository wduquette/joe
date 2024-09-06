package com.wjduquette.joe;

public final class Keyword {
    private final String name;

    /**
     * Creates a new keyword.  The name should omit the leading "#".
     * @param name The keyword's name.
     */
    public Keyword(String name) {
        this.name = name;
    }

    public String name() {
        return name;
    }

    @Override
    public String toString() {
        return "#" + name;
    }
}
