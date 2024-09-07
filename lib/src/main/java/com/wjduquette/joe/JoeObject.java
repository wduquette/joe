package com.wjduquette.joe;

interface JoeObject {
    Object get(Token name);

    void set(Token name, Object value);
}
