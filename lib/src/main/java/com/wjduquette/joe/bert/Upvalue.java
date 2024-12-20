package com.wjduquette.joe.bert;

class Upvalue {
    char index;
    boolean isLocal;

    Upvalue(char index, boolean isLocal) {
        this.index = index;
        this.isLocal = isLocal;
    }

    @Override
    public String toString() {
        return "Upvalue[index=" + (int)index + ", isLocal=" + isLocal + "]";
    }
}
