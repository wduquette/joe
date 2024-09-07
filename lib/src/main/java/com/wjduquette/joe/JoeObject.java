package com.wjduquette.joe;

public interface JoeObject {
    Object get(Token name);

    void set(Token name, Object value);
}
