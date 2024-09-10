package com.wjduquette.joe.tools.doc;

public record Line(int number, String text) {
    @Override
    public String toString() {
        return String.format("[line %04d] %s", number, text);
    }
}
